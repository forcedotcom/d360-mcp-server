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

import com.salesforce.data360.mcp.config.AppProperties;
import com.salesforce.data360.mcp.model.auth.OAuthTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final long DEFAULT_TOKEN_TTL_SECONDS = 3600;

    private final AppProperties properties;
    private final TokenCache tokenCache;
    private final String authMode;
    private final OAuthAuthenticator oauthAuthenticator;

    @Autowired
    public AuthService(AppProperties properties) {
        this(properties, RestClient.builder().build());
    }

    AuthService(AppProperties properties, RestClient restClient) {
        this.properties = properties;
        this.tokenCache = new TokenCache();
        this.authMode = resolveAuthMode(properties);
        this.oauthAuthenticator = initializeOAuthAuthenticator(properties, restClient);
    }

    public String getAccessToken() {
        if (AuthModeResolver.ACCESS_TOKEN.equals(authMode)) {
            return properties.getAccessToken();
        }
        if (oauthAuthenticator == null) {
            throw new IllegalStateException("No authentication configured");
        }
        return tokenCache.getAccessToken()
            .orElseGet(() -> performOAuthAndCache().accessToken());
    }

    public String getInstanceUrl() {
        if (AuthModeResolver.ACCESS_TOKEN.equals(authMode)) {
            return properties.getInstanceUrl();
        }
        if (oauthAuthenticator == null) {
            throw new IllegalStateException("No authentication configured");
        }
        return tokenCache.getInstanceUrl()
            .orElseGet(() -> performOAuthAndCache().instanceUrl());
    }

    private synchronized OAuthTokenResponse performOAuthAndCache() {
        // Double-check after acquiring lock
        if (tokenCache.getAccessToken().isPresent()) {
            return new OAuthTokenResponse(
                tokenCache.getAccessToken().get(),
                tokenCache.getInstanceUrl().orElse(null),
                null, null, null
            );
        }

        OAuthTokenResponse response = oauthAuthenticator.authenticate();
        long ttlSeconds = response.expiresIn() != null
            ? response.expiresIn() : DEFAULT_TOKEN_TTL_SECONDS;
        tokenCache.store(
            response.accessToken(), response.instanceUrl(), Duration.ofSeconds(ttlSeconds)
        );
        log.info("OAuth authentication successful, token cached for {} seconds", ttlSeconds);
        return response;
    }

    private static String resolveAuthMode(AppProperties properties) {
        String mode = AuthModeResolver.determineAuthMode(properties);
        if (AuthModeResolver.NONE.equals(mode)) {
            log.warn("No valid authentication configuration found. "
                + "Service will not be able to make Data 360 API calls.");
        }
        return mode;
    }

    private static OAuthAuthenticator initializeOAuthAuthenticator(
            AppProperties properties, RestClient restClient) {
        String mode = AuthModeResolver.determineAuthMode(properties);
        switch (mode) {
            case AuthModeResolver.CLIENT_CREDENTIALS:
                log.info("Initialized with client_credentials auth mode");
                return new ClientCredentialsAuth(
                    restClient,
                    properties.getLoginUrl(),
                    properties.getClientId(),
                    properties.getClientSecret()
                );

            case AuthModeResolver.SF_CLI:
                log.info("Initialized with sf_cli auth mode (org alias: {})",
                    properties.getSfOrgAlias());
                return new SfCliAuth(properties.getSfOrgAlias());

            case AuthModeResolver.ACCESS_TOKEN:
                log.info("Initialized with access_token auth mode");
                return null;

            default:
                log.warn("No authentication strategy initialized");
                return null;
        }
    }
}
