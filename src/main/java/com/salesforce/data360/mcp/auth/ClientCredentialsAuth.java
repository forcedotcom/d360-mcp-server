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

import com.salesforce.data360.mcp.model.auth.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * OAuth 2.0 Client Credentials flow authentication.
 */
public class ClientCredentialsAuth implements OAuthAuthenticator {

    private final RestClient restClient;
    private final String loginUrl;
    private final String clientId;
    private final String clientSecret;

    public ClientCredentialsAuth(RestClient restClient, String loginUrl, String clientId, String clientSecret) {
        this.restClient = restClient;
        this.loginUrl = loginUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Perform OAuth 2.0 client credentials authentication.
     */
    public OAuthTokenResponse authenticate() {
        String tokenEndpoint = loginUrl + "/services/oauth2/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return restClient.post()
            .uri(tokenEndpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(OAuthTokenResponse.class);
    }
}
