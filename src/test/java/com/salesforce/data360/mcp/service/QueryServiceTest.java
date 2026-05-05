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
package com.salesforce.data360.mcp.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

    @Mock
    private Data360Client client;

    private QueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new QueryService(client);
    }

    @Test
    void querySql_success() {
        // Given
        String sql = "SELECT * FROM Individual__dlm";
        String dataspace = "default";
        Integer rowLimit = 100;

        Map<String, Object> mockResponse = Map.of(
            "queryId", "query-123",
            "completionStatus", "COMPLETED",
            "rowCount", 50
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.querySql(sql, dataspace, null, rowLimit, null, null, null);

        // Then
        assertThat(result).contains("query-123", "COMPLETED");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);

        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/query-sql?dataspace=default");
        assertThat(bodyCaptor.getValue()).containsEntry("sql", sql);
        assertThat(bodyCaptor.getValue()).containsEntry("rowLimit", rowLimit);
    }

    @Test
    void querySqlStatus_success() {
        // Given
        String queryId = "query-123";
        String dataspace = "default";
        Integer waitTimeMs = 5000;

        Map<String, Object> mockResponse = Map.of(
            "queryId", queryId,
            "completionStatus", "RUNNING",
            "progress", 50
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.querySqlStatus(queryId, dataspace, null, waitTimeMs);

        // Then
        assertThat(result).contains("RUNNING", "progress");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/query-sql/query-123", "dataspace=default", "waitTimeMs=5000");
    }

    @Test
    void querySql_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid SQL", "/query-sql"));

        // When
        String result = queryService.querySql("INVALID SQL", null, null, null, null, null, null);

        // Then
        assertThat(result).contains("error", "Invalid SQL", "400");
    }

    @Test
    void querySqlRows_success() {
        // Given
        String queryId = "query-123";
        Integer offset = 0;
        Integer rowLimit = 100;

        Map<String, Object> mockResponse = Map.of(
            "data", Map.of("rows", java.util.List.of())
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.querySqlRows(queryId, offset, rowLimit, null, null, null);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/query-sql/query-123/rows", "offset=0", "rowLimit=100");
    }

    @Test
    void cancelQuerySql_success() {
        // Given
        String queryId = "query-123";
        String dataspace = "default";

        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.cancelQuerySql(queryId, dataspace, null);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/query-sql/query-123", "dataspace=default");
    }

    @Test
    void queryProfile_basicQuery() {
        // Given
        String dataModelName = "Individual";
        String dataspace = "default";

        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.queryProfile(dataModelName, null, null, null, null, null, null, null, null, null, dataspace);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/profile/Individual");
    }

    @Test
    void getProfileMetadata_noModelName() {
        // Given
        String dataspace = "default";

        Map<String, Object> mockResponse = Map.of(
            "profiles", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.getProfileMetadata(null, dataspace);

        // Then
        assertThat(result).contains("profiles");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/profile/metadata", "dataspace=default");
    }

    @Test
    void queryDataGraph_success() {
        // Given
        String dataGraphEntityName = "MyGraph";
        String id = "rec-1";

        Map<String, Object> mockResponse = Map.of("data", Map.of("id", id));

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.queryDataGraph(dataGraphEntityName, id, null, null);

        // Then
        assertThat(result).contains("rec-1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-graphs/data/MyGraph/rec-1");
    }

    @Test
    void lookupDataGraph_success() {
        // Given
        String dataGraphEntityName = "MyGraph";
        String lookupKeys = "email@test.com";

        Map<String, Object> mockResponse = Map.of("results", java.util.List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.lookupDataGraph(dataGraphEntityName, lookupKeys, null, null);

        // Then
        assertThat(result).contains("results");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-graphs/data/MyGraph", "lookupKeys=email%40test.com");
    }

    @Test
    void getDataGraphMetadata_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("entities", java.util.List.of("Graph1"));

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryService.getDataGraphMetadata(null, null);

        // Then
        assertThat(result).contains("Graph1");

        verify(client).get("/ssot/data-graphs/metadata", Map.class);
    }
}
