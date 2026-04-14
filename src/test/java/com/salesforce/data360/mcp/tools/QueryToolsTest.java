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
import com.salesforce.data360.mcp.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryToolsTest {

    @Mock
    private Data360Client client;

    private QueryTools queryTools;

    @BeforeEach
    void setUp() {
        queryTools = new QueryTools(new QueryService(client));
    }

    @Test
    void testQuerySql_success() {
        // Given
        String sql = "SELECT * FROM Individual__dlm";
        Integer rowLimit = 100;
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "queryId", "query-123",
            "completionStatus", "COMPLETED",
            "rowCount", 50
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.querySql(sql, dataspace, null, rowLimit, null, null, null);

        // Then
        assertThat(result).contains("query-123", "COMPLETED");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(
            pathCaptor.capture(),
            argThat((Map<String, Object> body) ->
                sql.equals(body.get("sql")) && rowLimit.equals(body.get("rowLimit"))
            ),
            eq(Map.class)
        );

        assertThat(pathCaptor.getValue()).isEqualTo("/query-sql?dataspace=default");
    }

    @Test
    void testQuerySql_withAllParams() {
        // Given
        String sql = "SELECT * FROM Individual__dlm";
        String dataspace = DEFAULT_DATASPACE;
        String workloadName = "myWorkload";
        Integer rowLimit = 50;
        Map<String, String> querySettings = Map.of("timeout", "30000");
        String sqlParameters = "[{\"name\":\"param1\",\"value\":\"value1\"}]";
        Integer adaptiveTimeout = 60000;

        Map<String, Object> mockResponse = Map.of("queryId", "query-456");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.querySql(sql, dataspace, workloadName, rowLimit, querySettings, sqlParameters, adaptiveTimeout);

        // Then
        assertThat(result).contains("query-456");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(
            pathCaptor.capture(),
            argThat((Map<String, Object> body) ->
                sql.equals(body.get("sql"))
                    && rowLimit.equals(body.get("rowLimit"))
                    && querySettings.equals(body.get("querySettings"))
                    && adaptiveTimeout.equals(body.get("adaptiveTimeout"))
            ),
            eq(Map.class)
        );

        assertThat(pathCaptor.getValue()).contains("dataspace=default", "workloadName=myWorkload");
    }

    @Test
    void testQuerySql_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid SQL", "/query-sql"));

        // When
        String result = queryTools.querySql("INVALID SQL", null, null, null, null, null, null);

        // Then
        assertThat(result).contains("error", "Invalid SQL", "400");
    }

    @Test
    void testQuerySqlStatus_success() {
        // Given
        String queryId = "query-123";
        String dataspace = DEFAULT_DATASPACE;
        Integer waitTimeMs = 5000;

        Map<String, Object> mockResponse = Map.of(
            "queryId", queryId,
            "completionStatus", "RUNNING",
            "progress", 50
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.querySqlStatus(queryId, dataspace, null, waitTimeMs);

        // Then
        assertThat(result).contains("RUNNING", "progress");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/query-sql/query-123", "dataspace=default", "waitTimeMs=5000");
    }

    @Test
    void testQuerySqlRows_success() {
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
        String result = queryTools.querySqlRows(queryId, offset, rowLimit, null, null, null);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/query-sql/query-123/rows", "offset=0", "rowLimit=100");
    }

    @Test
    void testCancelQuerySql_success() {
        // Given
        String queryId = "query-123";
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.cancelQuerySql(queryId, dataspace, null);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/query-sql/query-123", "dataspace=default");
    }

    @Test
    void testQueryAnsiSql_success() {
        // Given
        String sql = "SELECT * FROM Individual__dlm";
        Integer batchSize = 50;
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of()
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.queryAnsiSql(sql, batchSize, null, null, dataspace);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/query", "batchSize=50", "dataspace=default");
    }

    @Test
    void testQueryAnsiSqlV2_withSql() {
        // Given
        String sql = "SELECT * FROM Individual__dlm";
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(),
            "nextBatchId", "batch-123"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.queryAnsiSqlV2(sql, null, dataspace);

        // Then
        assertThat(result).contains("nextBatchId");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/queryv2");
    }

    @Test
    void testQueryAnsiSqlV2_withNextBatchId() {
        // Given
        String nextBatchId = "batch-123";
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.queryAnsiSqlV2(null, nextBatchId, dataspace);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/queryv2/batch-123");
    }

    @Test
    void testQueryAnsiSqlV2_requiresSqlOrNextBatchId() {
        String result = queryTools.queryAnsiSqlV2(null, null, DEFAULT_DATASPACE);

        assertThat(result).contains("error", "sql", "nextBatchId");
        verifyNoInteractions(client);
    }

    @Test
    void testQueryProfile_basicQuery() {
        // Given
        String dataModelName = "Individual";
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.queryProfile(dataModelName, null, null, null, null, null, null, null, null, null, dataspace);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/profile/Individual");
    }

    @Test
    void testQueryProfile_withId() {
        // Given
        String dataModelName = "Individual";
        String id = "0014x000001234";

        Map<String, Object> mockResponse = Map.of(
            "data", Map.of("id", id)
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.queryProfile(dataModelName, id, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(result).contains(id);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/profile/Individual/0014x000001234");
    }

    @Test
    void testGetProfileMetadata_noModelName() {
        // Given
        String dataspace = DEFAULT_DATASPACE;

        Map<String, Object> mockResponse = Map.of(
            "profiles", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.getProfileMetadata(null, dataspace);

        // Then
        assertThat(result).contains("profiles");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/profile/metadata", "dataspace=default");
    }

    @Test
    void testGetProfileMetadata_withModelName() {
        // Given
        String dataModelName = "Individual";

        Map<String, Object> mockResponse = Map.of(
            "name", "Individual"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = queryTools.getProfileMetadata(dataModelName, null);

        // Then
        assertThat(result).contains("Individual");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/profile/metadata/Individual");
    }

    // ============================================================
    // Data Graph Tools Tests
    // ============================================================

    @Test
    void testQueryDataGraph_success() {
        Map<String, Object> mockResponse = Map.of("data", Map.of("id", "rec-1"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = queryTools.queryDataGraph("MyGraph", "rec-1", null, null);

        assertThat(result).contains("rec-1");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/data-graphs/data/MyGraph/rec-1");
    }

    @Test
    void testQueryDataGraph_withLive() {
        Map<String, Object> mockResponse = Map.of("data", Map.of("id", "rec-1"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = queryTools.queryDataGraph("MyGraph", "rec-1", null, true);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("live=true");
    }

    @Test
    void testLookupDataGraph_success() {
        Map<String, Object> mockResponse = Map.of("results", java.util.List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = queryTools.lookupDataGraph("MyGraph", "email@test.com", null, null);

        assertThat(result).contains("results");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/data-graphs/data/MyGraph", "lookupKeys=email%40test.com");
    }

    @Test
    void testGetDataGraphMetadata_success() {
        Map<String, Object> mockResponse = Map.of("entities", java.util.List.of("Graph1"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = queryTools.getDataGraphMetadata(null, null);

        assertThat(result).contains("Graph1");
        verify(client).get("/ssot/data-graphs/metadata", Map.class);
    }

    @Test
    void testGetDataGraphMetadata_withEntity() {
        Map<String, Object> mockResponse = Map.of("name", "MyGraph");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = queryTools.getDataGraphMetadata("MyGraph", DEFAULT_DATASPACE);

        assertThat(result).contains("MyGraph");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("dataGraphEntityName=MyGraph", "dataspace=default");
    }
}
