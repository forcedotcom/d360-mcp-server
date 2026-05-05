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
import com.salesforce.data360.mcp.model.request.retriever.RetrieverConfigurationCreateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverConfigurationUpdateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverCreateRequest;
import com.salesforce.data360.mcp.model.request.retriever.RetrieverUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrieverToolsTest {

    @Mock
    private Data360Client client;

    private RetrieverTools retrieverTools;

    @BeforeEach
    void setUp() {
        retrieverTools = new RetrieverTools(client);
    }

    // ============================================================
    // List Retrievers
    // ============================================================

    @Test
    void testListRetrievers_success() {
        Map<String, Object> mockResponse = Map.of(
            "retrievers", List.of(
                Map.of("name", "MyRetriever", "queryType", "NoCode")
            ),
            "totalSize", 1
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = retrieverTools.listRetrievers(null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("MyRetriever");
        assertThat(result).contains("NoCode");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers");
    }

    @Test
    void testListRetrievers_withFilters() {
        Map<String, Object> mockResponse = Map.of("retrievers", List.of(), "totalSize", 0);
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        retrieverTools.listRetrievers(10, 5, "test", "Name", "ASC", null, true, null, "NoCode");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("limit=10");
        assertThat(pathCaptor.getValue()).contains("offset=5");
        assertThat(pathCaptor.getValue()).contains("search=test");
        assertThat(pathCaptor.getValue()).contains("sortBy=Name");
        assertThat(pathCaptor.getValue()).contains("sortOrder=ASC");
        assertThat(pathCaptor.getValue()).contains("isActive=true");
        assertThat(pathCaptor.getValue()).contains("queryType=NoCode");
    }

    @Test
    void testListRetrievers_apiError() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(403, "Forbidden", "/ssot/machine-learning/retrievers"));

        String result = retrieverTools.listRetrievers(null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("403");
    }

    @Test
    void testListRetrievers_connectionError() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException("Connection timeout", new RuntimeException("timeout")));

        String result = retrieverTools.listRetrievers(null, null, null, null, null, null, null, null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("Connection timeout");
    }

    // ============================================================
    // Get Retriever
    // ============================================================

    @Test
    void testGetRetriever_success() {
        Map<String, Object> mockResponse = Map.of(
            "name", "MyRetriever",
            "queryType", "NoCode",
            "description", "Test retriever"
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = retrieverTools.getRetriever("MyRetriever");

        assertThat(result).contains("MyRetriever");
        assertThat(result).contains("NoCode");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever");
    }

    @Test
    void testGetRetriever_notFound() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/machine-learning/retrievers/nonexistent"));

        String result = retrieverTools.getRetriever("nonexistent");

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    @Test
    void testGetRetriever_connectionError() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException("Connection refused", new RuntimeException("refused")));

        String result = retrieverTools.getRetriever("MyRetriever");

        assertThat(result).contains("error");
        assertThat(result).contains("Connection refused");
    }

    // ============================================================
    // Create Retriever
    // ============================================================

    @Test
    void testCreateRetriever_success() {
        Map<String, Object> mockResponse = Map.of(
            "name", "TestRetriever_1Cx_abc123",
            "queryType", "NoCode"
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        RetrieverCreateRequest request = new RetrieverCreateRequest();
        request.setLabel("TestRetriever");
        request.setDescription("A test retriever");
        request.setDataSourceType("SearchIndex");

        String result = retrieverTools.createRetriever(request);

        assertThat(result).contains("TestRetriever_1Cx_abc123");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "TestRetriever");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "A test retriever");
        assertThat(bodyCaptor.getValue()).containsEntry("dataSourceType", "SearchIndex");
    }

    @Test
    void testCreateRetriever_apiError() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/machine-learning/retrievers"));

        RetrieverCreateRequest request = new RetrieverCreateRequest();
        request.setLabel("Bad");

        String result = retrieverTools.createRetriever(request);

        assertThat(result).contains("error");
        assertThat(result).contains("400");
    }

    @Test
    void testCreateRetriever_connectionError() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException("Timeout", new RuntimeException("timeout")));

        RetrieverCreateRequest request = new RetrieverCreateRequest();
        request.setLabel("Test");

        String result = retrieverTools.createRetriever(request);

        assertThat(result).contains("error");
        assertThat(result).contains("Timeout");
    }

    // ============================================================
    // Update Retriever
    // ============================================================

    @Test
    void testUpdateRetriever_success() {
        Map<String, Object> mockResponse = Map.of("name", "MyRetriever", "label", "Updated Label");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        RetrieverUpdateRequest request = new RetrieverUpdateRequest();
        request.setLabel("Updated Label");
        request.setDescription("Updated description");

        String result = retrieverTools.updateRetriever("MyRetriever", request);

        assertThat(result).contains("Updated Label");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Updated Label");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Updated description");
    }

    @Test
    void testUpdateRetriever_apiError() {
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/machine-learning/retrievers/nonexistent"));

        RetrieverUpdateRequest request = new RetrieverUpdateRequest();
        request.setLabel("Test");

        String result = retrieverTools.updateRetriever("nonexistent", request);

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    @Test
    void testUpdateRetriever_connectionError() {
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException("Connection reset", new RuntimeException("reset")));

        RetrieverUpdateRequest request = new RetrieverUpdateRequest();
        request.setLabel("Test");

        String result = retrieverTools.updateRetriever("MyRetriever", request);

        assertThat(result).contains("error");
        assertThat(result).contains("Connection reset");
    }

    // ============================================================
    // Delete Retriever
    // ============================================================

    @Test
    void testDeleteRetriever_success() {
        doNothing().when(client).delete(anyString());

        String result = retrieverTools.deleteRetriever("MyRetriever");

        assertThat(result).contains("deleted");
        assertThat(result).contains("MyRetriever");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever");
    }

    @Test
    void testDeleteRetriever_apiError() {
        doThrow(new ApiException(404, "Not found", "/ssot/machine-learning/retrievers/nonexistent"))
            .when(client).delete(anyString());

        String result = retrieverTools.deleteRetriever("nonexistent");

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    @Test
    void testDeleteRetriever_connectionError() {
        doThrow(new ApiException("Timeout", new RuntimeException("timeout")))
            .when(client).delete(anyString());

        String result = retrieverTools.deleteRetriever("MyRetriever");

        assertThat(result).contains("error");
        assertThat(result).contains("Timeout");
    }

    // ============================================================
    // List Retriever Configurations
    // ============================================================

    @Test
    void testListRetrieverConfigurations_success() {
        Map<String, Object> mockResponse = Map.of(
            "configurations", List.of(
                Map.of("name", "config_v1", "version", 1)
            ),
            "totalSize", 1
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = retrieverTools.listRetrieverConfigurations("MyRetriever", null, null);

        assertThat(result).contains("config_v1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever/configurations");
    }

    @Test
    void testListRetrieverConfigurations_withPagination() {
        Map<String, Object> mockResponse = Map.of("configurations", List.of(), "totalSize", 0);
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        retrieverTools.listRetrieverConfigurations("MyRetriever", 5, 10);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("limit=5");
        assertThat(pathCaptor.getValue()).contains("offset=10");
    }

    @Test
    void testListRetrieverConfigurations_apiError() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Retriever not found", "/ssot/machine-learning/retrievers/bad/configurations"));

        String result = retrieverTools.listRetrieverConfigurations("bad", null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    // ============================================================
    // Get Retriever Configuration
    // ============================================================

    @Test
    void testGetRetrieverConfiguration_success() {
        Map<String, Object> mockResponse = Map.of(
            "name", "config_v1",
            "version", 1,
            "retrievalMode", "Basic"
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = retrieverTools.getRetrieverConfiguration("MyRetriever", "config_v1");

        assertThat(result).contains("config_v1");
        assertThat(result).contains("Basic");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever/configurations/config_v1");
    }

    @Test
    void testGetRetrieverConfiguration_notFound() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Configuration not found", "/ssot/machine-learning/retrievers/MyRetriever/configurations/bad"));

        String result = retrieverTools.getRetrieverConfiguration("MyRetriever", "bad");

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    // ============================================================
    // Create Retriever Configuration
    // ============================================================

    @Test
    void testCreateRetrieverConfiguration_success() {
        Map<String, Object> mockResponse = Map.of(
            "name", "config_v2",
            "version", 2,
            "queryType", "NoCode"
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        RetrieverConfigurationCreateRequest request = new RetrieverConfigurationCreateRequest();
        request.setQueryType("NoCode");
        request.setIsActive(true);
        request.setNumberOfResults(5);

        String result = retrieverTools.createRetrieverConfiguration("MyRetriever", request);

        assertThat(result).contains("config_v2");
        assertThat(result).contains("NoCode");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever/configurations");
        assertThat(bodyCaptor.getValue()).containsEntry("queryType", "NoCode");
        assertThat(bodyCaptor.getValue()).containsEntry("isActive", true);
        assertThat(bodyCaptor.getValue()).containsEntry("numberOfResults", 5);
    }

    @Test
    void testCreateRetrieverConfiguration_apiError() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid configuration", "/ssot/machine-learning/retrievers/MyRetriever/configurations"));

        RetrieverConfigurationCreateRequest request = new RetrieverConfigurationCreateRequest();
        request.setQueryType("Invalid");

        String result = retrieverTools.createRetrieverConfiguration("MyRetriever", request);

        assertThat(result).contains("error");
        assertThat(result).contains("400");
    }

    @Test
    void testCreateRetrieverConfiguration_connectionError() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException("Timeout", new RuntimeException("timeout")));

        RetrieverConfigurationCreateRequest request = new RetrieverConfigurationCreateRequest();
        request.setQueryType("NoCode");

        String result = retrieverTools.createRetrieverConfiguration("MyRetriever", request);

        assertThat(result).contains("error");
        assertThat(result).contains("Timeout");
    }

    // ============================================================
    // Update Retriever Configuration
    // ============================================================

    @Test
    void testUpdateRetrieverConfiguration_success() {
        Map<String, Object> mockResponse = Map.of("name", "config_v1", "isActive", true);
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        RetrieverConfigurationUpdateRequest request = new RetrieverConfigurationUpdateRequest();
        request.setIsActive(true);

        String result = retrieverTools.updateRetrieverConfiguration("MyRetriever", "config_v1", request);

        assertThat(result).contains("config_v1");
        assertThat(result).contains("true");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever/configurations/config_v1");
        assertThat(bodyCaptor.getValue()).containsEntry("isActive", true);
    }

    @Test
    void testUpdateRetrieverConfiguration_apiError() {
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(409, "Conflict", "/ssot/machine-learning/retrievers/MyRetriever/configurations/config_v1"));

        RetrieverConfigurationUpdateRequest request = new RetrieverConfigurationUpdateRequest();
        request.setIsActive(true);

        String result = retrieverTools.updateRetrieverConfiguration("MyRetriever", "config_v1", request);

        assertThat(result).contains("error");
        assertThat(result).contains("409");
    }

    // ============================================================
    // Delete Retriever Configuration
    // ============================================================

    @Test
    void testDeleteRetrieverConfiguration_success() {
        doNothing().when(client).delete(anyString());

        String result = retrieverTools.deleteRetrieverConfiguration("MyRetriever", "config_v1");

        assertThat(result).contains("deleted");
        assertThat(result).contains("MyRetriever");
        assertThat(result).contains("config_v1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture());
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/machine-learning/retrievers/MyRetriever/configurations/config_v1");
    }

    @Test
    void testDeleteRetrieverConfiguration_apiError() {
        doThrow(new ApiException(404, "Not found", "/ssot/machine-learning/retrievers/MyRetriever/configurations/bad"))
            .when(client).delete(anyString());

        String result = retrieverTools.deleteRetrieverConfiguration("MyRetriever", "bad");

        assertThat(result).contains("error");
        assertThat(result).contains("404");
    }

    @Test
    void testDeleteRetrieverConfiguration_connectionError() {
        doThrow(new ApiException("Connection refused", new RuntimeException("refused")))
            .when(client).delete(anyString());

        String result = retrieverTools.deleteRetrieverConfiguration("MyRetriever", "config_v1");

        assertThat(result).contains("error");
        assertThat(result).contains("Connection refused");
    }

    // ============================================================
    // Query Retriever (Runtime)
    // ============================================================

}
