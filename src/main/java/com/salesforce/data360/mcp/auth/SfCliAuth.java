/*
 * Copyright (c) 2026, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.salesforce.data360.mcp.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.data360.mcp.model.auth.OAuthTokenResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Authenticator that shells out to the Salesforce CLI (`sf org display --target-org <alias> --json`)
 * to obtain an access token and instance URL. This reuses the interactive auth session the user
 * already established via `sf org login web --alias <alias>`, avoiding any Connected App setup.
 *
 * <p>The token and instance URL are cached by {@link AuthService} via {@link TokenCache} using the
 * TTL reported on the returned {@link OAuthTokenResponse} ({@link #CACHE_TTL_SECONDS}); this class
 * itself is stateless.
 */
public class SfCliAuth implements OAuthAuthenticator {

    static final long PROCESS_TIMEOUT_SECONDS = 120;
    // SF CLI tokens don't expose their true TTL via `org display`; cache briefly so we revalidate
    // often and let the CLI transparently refresh its own session when needed.
    static final long CACHE_TTL_SECONDS = 300;

    private final String orgAlias;
    private final ProcessRunner processRunner;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SfCliAuth(String orgAlias) {
        this(orgAlias, SfCliAuth::runSfCli);
    }

    SfCliAuth(String orgAlias, ProcessRunner processRunner) {
        this.orgAlias = orgAlias;
        this.processRunner = processRunner;
    }

    @Override
    public OAuthTokenResponse authenticate() {
        ProcessResult result = processRunner.run(List.of(
            "sf", "org", "display", "--target-org", orgAlias, "--json"
        ));

        if (result.exitCode() != 0) {
            throw new IllegalStateException(
                "sf CLI failed (exit " + result.exitCode() + ") for org '" + orgAlias + "'. "
                    + "Ensure you've authenticated via: sf org login web --alias " + orgAlias
                    + "\nstderr: " + truncate(result.stderr(), 500)
            );
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(result.stdout());
        } catch (IOException e) {
            throw new IllegalStateException(
                "sf CLI returned invalid JSON: " + truncate(result.stdout(), 500), e);
        }

        JsonNode resultNode = root.path("result");
        String accessToken = resultNode.path("accessToken").asText(null);
        String instanceUrl = resultNode.path("instanceUrl").asText(null);

        if (accessToken == null || accessToken.isBlank()
            || instanceUrl == null || instanceUrl.isBlank()) {
            throw new IllegalStateException(
                "sf CLI response missing accessToken or instanceUrl for org '" + orgAlias + "'");
        }

        return new OAuthTokenResponse(
            accessToken,
            instanceUrl,
            "Bearer",
            null,
            CACHE_TTL_SECONDS
        );
    }

    private static ProcessResult runSfCli(List<String> command) {
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(false).start();
            // Drain stdout and stderr concurrently so the child can't block on a full pipe buffer
            // while we are waiting on the other stream — a classic Process deadlock.
            CompletableFuture<String> stdoutFuture = readStreamAsync(process.getInputStream());
            CompletableFuture<String> stderrFuture = readStreamAsync(process.getErrorStream());

            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException(
                    "sf CLI command timed out after " + PROCESS_TIMEOUT_SECONDS + " seconds");
            }
            return new ProcessResult(
                process.exitValue(),
                stdoutFuture.get(),
                stderrFuture.get()
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to invoke sf CLI. Install from "
                    + "https://developer.salesforce.com/tools/salesforcecli", e);
        } catch (InterruptedException e) {
            if (process != null) process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("sf CLI invocation interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to read sf CLI output", e.getCause());
        }
    }

    private static CompletableFuture<String> readStreamAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new String(stream.readAllBytes());
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    @FunctionalInterface
    interface ProcessRunner {
        ProcessResult run(List<String> command);
    }

    record ProcessResult(int exitCode, String stdout, String stderr) {
    }
}
