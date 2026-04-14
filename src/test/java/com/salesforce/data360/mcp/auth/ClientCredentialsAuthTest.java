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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientCredentialsAuthTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void authenticateReturnsTokenResponse() {
        // Setup mock chain
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("https://login.salesforce.com/services/oauth2/token")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        OAuthTokenResponse expectedResponse = new OAuthTokenResponse(
            "test-access-token",
            "https://test.salesforce.com",
            "Bearer",
            "1234567890",
            null
        );
        when(responseSpec.body(OAuthTokenResponse.class)).thenReturn(expectedResponse);

        // Execute
        ClientCredentialsAuth auth = new ClientCredentialsAuth(
            restClient,
            "https://login.salesforce.com",
            "test-client-id",
            "test-client-secret"
        );

        OAuthTokenResponse response = auth.authenticate();

        // Verify
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("test-access-token");
        assertThat(response.instanceUrl()).isEqualTo("https://test.salesforce.com");

        // Verify form data was sent
        ArgumentCaptor<MultiValueMap<String, String>> bodyCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(requestBodySpec).body(bodyCaptor.capture());

        MultiValueMap<String, String> formData = bodyCaptor.getValue();
        assertThat(formData.getFirst("grant_type")).isEqualTo("client_credentials");
        assertThat(formData.getFirst("client_id")).isEqualTo("test-client-id");
        assertThat(formData.getFirst("client_secret")).isEqualTo("test-client-secret");
    }
}
