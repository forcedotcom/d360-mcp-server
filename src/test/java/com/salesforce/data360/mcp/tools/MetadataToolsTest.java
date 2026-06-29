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
import com.salesforce.data360.mcp.model.request.metadata.ConnectionCreateRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionDbSchemaCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionFieldCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionObjectCollectionRequest;
import com.salesforce.data360.mcp.model.request.metadata.ConnectionTestRequest;
import com.salesforce.data360.mcp.model.request.metadata.DataConnectionParameterInput;
import com.salesforce.data360.mcp.model.request.metadata.PrismMetadataSearchFilterInputRepresentation;
import com.salesforce.data360.mcp.model.request.metadata.PrismMetadataSearchInputRepresentation;
import com.salesforce.data360.mcp.model.request.metadata.PrismMetadataSearchPaginationInputRepresentation;
import com.salesforce.data360.mcp.model.request.metadata.ResourceFilterByPropertyInput;
import com.salesforce.data360.mcp.model.request.metadata.ResourceFiltersInput;
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

        PrismMetadataSearchInputRepresentation request = new PrismMetadataSearchInputRepresentation();
        request.setQuery("Individual");

        // When
        String result = metadataTools.searchMetadata(request);

        // Then
        assertThat(result).contains("Individual__dlm");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/connect/search/metadata/results");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("query")).isEqualTo("Individual");
        assertThat(body).doesNotContainKey("pagination");
        assertThat(body).doesNotContainKey("filters");
    }

    @Test
    void testSearchMetadata_withFilters() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of(), "totalRecords", 0);
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        PrismMetadataSearchPaginationInputRepresentation pagination =
            new PrismMetadataSearchPaginationInputRepresentation();
        pagination.setLimit(20);

        PrismMetadataSearchFilterInputRepresentation typeFilter = new PrismMetadataSearchFilterInputRepresentation();
        typeFilter.setField("metadataType");
        typeFilter.setValues(List.of("DataModelObject"));

        PrismMetadataSearchFilterInputRepresentation tagsFilter = new PrismMetadataSearchFilterInputRepresentation();
        tagsFilter.setField("tags");
        tagsFilter.setValues(List.of("crm"));

        PrismMetadataSearchInputRepresentation request = new PrismMetadataSearchInputRepresentation();
        request.setQuery("Account");
        request.setPagination(pagination);
        request.setFilters(List.of(typeFilter, tagsFilter));

        // When
        metadataTools.searchMetadata(request);

        // Then
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("query")).isEqualTo("Account");
        assertThat(((Map) body.get("pagination")).get("limit")).isEqualTo(20);

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

        PrismMetadataSearchInputRepresentation request = new PrismMetadataSearchInputRepresentation();
        request.setQuery("test");

        // When
        String result = metadataTools.searchMetadata(request);

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

        String result = metadataTools.listConnections("SALESFORCE", null, null, null, null, null, null, null);

        assertThat(result).contains("conn-1");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("connectorType=SALESFORCE");
    }

    @Test
    void testGetConnection_success() {
        Map<String, Object> mockResponse = Map.of("id", "conn-1", "name", "My Connection");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.getConnection("conn-1", null);

        assertThat(result).contains("My Connection");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/connections/conn-1");
    }

    @Test
    void testCreateConnection_success() {
        Map<String, Object> mockResponse = Map.of("id", "conn-new");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        ConnectionCreateRequest request = new ConnectionCreateRequest();
        request.setName("New Conn");
        request.setConnectorType("SALESFORCE");
        String result = metadataTools.createConnection(request);

        assertThat(result).contains("conn-new");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/connections");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("connectorType", "SALESFORCE");
        assertThat(body).containsEntry("name", "New Conn");
    }

    @Test
    void testDeleteConnection_success() {
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.deleteConnection("conn-1");

        assertThat(result).contains("success");
        verify(client).delete(anyString(), eq(Map.class));

    }

    @Test
    void testTestConnection_success() {
        Map<String, Object> mockResponse = Map.of("status", "connected");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataConnectionParameterInput cred = new DataConnectionParameterInput();
        cred.setParamName("clientId");
        cred.setValue("abc");
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setConnectorType("SALESFORCE");
        request.setCredentials(List.of(cred));
        String result = metadataTools.testConnection(request);

        assertThat(result).contains("connected");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/connections/actions/test");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("connectorType", "SALESFORCE");
        List<Map<String, Object>> creds = (List<Map<String, Object>>) body.get("credentials");
        assertThat(creds).hasSize(1);
        assertThat(creds.get(0)).containsEntry("paramName", "clientId").containsEntry("value", "abc");
    }

    @Test
    void testListConnectors_success() {
        Map<String, Object> mockResponse = Map.of("connectors", List.of("SALESFORCE", "REST_API"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.listConnectors(null, null, null);

        assertThat(result).contains("SALESFORCE");
        verify(client).get("/ssot/connectors", Map.class);
    }

    @Test
    void testGetConnectorMetadata_success() {
        Map<String, Object> mockResponse = Map.of("type", "SALESFORCE", "fields", List.of("clientId"));
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        String result = metadataTools.getConnectorMetadata("SALESFORCE");

        assertThat(result).contains("SALESFORCE", "clientId");
        verify(client).get("/ssot/connectors/SALESFORCE", Map.class);
    }

    @Test
    void testConnection_error() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Error", "/ssot/connections"));

        String result = metadataTools.listConnections("SALESFORCE", null, null, null, null, null, null, null);

        assertThat(result).contains("error", "500");
    }

    // ============================================================
    // Connection Schema Discovery Tests
    // ============================================================

    @Test
    void testListConnectionDbSchemas_buildsCorrectPathAndBody() {
        Map<String, Object> mockResponse = Map.of("databaseSchemas", List.of("PUBLIC", "ANALYTICS"));
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        ConnectionDbSchemaCollectionRequest request = new ConnectionDbSchemaCollectionRequest();
        request.setAdvancedAttributes(Map.of("database", "MY_DB"));
        String result = metadataTools.listConnectionDbSchemas("CONN_ID", request);

        assertThat(result).contains("PUBLIC", "ANALYTICS");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/connections/CONN_ID/database-schemas");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsOnlyKeys("advancedAttributes");
        assertThat((Map) body.get("advancedAttributes")).containsEntry("database", "MY_DB");
    }

    @Test
    void testListConnectionObjects_omitsUnsetKeys() {
        Map<String, Object> mockResponse = Map.of(
            "objects", List.of(Map.of("name", "ORDERS", "label", "Orders"))
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        ConnectionObjectCollectionRequest request = new ConnectionObjectCollectionRequest();
        request.setAdvancedAttributes(Map.of("database", "MY_DB", "schema", "PUBLIC"));
        String result = metadataTools.listConnectionObjects("CONN_ID", request);

        assertThat(result).contains("ORDERS");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/connections/CONN_ID/objects");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsOnlyKeys("advancedAttributes");
        assertThat((Map) body.get("advancedAttributes")).containsEntry("schema", "PUBLIC");
    }

    @Test
    void testListConnectionObjects_withFilters() {
        Map<String, Object> mockResponse = Map.of("objects", List.of());
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        ResourceFilterByPropertyInput byProp = new ResourceFilterByPropertyInput();
        byProp.setFilterOperator("EQUALS");
        byProp.setValues(List.of("ORD"));
        ResourceFiltersInput filters = new ResourceFiltersInput();
        filters.setFiltersByProperty(List.of(byProp));
        ConnectionObjectCollectionRequest request = new ConnectionObjectCollectionRequest();
        request.setAdvancedAttributes(Map.of("database", "DB"));
        request.setFilters(filters);

        metadataTools.listConnectionObjects("CONN_ID", request);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsKeys("advancedAttributes", "filters");
        Map<String, Object> filtersMap = (Map<String, Object>) body.get("filters");
        List<Map<String, Object>> byPropList = (List<Map<String, Object>>) filtersMap.get("filtersByProperty");
        assertThat(byPropList).hasSize(1);
        assertThat(byPropList.get(0)).containsEntry("filterOperator", "EQUALS");
    }

    @Test
    void testDescribeConnectionObjectFields_encodesResourceName() {
        Map<String, Object> mockResponse = Map.of(
            "fields", List.of(Map.of("name", "ID", "type", "Number")),
            "primaryKeys", List.of("ID")
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        ResourceFiltersInput filters = new ResourceFiltersInput();
        filters.setFiltersByProperty(List.of());
        ConnectionFieldCollectionRequest request = new ConnectionFieldCollectionRequest();
        request.setAdvancedAttributes(Map.of("database", "DB", "schema", "PUBLIC"));
        request.setFilters(filters);

        String result = metadataTools.describeConnectionObjectFields("CONN_ID", "MY.TABLE", request);

        assertThat(result).contains("primaryKeys", "ID");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/connections/CONN_ID/objects/MY.TABLE/fields");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsKeys("advancedAttributes", "filters");
    }

    @Test
    void testDescribeConnectionObjectFields_minimalParams() {
        Map<String, Object> mockResponse = Map.of("fields", List.of());
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        metadataTools.describeConnectionObjectFields(
            "CONN_ID", "ORDERS", new ConnectionFieldCollectionRequest());

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).isEmpty();
    }
}
