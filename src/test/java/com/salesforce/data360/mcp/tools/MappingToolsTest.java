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
import com.salesforce.data360.mcp.model.request.mapping.FieldMappingAddRequest;
import com.salesforce.data360.mcp.model.request.mapping.FieldMappingInput;
import com.salesforce.data360.mcp.model.request.mapping.MappingCreateRequest;
import com.salesforce.data360.mcp.model.request.mapping.MappingUpdateRequest;
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
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MappingToolsTest {

    @Mock
    private Data360Client client;

    private MappingTools mappingTools;

    @BeforeEach
    void setUp() {
        mappingTools = new MappingTools(client);
    }

    // ── listMappingsBySource tests ──────────────────────────────────────

    @Test
    void testListMappingsBySource_byDmo() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "objectSourceTargetMaps", List.of(
                Map.of(
                    "developerName", "Contact_map_ContactPointAddress_123",
                    "sourceEntityDeveloperName", "Contact_00D000000000000__dll",
                    "targetEntityDeveloperName", "ssot__ContactPointAddress__dlm"
                )
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.listMappingsBySource("ssot__ContactPointAddress__dlm", null, null, null);

        // Then
        assertThat(result).contains("Contact_map_ContactPointAddress_123");
        verify(client).get("/ssot/data-model-object-mappings?dmoDeveloperName=ssot__ContactPointAddress__dlm", Map.class);
    }

    @Test
    void testListMappingsBySource_bySourceObjectName() {
        // Given
        Map<String, Object> mockResponse = Map.of("objectSourceTargetMaps", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.listMappingsBySource(null, null, "Contact", null);

        // Then
        verify(client).get("/ssot/data-model-object-mappings?sourceObjectName=Contact", Map.class);
    }

    @Test
    void testListMappingsBySource_withAllParams() {
        // Given
        Map<String, Object> mockResponse = Map.of("objectSourceTargetMaps", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.listMappingsBySource("ssot__Individual__dlm", "Contact_00D000000000000__dll", null, DEFAULT_DATASPACE);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("dmoDeveloperName=ssot__Individual__dlm",
                "dloDeveloperName=Contact_00D000000000000__dll", "dataspace=default");
    }

    @Test
    void testListMappingsBySource_missingRequiredParams() {
        // When — neither dmoDeveloperName nor sourceObjectName provided
        String result = mappingTools.listMappingsBySource(null, null, null, null);

        // Then — client-side validation returns error without calling API
        assertThat(result).contains("error", "Either dmoDeveloperName or sourceObjectName is required");
        verify(client, never()).get(anyString(), eq(Map.class));
    }

    // ── getDataModelObjectMapping tests ─────────────────────────────────

    @Test
    void testGetDataModelObjectMapping_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "AccountMapping",
            "sourceObjectName", "Account_00D000000000000__dll",
            "targetObjectName", "ssot__Account__dlm",
            "fieldMappings", List.of(
                Map.of("sourceFieldName", "Id", "targetFieldName", "ssot__Id__c"),
                Map.of("sourceFieldName", "Name", "targetFieldName", "ssot__Name__c")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.getDataModelObjectMapping("AccountMapping", null);

        // Then
        assertThat(result).contains("AccountMapping", "fieldMappings", "sourceFieldName", "targetFieldName");
        verify(client).get("/ssot/data-model-object-mappings/AccountMapping", Map.class);
    }

    @Test
    void testGetDataModelObjectMapping_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "AccountMapping");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.getDataModelObjectMapping("AccountMapping", DEFAULT_DATASPACE);

        // Then
        verify(client).get("/ssot/data-model-object-mappings/AccountMapping?dataspace=default", Map.class);
    }

    @Test
    void testGetDataModelObjectMapping_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Mapping not found", "/ssot/data-model-object-mappings/NonExistent"));

        // When
        String result = mappingTools.getDataModelObjectMapping("NonExistent", null);

        // Then
        assertThat(result).contains("error", "404", "not found");
    }

    @Test
    void testCreateDataModelObjectMapping_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "NewMapping",
            "sourceObjectName", "Source__dll",
            "targetObjectName", "Target__dlm"
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        FieldMappingInput fieldMapping = new FieldMappingInput();
        fieldMapping.setSourceFieldDeveloperName("Id");
        fieldMapping.setTargetFieldDeveloperName("ssot__Id__c");

        MappingCreateRequest request = new MappingCreateRequest();
        request.setSourceEntityDeveloperName("Source__dll");
        request.setTargetEntityDeveloperName("Target__dlm");
        request.setFieldMapping(List.of(fieldMapping));

        // When
        String result = mappingTools.createDataModelObjectMapping(request, null);

        // Then
        assertThat(result).contains("NewMapping");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-object-mappings");
        assertThat(bodyCaptor.getValue()).containsEntry("sourceEntityDeveloperName", "Source__dll");
        assertThat(bodyCaptor.getValue()).containsEntry("targetEntityDeveloperName", "Target__dlm");
    }

    @Test
    void testCreateDataModelObjectMapping_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "NewMapping");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        MappingCreateRequest request = new MappingCreateRequest();
        request.setSourceEntityDeveloperName("Source__dll");
        request.setTargetEntityDeveloperName("Target__dlm");
        request.setFieldMapping(List.of());

        // When
        String result = mappingTools.createDataModelObjectMapping(request, DEFAULT_DATASPACE);

        // Then
        verify(client).post(eq("/ssot/data-model-object-mappings?dataspace=default"), any(), eq(Map.class));
    }

    @Test
    void testCreateDataModelObjectMapping_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid mapping definition", "/ssot/data-model-object-mappings"));

        MappingCreateRequest request = new MappingCreateRequest();
        request.setSourceEntityDeveloperName("Source__dll");
        request.setTargetEntityDeveloperName("Target__dlm");
        request.setFieldMapping(List.of());

        // When
        String result = mappingTools.createDataModelObjectMapping(request, null);

        // Then
        assertThat(result).contains("error", "400", "Invalid mapping definition");
    }

    @Test
    void testUpdateDataModelObjectMapping_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "AccountMapping");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        FieldMappingInput fieldMapping = new FieldMappingInput();
        fieldMapping.setSourceFieldDeveloperName("Email");
        fieldMapping.setTargetFieldDeveloperName("ssot__Email__c");

        MappingUpdateRequest request = new MappingUpdateRequest();
        request.setFieldMapping(List.of(fieldMapping));

        // When
        String result = mappingTools.updateDataModelObjectMapping("AccountMapping", request, null);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-object-mappings/AccountMapping");
        assertThat(bodyCaptor.getValue()).containsKey("fieldMapping");
    }

    @Test
    void testUpdateDataModelObjectMapping_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "AccountMapping");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        MappingUpdateRequest request = new MappingUpdateRequest();
        request.setSourceEntityDeveloperName("NewSource__dll");

        // When
        String result = mappingTools.updateDataModelObjectMapping("AccountMapping", request, DEFAULT_DATASPACE);

        // Then
        verify(client).patch(eq("/ssot/data-model-object-mappings/AccountMapping?dataspace=default"), any(), eq(Map.class));
    }

    @Test
    void testUpdateDataModelObjectMapping_error() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Mapping not found", "/ssot/data-model-object-mappings/NonExistent"));

        MappingUpdateRequest request = new MappingUpdateRequest();
        request.setFieldMapping(List.of());

        // When
        String result = mappingTools.updateDataModelObjectMapping("NonExistent", request, null);

        // Then
        assertThat(result).contains("error", "404");
    }

    @Test
    void testDeleteDataModelObjectMapping_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.deleteDataModelObjectMapping("AccountMapping", null);

        // Then
        assertThat(result).contains("success");
        verify(client).delete("/ssot/data-model-object-mappings/AccountMapping", Map.class);
    }

    @Test
    void testDeleteDataModelObjectMapping_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.deleteDataModelObjectMapping("AccountMapping", DEFAULT_DATASPACE);

        // Then
        verify(client).delete("/ssot/data-model-object-mappings/AccountMapping?dataspace=default", Map.class);
    }

    @Test
    void testDeleteDataModelObjectMapping_error() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(409, "Mapping is in use", "/ssot/data-model-object-mappings/AccountMapping"));

        // When
        String result = mappingTools.deleteDataModelObjectMapping("AccountMapping", null);

        // Then
        assertThat(result).contains("error", "409", "in use");
    }

    // ── addFieldMappings tests ──────────────────────────────────────────

    @Test
    void testAddFieldMappings_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "developerName", "Contact_map_CPA_123",
            "fieldMappings", List.of(
                Map.of("sourceFieldDeveloperName", "MailingCity__c", "targetFieldDeveloperName", "ssot__CityId__c")
            )
        );
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        FieldMappingInput fieldMapping = new FieldMappingInput();
        fieldMapping.setSourceFieldDeveloperName("MailingCity__c");
        fieldMapping.setTargetFieldDeveloperName("ssot__CityId__c");

        FieldMappingAddRequest request = new FieldMappingAddRequest();
        request.setFieldMappings(List.of(fieldMapping));

        // When
        String result = mappingTools.addFieldMappings("Contact_map_CPA_123", request, null);

        // Then
        assertThat(result).contains("Contact_map_CPA_123", "MailingCity__c");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).patch(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-object-mappings/Contact_map_CPA_123/field-mappings");
    }

    @Test
    void testAddFieldMappings_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("developerName", "Contact_map_CPA_123");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        FieldMappingAddRequest request = new FieldMappingAddRequest();
        request.setFieldMappings(List.of());

        // When
        String result = mappingTools.addFieldMappings("Contact_map_CPA_123", request, DEFAULT_DATASPACE);

        // Then
        verify(client).patch(eq("/ssot/data-model-object-mappings/Contact_map_CPA_123/field-mappings?dataspace=default"), any(), eq(Map.class));
    }

    @Test
    void testAddFieldMappings_error() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid field mapping", "/ssot/data-model-object-mappings/X/field-mappings"));

        FieldMappingAddRequest request = new FieldMappingAddRequest();
        request.setFieldMappings(List.of());

        // When
        String result = mappingTools.addFieldMappings("X", request, null);

        // Then
        assertThat(result).contains("error", "400");
    }

    // ── deleteFieldMapping tests ────────────────────────────────────────

    @Test
    void testDeleteFieldMapping_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.deleteFieldMapping("Contact_map_CPA_123", "MailingCity__c_fieldmap_ssot__CityId__c", null);

        // Then
        assertThat(result).contains("success");
        verify(client).delete("/ssot/data-model-object-mappings/Contact_map_CPA_123/field-mappings/MailingCity__c_fieldmap_ssot__CityId__c", Map.class);
    }

    @Test
    void testDeleteFieldMapping_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = mappingTools.deleteFieldMapping("Contact_map_CPA_123", "MailingCity__c_fieldmap_ssot__CityId__c", DEFAULT_DATASPACE);

        // Then
        verify(client).delete(
            "/ssot/data-model-object-mappings/Contact_map_CPA_123/field-mappings/MailingCity__c_fieldmap_ssot__CityId__c?dataspace=default",
            Map.class);
    }

    @Test
    void testDeleteFieldMapping_error() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Field mapping not found", "/ssot/data-model-object-mappings/X/field-mappings/Y"));

        // When
        String result = mappingTools.deleteFieldMapping("X", "Y", null);

        // Then
        assertThat(result).contains("error", "404", "not found");
    }
}
