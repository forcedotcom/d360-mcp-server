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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class Data360ClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private AuthService authService;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private Data360Client client;

    @BeforeEach
    void setUp() {
        when(authService.getAccessToken()).thenReturn("test-access-token");
        when(authService.getInstanceUrl()).thenReturn("https://test.salesforce.com");

        client = new Data360Client(restClient, authService, "66.0",
            "client=data360-mcp-server-oss/1.0.0");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getConstructsCorrectUrl() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(URI.create("https://test.salesforce.com/services/data/v66.0/dmos")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        String expectedResponse = "{\"data\": []}";
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String response = client.get("/dmos", String.class);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void postSendsBodyWithCorrectHeaders() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(ArgumentMatchers.any(URI.class)))
            .thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).body(ArgumentMatchers.any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        String expectedResponse = "{\"id\": \"123\"}";
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String requestBody = "{\"name\": \"TestDMO\"}";
        String response = client.post("/dmos", requestBody, String.class);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void putSendsBodyWithCorrectHeaders() {
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(ArgumentMatchers.any(URI.class)))
            .thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).body(ArgumentMatchers.any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        String expectedResponse = "{\"id\": \"123\"}";
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String requestBody = "{\"name\": \"UpdatedDMO\"}";
        String response = client.put("/dmos/123", requestBody, String.class);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void patchSendsBodyWithCorrectHeaders() {
        when(restClient.patch()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(ArgumentMatchers.any(URI.class)))
            .thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).header(anyString(), anyString());
        doReturn(requestBodySpec).when(requestBodySpec).body(ArgumentMatchers.any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        String expectedResponse = "{\"id\": \"123\"}";
        when(responseSpec.body(String.class)).thenReturn(expectedResponse);

        String requestBody = "{\"status\": \"active\"}";
        String response = client.patch("/dmos/123", requestBody, String.class);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteExecutesCorrectly() {
        when(restClient.delete()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(URI.create("https://test.salesforce.com/services/data/v66.0/dmos/123")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        client.delete("/dmos/123");
    }

    @Test
    @SuppressWarnings("unchecked")
    void httpClientErrorThrowsApiException() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(URI.create("https://test.salesforce.com/services/data/v66.0/invalid")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class))
            .thenThrow(HttpClientErrorException.create(
                BAD_REQUEST,
                "Bad Request",
                null,
                "{\"error\": \"Invalid request\"}".getBytes(),
                null
            ));

        assertThatThrownBy(() -> client.get("/invalid", String.class))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Data 360 API error 400")
            .hasMessageContaining("/invalid")
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatusCode()).isEqualTo(400);
                assertThat(apiEx.getRequestPath()).isEqualTo("/invalid");
                assertThat(apiEx.getResponseBody()).contains("Invalid request");
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void httpServerErrorThrowsApiException() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(URI.create("https://test.salesforce.com/services/data/v66.0/error")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class))
            .thenThrow(HttpServerErrorException.create(
                INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                null,
                "{\"error\": \"Server error\"}".getBytes(),
                null
            ));

        assertThatThrownBy(() -> client.get("/error", String.class))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Data 360 API error 500")
            .hasMessageContaining("/error")
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;
                assertThat(apiEx.getStatusCode()).isEqualTo(500);
            });
    }

    @Test
    @SuppressWarnings("unchecked")
    void resourceAccessExceptionThrowsApiException() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(URI.create("https://test.salesforce.com/services/data/v66.0/timeout")))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString()))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class))
            .thenThrow(new ResourceAccessException("Connection timeout", new IOException("timeout")));

        assertThatThrownBy(() -> client.get("/timeout", String.class))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Data 360 API connection error")
            .hasMessageContaining("/timeout")
            .hasCauseInstanceOf(ResourceAccessException.class);
    }
}