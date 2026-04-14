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
package com.salesforce.data360.mcp.tools;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GdprToolsTest {

    @Mock
    private Data360Client client;

    private GdprTools gdprTools;

    @BeforeEach
    void setUp() {
        gdprTools = new GdprTools(client);
    }

    @Test
    void testGdprRead_success() {
        Map<String, Object> mockResponse = Map.of("data", "personal_data");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = gdprTools.gdprRead("ind-123", "ACCESS");

        assertThat(result).contains("personal_data");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/cdp/gdpr/read/ind-123/ACCESS");
    }

    @Test
    void testGdprRead_error() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not Found", "/cdp/gdpr/read"));

        String result = gdprTools.gdprRead("ind-999", "ACCESS");

        assertThat(result).contains("error", "404");
    }

    @Test
    void testGdprBulkRead_success() {
        Map<String, Object> mockResponse = Map.of("results", "bulk_data");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = gdprTools.gdprBulkRead("ind1:ACCESS,ind2:ACCESS");

        assertThat(result).contains("bulk_data");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/cdp/gdpr/bulkRead/");
    }

    @Test
    void testGdprRequest_success() {
        Map<String, Object> mockResponse = Map.of("requestId", "req-456");
        when(client.put(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        String result = gdprTools.gdprRequest("{\"requestType\":\"DELETE\",\"individuals\":[\"ind1\"]}");

        assertThat(result).contains("req-456");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).put(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/cdp/gdpr");
    }

    @Test
    void testGdprRequest_invalidJson() {
        String result = gdprTools.gdprRequest("not-valid-json");

        assertThat(result).contains("error");
    }
}
