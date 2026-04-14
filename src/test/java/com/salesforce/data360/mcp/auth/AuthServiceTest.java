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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void usesAccessTokenAuthWhenTokenAndUrlProvided() {
        AppProperties properties = new AppProperties();
        properties.setAccessToken("test-token");
        properties.setInstanceUrl("https://test.salesforce.com");
        properties.setApiVersion("66.0");

        AuthService service = new AuthService(properties, restClient);

        assertThat(service.getAccessToken()).isEqualTo("test-token");
        assertThat(service.getInstanceUrl()).isEqualTo("https://test.salesforce.com");
    }

    @Test
    void usesClientCredentialsAuthWhenConfigured() {
        AppProperties properties = new AppProperties();
        properties.setAuthFlow("client_credentials");
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        properties.setLoginUrl("https://login.salesforce.com");
        properties.setApiVersion("66.0");

        AuthService service = new AuthService(properties, restClient);
        stubOAuthPost("https://login.salesforce.com/services/oauth2/token");

        assertThat(service.getAccessToken()).isEqualTo("oauth-token");
        assertThat(service.getAccessToken()).isEqualTo("oauth-token");
        assertThat(service.getInstanceUrl()).isEqualTo("https://oauth.salesforce.com");
    }

    @Test
    void throwsWhenNoAuthConfigured() {
        AppProperties properties = new AppProperties();

        AuthService service = new AuthService(properties, restClient);

        assertThatThrownBy(service::getAccessToken)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No authentication configured");

        assertThatThrownBy(service::getInstanceUrl)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No authentication configured");
    }

    private void stubOAuthPost(String tokenUri) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(tokenUri)).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(OAuthTokenResponse.class)).thenReturn(new OAuthTokenResponse(
            "oauth-token",
            "https://oauth.salesforce.com",
            "Bearer",
            "1234567890",
            null
        ));
    }
}
