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
import com.salesforce.data360.mcp.model.request.sdm.*;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SdmTools - Semantic Data Model operations.
 */
@ExtendWith(MockitoExtension.class)
class SdmToolsTest {

    @Mock
    private Data360Client client;

    @InjectMocks
    private SdmTools sdmTools;

    private Map<String, Object> mockModel;
    private Map<String, Object> mockDataObject;
    private Map<String, Object> mockRelationship;

    @BeforeEach
    void setUp() {
        mockModel = Map.of(
            "id", "model-123",
            "apiName", "TestModel",
            "label", "Test Model",
            "dataspace", "default"
        );

        mockDataObject = Map.of(
            "id", "do-456",
            "dataObjectName", "Account__dlm",
            "label", "Account",
            "dataObjectType", "Dmo"
        );

        mockRelationship = Map.of(
            "id", "rel-789",
            "label", "Account to Opportunity",
            "leftSemanticDefinitionApiName", "Account",
            "rightSemanticDefinitionApiName", "Opportunity"
        );
    }

    // ============================================================
    // Model CRUD Tests
    // ============================================================

    @Test
    void testListSemanticModels() {
        Map<String, Object> response = Map.of(
            "items", List.of(mockModel),
            "totalCount", 1
        );
        when(client.get(eq("/ssot/semantic/models?dataspace=default"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.listSemanticModels(DEFAULT_DATASPACE, null, null, null, null, null, null, null, null, null, null);

        assertThat(result).isNotNull();
        Map parsed = JsonUtil.fromJson(result, Map.class);
        assertThat(parsed.get("totalCount")).isEqualTo(1);
        verify(client).get(eq("/ssot/semantic/models?dataspace=default"), eq(Map.class));
    }

    @Test
    void testGetSemanticModel() {
        when(client.get(eq("/ssot/semantic/models/TestModel"), eq(Map.class)))
            .thenReturn(mockModel);

        String result = sdmTools.getSemanticModel("TestModel", null, null, null, null, null, null);

        assertThat(result).contains("TestModel");
        verify(client).get(eq("/ssot/semantic/models/TestModel"), eq(Map.class));
    }

    @Test
    void testCreateSemanticModel() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("apiName", "TestModel");
        expectedBody.put("label", "Test Model");
        expectedBody.put("dataspace", DEFAULT_DATASPACE);
        when(client.post(eq("/ssot/semantic/models"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockModel);

        SdmModelCreateRequest request = new SdmModelCreateRequest();
        request.setApiName("TestModel");
        request.setLabel("Test Model");
        request.setDataspace(DEFAULT_DATASPACE);
        String result = sdmTools.createSemanticModel(request, null, null, null);

        assertThat(result).contains("TestModel");
        verify(client).post(eq("/ssot/semantic/models"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testUpdateSemanticModel() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("label", "Updated Model");
        when(client.patch(eq("/ssot/semantic/models/TestModel"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockModel);

        SdmModelUpdateRequest request = new SdmModelUpdateRequest();
        request.setLabel("Updated Model");
        String result = sdmTools.updateSemanticModel("TestModel", request, null);

        assertThat(result).contains("TestModel");
        verify(client).patch(eq("/ssot/semantic/models/TestModel"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testDeleteSemanticModel() {
        Map<String, Object> response = Map.of("success", true);
        when(client.delete(eq("/ssot/semantic/models/TestModel"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.deleteSemanticModel("TestModel");

        assertThat(result).contains("success");
        verify(client).delete(eq("/ssot/semantic/models/TestModel"), eq(Map.class));
    }

    @Test
    void testCloneSemanticModel() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("apiName", "ClonedModel");
        when(client.post(eq("/ssot/semantic/models/TestModel/clone"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockModel);

        SdmModelCloneRequest request = new SdmModelCloneRequest();
        request.setApiName("ClonedModel");
        String result = sdmTools.cloneSemanticModel("TestModel", request);

        assertThat(result).contains("TestModel");
        verify(client).post(eq("/ssot/semantic/models/TestModel/clone"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testValidateSemanticModel() {
        Map<String, Object> response = Map.of("valid", true);
        when(client.get(eq("/ssot/semantic/models/TestModel/validate"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.validateSemanticModel("TestModel");

        assertThat(result).contains("valid");
        verify(client).get(eq("/ssot/semantic/models/TestModel/validate"), eq(Map.class));
    }

    @Test
    void testGetSemanticModelDependencies() {
        Map<String, Object> response = Map.of("dependencies", List.of("CI1", "CI2"));
        when(client.get(eq("/ssot/semantic/models/TestModel/external-dependencies"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.getSemanticModelDependencies("TestModel");

        assertThat(result).contains("dependencies");
        verify(client).get(eq("/ssot/semantic/models/TestModel/external-dependencies"), eq(Map.class));
    }

    // ============================================================
    // Data Object Tests
    // ============================================================

    @Test
    void testCreateDataObject() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("dataObjectName", "Account__dlm");
        expectedBody.put("label", "Account");
        expectedBody.put("dataObjectType", "Dmo");
        expectedBody.put("shouldIncludeAllFields", true);
        when(client.post(eq("/ssot/semantic/models/TestModel/data-objects"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockDataObject);

        SdmDataObjectCreateRequest request = new SdmDataObjectCreateRequest();
        request.setDataObjectName("Account__dlm");
        request.setLabel("Account");
        request.setDataObjectType("Dmo");
        request.setShouldIncludeAllFields(true);
        String result = sdmTools.createDataObject("TestModel", request, null);

        assertThat(result).contains("Account__dlm");
        verify(client).post(eq("/ssot/semantic/models/TestModel/data-objects"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testListDataObjects() {
        Map<String, Object> response = Map.of("items", List.of(mockDataObject));
        when(client.get(eq("/ssot/semantic/models/TestModel/data-objects"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.listDataObjects("TestModel");

        assertThat(result).contains("Account__dlm");
        verify(client).get(eq("/ssot/semantic/models/TestModel/data-objects"), eq(Map.class));
    }

    @Test
    void testGetDataObject() {
        when(client.get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(Map.class)))
            .thenReturn(mockDataObject);

        String result = sdmTools.getDataObject("TestModel", "Account__dlm");

        assertThat(result).contains("Account__dlm");
        verify(client).get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(Map.class));
    }

    @Test
    void testUpdateDataObject() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("label", "Updated Account");
        when(client.put(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockDataObject);

        SdmDataObjectUpdateRequest request = new SdmDataObjectUpdateRequest();
        request.setLabel("Updated Account");
        String result = sdmTools.updateDataObject("TestModel", "Account__dlm", request);

        assertThat(result).contains("Account__dlm");
        verify(client).put(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testDeleteDataObject() {
        Map<String, Object> response = Map.of("success", true);
        when(client.delete(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.deleteDataObject("TestModel", "Account__dlm", null);

        assertThat(result).contains("success");
        verify(client).delete(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm"), eq(Map.class));
    }

    // ============================================================
    // Dimension & Measurement Tests
    // ============================================================

    @Test
    void testListDimensions() {
        Map<String, Object> response = Map.of("items", List.of(Map.of("name", "AccountName")));
        when(client.get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm/dimensions"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.listDimensions("TestModel", "Account__dlm", null);

        assertThat(result).contains("AccountName");
        verify(client).get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm/dimensions"), eq(Map.class));
    }

    @Test
    void testListMeasurements() {
        Map<String, Object> response = Map.of("items", List.of(Map.of("name", "Revenue")));
        when(client.get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm/measurements"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.listMeasurements("TestModel", "Account__dlm", null);

        assertThat(result).contains("Revenue");
        verify(client).get(eq("/ssot/semantic/models/TestModel/data-objects/Account__dlm/measurements"), eq(Map.class));
    }

    // ============================================================
    // Relationship Tests
    // ============================================================

    @Test
    void testCreateRelationship() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("label", "Account to Opportunity");
        expectedBody.put("leftSemanticDefinitionApiName", "Account");
        expectedBody.put("rightSemanticDefinitionApiName", "Opportunity");
        expectedBody.put("cardinality", "OneToMany");
        when(client.post(eq("/ssot/semantic/models/TestModel/relationships"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockRelationship);

        SdmRelationshipCreateRequest request = new SdmRelationshipCreateRequest();
        request.setLabel("Account to Opportunity");
        request.setLeftSemanticDefinitionApiName("Account");
        request.setRightSemanticDefinitionApiName("Opportunity");
        request.setCardinality("OneToMany");
        String result = sdmTools.createRelationship("TestModel", request);

        assertThat(result).contains("Account to Opportunity");
        verify(client).post(eq("/ssot/semantic/models/TestModel/relationships"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testListRelationships() {
        Map<String, Object> response = Map.of("items", List.of(mockRelationship));
        when(client.get(eq("/ssot/semantic/models/TestModel/relationships"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.listRelationships("TestModel");

        assertThat(result).contains("Account to Opportunity");
        verify(client).get(eq("/ssot/semantic/models/TestModel/relationships"), eq(Map.class));
    }

    @Test
    void testGetRelationship() {
        when(client.get(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(Map.class)))
            .thenReturn(mockRelationship);

        String result = sdmTools.getRelationship("TestModel", "rel-789");

        assertThat(result).contains("Account to Opportunity");
        verify(client).get(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(Map.class));
    }

    @Test
    void testUpdateRelationship() {
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("cardinality", "ManyToOne");
        when(client.put(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(expectedBody), eq(Map.class)))
            .thenReturn(mockRelationship);

        SdmRelationshipUpdateRequest request = new SdmRelationshipUpdateRequest();
        request.setCardinality("ManyToOne");
        String result = sdmTools.updateRelationship("TestModel", "rel-789", request);

        assertThat(result).contains("Account to Opportunity");
        verify(client).put(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(expectedBody), eq(Map.class));
    }

    @Test
    void testDeleteRelationship() {
        Map<String, Object> response = Map.of("success", true);
        when(client.delete(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(Map.class)))
            .thenReturn(response);

        String result = sdmTools.deleteRelationship("TestModel", "rel-789");

        assertThat(result).contains("success");
        verify(client).delete(eq("/ssot/semantic/models/TestModel/relationships/rel-789"), eq(Map.class));
    }

    // ============================================================
    // Query Tests
    // ============================================================

    @Test
    void testExecuteSemanticQuery() {
        Map<String, Object> queryObj = Map.of(
            "fields", List.of(Map.of("expression", Map.of("tableField", Map.of("name", "Revenue")))),
            "options", Map.of("limitOptions", Map.of("limit", 10))
        );
        Map<String, Object> expectedBody = new HashMap<>();
        expectedBody.put("semanticModelId", "model-123");
        expectedBody.put("structuredSemanticQuery", queryObj);
        Map<String, Object> response = Map.of("rows", List.of(Map.of("Revenue", 1000)));
        when(client.post(eq("/semantic-engine/gateway"), eq(expectedBody), eq(Map.class)))
            .thenReturn(response);

        SdmSemanticQueryRequest request = new SdmSemanticQueryRequest();
        request.setSemanticModelId("model-123");
        request.setStructuredSemanticQuery(JsonUtil.toJson(queryObj));
        String result = sdmTools.executeSemanticQuery(request);

        assertThat(result).contains("rows");
        verify(client).post(eq("/semantic-engine/gateway"), eq(expectedBody), eq(Map.class));
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    void testErrorHandling() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/semantic/models/missing"));

        String result = sdmTools.getSemanticModel("missing", null, null, null, null, null, null);

        assertThat(result).contains("error", "Not found", "404");
    }
}
