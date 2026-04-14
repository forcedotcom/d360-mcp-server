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

import java.util.List;
import java.util.Map;

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataToolsTest {

    @Mock
    private Data360Client client;

    private MetadataTools metadataTools;

    @BeforeEach
    void setUp() {
        metadataTools = new MetadataTools(client);
    }

    @Test
    void testSearchMetadata_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("apiName", "Individual__dlm", "label", "Individual", "metadataType", "DataModelObject")
            ),
            "totalRecords", 1
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.searchMetadata("Individual", null, null, null, null);

        // Then
        assertThat(result).contains("Individual__dlm");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/connect/search/metadata/results");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("query")).isEqualTo("Individual");
        assertThat(((Map) body.get("pagination")).get("limit")).isEqualTo(10);
    }

    @Test
    void testSearchMetadata_withFilters() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of(), "totalRecords", 0);
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.searchMetadata(
            "Account",
            20,
            10,
            "[\"DataModelObject\"]",
            "[\"crm\"]"
        );

        // Then
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("query")).isEqualTo("Account");
        assertThat(((Map) body.get("pagination")).get("limit")).isEqualTo(20);
        assertThat(((Map) body.get("pagination")).get("offset")).isEqualTo(10);

        List<Map<String, Object>> filters = (List<Map<String, Object>>) body.get("filters");
        assertThat(filters).hasSize(2);
        assertThat(filters.get(0).get("field")).isEqualTo("metadataType");
        assertThat(filters.get(1).get("field")).isEqualTo("tags");
    }

    @Test
    void testSearchMetadata_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/connect/search/metadata/results"));

        // When
        String result = metadataTools.searchMetadata("test", null, null, null, null);

        // Then
        assertThat(result).contains("error", "500", "Internal Server Error");
    }

    @Test
    void testGetMetadata_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "entityName", "Individual__dlm",
            "fields", List.of(
                Map.of("name", "ssot__Id__c", "type", "string")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadata("Individual__dlm", null, null, null);

        // Then
        assertThat(result).contains("Individual__dlm", "ssot__Id__c");
        verify(client).get("/ssot/metadata?entityName=Individual__dlm", Map.class);
    }

    @Test
    void testGetMetadata_withAllParams() {
        // Given
        Map<String, Object> mockResponse = Map.of("entityName", "Account__dlm");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadata(
            "Account__dlm",
            "DataModelObject",
            "Profile",
            DEFAULT_DATASPACE
        );

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String path = pathCaptor.getValue();
        assertThat(path).startsWith("/ssot/metadata?");
        assertThat(path).contains("entityName=Account__dlm");
        assertThat(path).contains("entityType=DataModelObject");
        assertThat(path).contains("entityCategory=Profile");
        assertThat(path).contains("dataspace=default");
    }

    @Test
    void testGetMetadata_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not Found", "/ssot/metadata"));

        // When
        String result = metadataTools.getMetadata("NonExistent__dlm", null, null, null);

        // Then
        assertThat(result).contains("error", "404", "Not Found");
    }

    @Test
    void testGetMetadataEntities_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("entityName", "Individual__dlm", "entityType", "DataModelObject")
            ),
            "nextBatchId", "batch123"
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadataEntities("DataModelObject", null, null, null);

        // Then
        assertThat(result).contains("Individual__dlm", "batch123");
        verify(client).get("/ssot/metadata-entities?entityType=DataModelObject", Map.class);
    }

    @Test
    void testGetMetadataEntities_withNextBatchId() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadataEntities(null, null, null, "batch123");

        // Then
        verify(client).get("/ssot/metadata-entities/batch123", Map.class);
    }

    @Test
    void testGetMetadataEntities_withAllParams() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadataEntities(
            "DataModelObject",
            "Profile",
            DEFAULT_DATASPACE,
            null
        );

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        String path = pathCaptor.getValue();
        assertThat(path).startsWith("/ssot/metadata-entities?");
        assertThat(path).contains("entityType=DataModelObject");
        assertThat(path).contains("entityCategory=Profile");
        assertThat(path).contains("dataspace=default");
    }

    @Test
    void testGetMetadataEntities_withDataspaceAndNextBatchId() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = metadataTools.getMetadataEntities(null, null, DEFAULT_DATASPACE, "batch123");

        // Then
        verify(client).get("/ssot/metadata-entities/batch123?dataspace=default", Map.class);
    }

    @Test
    void testGetMetadataEntities_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad Request", "/ssot/metadata-entities"));

        // When
        String result = metadataTools.getMetadataEntities("InvalidType", null, null, null);

        // Then
        assertThat(result).contains("error", "400", "Bad Request");
    }

    // ============================================================
    // Connection Tools Tests
    // ============================================================

    @Test
    void testListConnections_success() {
        Map<String, Object> mockResponse = Map.of("data", List.of(Map.of("id", "conn-1")));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.listConnections("SALESFORCE", null);

        assertThat(result).contains("conn-1");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("connectorType=SALESFORCE");
    }

    @Test
    void testGetConnection_success() {
        Map<String, Object> mockResponse = Map.of("id", "conn-1", "name", "My Connection");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.getConnection("conn-1", "SALESFORCE", null);

        assertThat(result).contains("My Connection");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/connections/conn-1");
        assertThat(pathCaptor.getValue()).contains("connectorType=SALESFORCE");
    }

    @Test
    void testCreateConnection_success() {
        Map<String, Object> mockResponse = Map.of("id", "conn-new");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.createConnection("{\"name\":\"New Conn\"}", "SALESFORCE", null);

        assertThat(result).contains("conn-new");
        verify(client).post(anyString(), any(), eq(Map.class));
    }

    @Test
    void testDeleteConnection_success() {
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.deleteConnection("conn-1", "SALESFORCE", null);

        assertThat(result).contains("success");
        verify(client).delete(anyString(), eq(Map.class));
    }

    @Test
    void testTestConnection_success() {
        Map<String, Object> mockResponse = Map.of("status", "connected");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.testConnection("{\"credentials\":{}}", "SALESFORCE", null);

        assertThat(result).contains("connected");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/connections/actions/test");
    }

    @Test
    void testListConnectionEndpoints_success() {
        Map<String, Object> mockResponse = Map.of("endpoints", List.of("ep-1"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.listConnectionEndpoints(null);

        assertThat(result).contains("ep-1");
        verify(client).get("/ssot/connection-endpoints", Map.class);
    }

    @Test
    void testListConnectors_success() {
        Map<String, Object> mockResponse = Map.of("connectors", List.of("SALESFORCE", "REST_API"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.listConnectors(null);

        assertThat(result).contains("SALESFORCE");
        verify(client).get("/ssot/connectors", Map.class);
    }

    @Test
    void testGetConnectorMetadata_success() {
        Map<String, Object> mockResponse = Map.of("type", "SALESFORCE", "fields", List.of("clientId"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.getConnectorMetadata("SALESFORCE", null);

        assertThat(result).contains("SALESFORCE", "clientId");
        verify(client).get("/ssot/connectors/SALESFORCE", Map.class);
    }

    @Test
    void testConnection_error() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Error", "/ssot/connections"));

        String result = metadataTools.listConnections("SALESFORCE", null);

        assertThat(result).contains("error", "500");
    }
}
