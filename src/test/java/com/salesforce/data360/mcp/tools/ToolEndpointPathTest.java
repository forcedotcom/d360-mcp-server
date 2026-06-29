/*
 * Copyright (c) 2026, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.salesforce.data360.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.data360.mcp.runtime.FamilyCatalog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Validates every {@link FamilyCatalog#getAllToolDefs()} entry's (httpMethod, apiPath)
 * against the {@code connect-routes.json} manifest extracted from connect-api goldfiles.
 *
 * <p>Test fails when a tool's path or verb does not match what the connect-api
 * source-of-truth actually ships — usually because of a typo, a deprecated route,
 * or a route renamed in core. Regenerate the manifest by running
 * {@link ConnectRoutesManifestGeneratorTest} with {@code -Dgoldfiles=...} when
 * core's goldfiles update.
 */
class ToolEndpointPathTest {

    private static List<Route> manifestRoutes;
    private static String manifestCoreBranch;

    /**
     * Tools whose endpoints are intentionally absent from the connect-api goldfiles.
     * GDPR resources are declared with {@code @ConnectHidden(from = ConnectApiType.Apex)}
     * which excludes them from goldfile generation, but they ship at runtime.
     * Reviewed against cdp-connect-api source on 2026-05-07.
     */
    private static final Set<String> KNOWN_GOLDFILE_GAPS = Set.of(
        // Add tool names here when they're known to ship outside the goldfile system.
    );

    /**
     * Tools that talk to non-connect-api surfaces (a different service entirely)
     * and so are out of scope for this test.
     */
    private static final Set<String> NON_CONNECT_API = Set.of(
        // Smart family
        "d360_smart_mapping_suggest", "d360_preview_field_matches", "d360_smart_event_date",
        "d360_smart_filter_audit",
        // Standard mapping family (uses /connect/standard-mapping/* not /ssot/*)
        "d360_standard_mapping_definitions", "d360_standard_mapping_preview",
        "d360_standard_mapping_create",
        // Search/metadata-search (uses /connect/search/*)
        "d360_metadata_search",
        // Snowflake direct (uses /connections, not /ssot/connections)
        "d360_snowflake_connection_list", "d360_connection_create_snowflake",
        // Semantic engine (separate service)
        "d360_sdm_query"
    );

    @BeforeAll
    static void loadManifest() throws IOException {
        ObjectMapper m = new ObjectMapper();
        try (InputStream in =
                ToolEndpointPathTest.class.getResourceAsStream("/connect-routes.json")) {
            if (in == null) {
                // Manifest is generated locally from core goldfiles and is not shipped in
                // the public OSS repo. Tests that depend on it skip when it's absent.
                manifestRoutes = null;
                manifestCoreBranch = null;
                return;
            }
            JsonNode root = m.readTree(in);
            manifestCoreBranch = root.path("coreBranch").asText("unknown");
            manifestRoutes = new ArrayList<>();
            for (JsonNode r : root.path("routes")) {
                String path = r.path("path").asText();
                List<String> methods = new ArrayList<>();
                r.path("methods").forEach(n -> methods.add(n.asText()));
                manifestRoutes.add(new Route(path, methods, compileTemplate(path)));
            }
            if (manifestRoutes.isEmpty()) {
                fail("connect-routes.json has 0 routes — manifest looks broken.");
            }
        }
    }

    @Test
    @DisplayName("Every FamilyCatalog tool has a matching connect-api route")
    void everyToolMatchesAManifestRoute() {
        assumeTrue(manifestRoutes != null,
                "connect-routes.json not on classpath — regenerate via "
                        + "ConnectRoutesManifestGeneratorTest with -Dgoldfiles=...");
        FamilyCatalog catalog = new FamilyCatalog();
        List<String> failures = new ArrayList<>();

        for (FamilyCatalog.ToolDef def : catalog.getAllToolDefs()) {
            String path = def.apiPath();
            String method = def.httpMethod();
            if (path == null || method == null) {
                continue;
            }
            if (!path.startsWith("/ssot/") && !path.startsWith("/cdp/")) {
                continue;
            }
            if (KNOWN_GOLDFILE_GAPS.contains(def.name())) {
                continue;
            }
            if (NON_CONNECT_API.contains(def.name())) {
                continue;
            }

            String normalized = normalizeBraces(path);
            List<Route> pathMatches = new ArrayList<>();
            for (Route r : manifestRoutes) {
                if (r.regex().matcher(normalized).matches()) {
                    pathMatches.add(r);
                }
            }

            if (pathMatches.isEmpty()) {
                failures.add(String.format(
                        "Tool '%s' (%s %s): no manifest route matches path. "
                                + "Closest paths: %s",
                        def.name(), method, path, suggest(normalized)));
                continue;
            }

            boolean methodOk = pathMatches.stream()
                    .anyMatch(r -> r.methods().contains(method));
            if (!methodOk) {
                List<String> allowedMethods = new ArrayList<>();
                for (Route r : pathMatches) {
                    for (String mth : r.methods()) {
                        if (!allowedMethods.contains(mth)) allowedMethods.add(mth);
                    }
                }
                failures.add(String.format(
                        "Tool '%s' (%s %s): path matches manifest but method does not. "
                                + "Manifest allows %s on this path.",
                        def.name(), method, path, allowedMethods));
            }
        }

        if (!failures.isEmpty()) {
            String header = String.format(
                    "%d FamilyCatalog tools failed validation against connect-routes.json (coreBranch=%s):%n",
                    failures.size(), manifestCoreBranch);
            fail(header + String.join("\n", failures));
        }
    }

    @Test
    @DisplayName("Manifest coverage report (informational, never fails)")
    void manifestCoverageReport() {
        assumeTrue(manifestRoutes != null,
                "connect-routes.json not on classpath — skipping coverage report");
        FamilyCatalog catalog = new FamilyCatalog();
        long toolPaths = catalog.getAllToolDefs().stream()
                .filter(d -> d.httpMethod() != null && d.apiPath() != null)
                .map(d -> d.httpMethod() + " " + normalizeBraces(d.apiPath()))
                .distinct()
                .count();
        System.out.printf(
                "[connect-routes coverage] coreBranch=%s, manifest=%d, tools=%d%n",
                manifestCoreBranch, manifestRoutes.size(), toolPaths);
        assertFalse(false);
    }

    private static String normalizeBraces(String path) {
        if (path == null) return "";
        return path.replaceAll("\\{([^}]+)\\}", "\\${$1}");
    }

    private static Pattern compileTemplate(String manifestPath) {
        StringBuilder sb = new StringBuilder("^");
        int i = 0;
        while (i < manifestPath.length()) {
            char c = manifestPath.charAt(i);
            if (c == '$' && i + 1 < manifestPath.length() && manifestPath.charAt(i + 1) == '{') {
                int end = manifestPath.indexOf('}', i + 2);
                if (end < 0) break;
                sb.append("[^/]+");
                i = end + 1;
            } else {
                if ("\\.+*?()[]{}|^$".indexOf(c) >= 0) sb.append('\\');
                sb.append(c);
                i++;
            }
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    private static String suggest(String normalized) {
        String[] segs = normalized.split("/");
        String prefix = "/" + segs[1] + (segs.length > 2 ? "/" + segs[2] : "");
        return manifestRoutes.stream()
                .map(Route::path)
                .filter(p -> p.startsWith(prefix))
                .limit(3)
                .toList()
                .toString();
    }

    private record Route(String path, List<String> methods, Pattern regex) {}
}
