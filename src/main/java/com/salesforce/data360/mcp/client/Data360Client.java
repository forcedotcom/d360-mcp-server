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
package com.salesforce.data360.mcp.client;

import com.salesforce.data360.mcp.auth.AuthService;
import com.salesforce.data360.mcp.model.common.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.function.Supplier;

/**
 * HTTP client for Salesforce Data 360 REST APIs.
 * Base URL: {instanceUrl}/services/data/v{apiVersion}
 *
 * <p>Callers pass paths relative to that base. SSOT endpoints begin with
 * {@code /ssot/...}; other Connect-surface endpoints (e.g.
 * {@code /connect/search/metadata/results},
 * {@code /semantic-engine/gateway}) begin with their own prefixes.
 */
public class Data360Client {

    private static final Logger log = LoggerFactory.getLogger(Data360Client.class);

    private final RestClient restClient;
    private final AuthService authService;
    private final String apiVersion;
    private final String serverCallOptions;

    public Data360Client(RestClient restClient, AuthService authService, String apiVersion,
                         String serverCallOptions) {
        this.restClient = restClient;
        this.authService = authService;
        this.apiVersion = apiVersion;
        this.serverCallOptions = serverCallOptions;
    }

    private String baseUrl() {
        return authService.getInstanceUrl() + "/services/data/v" + apiVersion;
    }

    private String sforceCallOptions() {
        String clientInfo = ClientContext.get();
        if (clientInfo == null) {
            return serverCallOptions;
        }
        return serverCallOptions + "/" + clientInfo;
    }

    public <T> T get(String path, Class<T> responseType) {
        String fullUrl = baseUrl() + path;
        log.debug("GET {}", sanitizeUrl(fullUrl));
        return execute(path, () ->
            restClient.get()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Sforce-Call-Options", sforceCallOptions())
                .retrieve()
                .body(responseType)
        );
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        String fullUrl = baseUrl() + path;
        log.debug("POST {}", sanitizeUrl(fullUrl));
        return execute(path, () ->
            restClient.post()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Sforce-Call-Options", sforceCallOptions())
                .body(body)
                .retrieve()
                .body(responseType)
        );
    }

    public <T> T put(String path, Object body, Class<T> responseType) {
        String fullUrl = baseUrl() + path;
        log.debug("PUT {}", sanitizeUrl(fullUrl));
        return execute(path, () ->
            restClient.put()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Sforce-Call-Options", sforceCallOptions())
                .body(body)
                .retrieve()
                .body(responseType)
        );
    }

    public <T> T patch(String path, Object body, Class<T> responseType) {
        String fullUrl = baseUrl() + path;
        log.debug("PATCH {}", sanitizeUrl(fullUrl));
        return execute(path, () ->
            restClient.patch()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Sforce-Call-Options", sforceCallOptions())
                .body(body)
                .retrieve()
                .body(responseType)
        );
    }

    public void delete(String path) {
        String fullUrl = baseUrl() + path;
        log.debug("DELETE {}", sanitizeUrl(fullUrl));
        execute(path, () -> {
            restClient.delete()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Sforce-Call-Options", sforceCallOptions())
                .retrieve()
                .toBodilessEntity();
            return null;
        });
    }

    public <T> T delete(String path, Class<T> responseType) {
        String fullUrl = baseUrl() + path;
        log.debug("DELETE {}", sanitizeUrl(fullUrl));
        return execute(path, () ->
            restClient.delete()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Sforce-Call-Options", sforceCallOptions())
                .retrieve()
                .body(responseType)
        );
    }

    private <T> T execute(String path, Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ApiException(e.getStatusCode().value(), e.getResponseBodyAsString(), path);
        } catch (ResourceAccessException e) {
            throw new ApiException("Data 360 API connection error on " + path, e);
        }
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("access_token=[^&]*", "access_token=***");
    }
}
