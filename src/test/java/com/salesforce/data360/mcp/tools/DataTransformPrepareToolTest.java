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
import com.salesforce.data360.mcp.model.request.datatransform.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataTransformPrepareToolTest {

    @Mock
    private Data360Client client;

    private DataTransformTools dataTransformTools;

    @BeforeEach
    void setUp() {
        dataTransformTools = new DataTransformTools(client);
    }

    @Test
    void testPrepareDataTransform_DCSQLBatch_Success() {
        // Given
        String dataspace = DEFAULT_DATASPACE;

        // Mock validation response with outputDataObjects
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();

        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "dcsql_output__dll");
        dlo.put("label", "DCSQL Output");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "Id__c");
        field1.put("label", "ID");
        field1.put("type", "Text");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "Name__c");
        field2.put("label", "Name");
        field2.put("type", "Text");
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("sampleDBTExample", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(validationResponse);

        // Prepare request
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        definition.setVersion("1.0");
        Map<String, Object> manifest = new HashMap<>();
        definition.setManifest(manifest);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setLabel("DBT Example DT");
        request.setName("sampleDBTExample");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, dataspace);

        // Then
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("preparedPayload");
        assertThat(result).contains("suggestions");
        assertThat(result).contains("Id__c");
        assertThat(result).contains("Profile");
        assertThat(result).contains("isPrimaryKey");

        // Verify validation endpoint was called
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms-validation");
    }

    @Test
    void testPrepareDataTransform_ValidationFails() {
        // Given
        Map<String, Object> validationResponse = new HashMap<>();
        List<Map<String, Object>> issues = new ArrayList<>();
        Map<String, Object> issue = new HashMap<>();
        issue.put("message", "Invalid SQL syntax");
        issues.add(issue);
        validationResponse.put("issues", issues);

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"validationStatus\":\"invalid\"");
        assertThat(result).contains("Invalid SQL syntax");
    }

    @Test
    void testCreateDataTransform_WithCompletePayload_Success() {
        // Given
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "id", "transform-new",
            "name", "sampleDBTExample"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // Create complete request with outputDataObjects
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        definition.setVersion("1.0");

        List<Map<String, Object>> outputDataObjects = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "dcsql_output__dll");
        dlo.put("category", "Profile");
        dlo.put("type", "dataLakeObject");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        field.put("isPrimaryKey", true);
        fields.add(field);
        dlo.put("fields", fields);

        outputDataObjects.add(dlo);
        definition.setOutputDataObjects(outputDataObjects);

        DataTransformCreateRequest request = new DataTransformCreateRequest();
        request.setLabel("DBT Example DT");
        request.setName("sampleDBTExample");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.createDataTransform(request, dataspace);

        // Then
        assertThat(result).contains("transform-new");
        assertThat(result).contains("sampleDBTExample");

        // Verify creation endpoint was called
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms");
    }


    @Test
    void testCreateDataTransform_StreamingTransform_Success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "streaming-123",
            "name", "streaming_transform"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setVersion("63.0");
        definition.setExpression("SELECT Id__c FROM Account_Home__dll");
        definition.setTargetDlo("Account_Home_Target__dll");

        DataTransformCreateRequest request = new DataTransformCreateRequest();
        request.setLabel("Streaming Transform");
        request.setName("streaming_transform");
        request.setType("streaming"); // Streaming type
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.createDataTransform(request, null);

        // Then
        assertThat(result).contains("streaming-123");
        verify(client).post(anyString(), any(), eq(Map.class));
    }

    @Test
    void testPrepareDataTransform_PrimaryKeyDetection_IdField() {
        // Given - Id__c should be detected as primary key
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "Name__c");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "Id__c");  // Should be detected
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"name\":\"Id__c\"");
        assertThat(result).contains("\"isPrimaryKey\":true");
    }

    @Test
    void testPrepareDataTransform_PrimaryKeyDetection_PatternMatch() {
        // Given - account_id__c should be detected as primary key
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "account_id__c");  // Should match *_id__c pattern
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "Name__c");
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"name\":\"account_id__c\"");
        assertThat(result).contains("\"isPrimaryKey\":true");
    }

    @Test
    void testPrepareDataTransform_PrimaryKeyDetection_FirstFieldFallback() {
        // Given - no Id__c or *_id__c pattern, should use first field
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "UniqueKey__c");  // First field should be primary key
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "Name__c");
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"name\":\"UniqueKey__c\"");
        assertThat(result).contains("\"isPrimaryKey\":true");
    }

    @Test
    void testPrepareDataTransform_CategoryAlwaysProfile() {
        // Given - Category is currently hardcoded to "Profile"
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "individual_profile__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"category\":\"Profile\"");
    }

    @Test
    void testPrepareDataTransform_NoOutputDataObjects() {
        // Given - validation response has no outputDataObjects
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());
        // No outputDataObjects field

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("streaming");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"validationStatus\":\"invalid\"");
        assertThat(result).contains("Validation did not return output schema");
    }

    @Test
    void testPrepareDataTransform_EmptyFields() {
        // Given - DLO has no fields
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");
        dlo.put("fields", Collections.emptyList());

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"validationStatus\":\"valid\"");
    }

    @Test
    void testPrepareDataTransform_ApiException() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/ssot/data-transforms-validation"));

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("error");
        assertThat(result).contains("500");
    }

    @Test
    void testPrepareDataTransform_MultipleDLOs() {
        // Given - validation response with multiple DLOs
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();

        // First DLO
        Map<String, Object> dlo1 = new HashMap<>();
        dlo1.put("name", "accounts__dll");
        List<Map<String, Object>> fields1 = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "account_id__c");
        fields1.add(field1);
        dlo1.put("fields", fields1);
        dloList.add(dlo1);

        // Second DLO
        Map<String, Object> dlo2 = new HashMap<>();
        dlo2.put("name", "contacts__dll");
        List<Map<String, Object>> fields2 = new ArrayList<>();
        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "contact_id__c");
        fields2.add(field2);
        dlo2.put("fields", fields2);
        dloList.add(dlo2);

        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("accounts__dll");
        assertThat(result).contains("contacts__dll");
        assertThat(result).contains("account_id__c");
        assertThat(result).contains("contact_id__c");
    }

    @Test
    void testPrepareDataTransform_DLOWithNullFields() {
        // Given - DLO with null fields list
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");
        dlo.put("fields", null);  // null fields

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should handle null fields gracefully
        assertThat(result).contains("\"validationStatus\":\"valid\"");
    }

    @Test
    void testPrepareDataTransform_MultipleFieldsNoPrimaryKeyPattern() {
        // Given - Multiple fields with no Id__c or *_id__c pattern
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "FirstField__c");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "SecondField__c");
        fields.add(field2);

        Map<String, Object> field3 = new HashMap<>();
        field3.put("name", "ThirdField__c");
        fields.add(field3);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should use first field as primary key
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("FirstField__c");
        assertThat(result).contains("\"isPrimaryKey\":true");
    }

    @Test
    void testPrepareDataTransform_WithDataspace() {
        // Given
        String dataspace = "custom_dataspace";
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");
        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, dataspace);

        // Then
        assertThat(result).contains("\"validationStatus\":\"valid\"");

        // Verify dataspace was passed to buildPath
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("dataspace=custom_dataspace");
    }

    @Test
    void testPrepareDataTransform_DLOWithLabel() {
        // Given - DLO has a label field
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");
        dlo.put("label", "Output Data Lake Object");  // Has label

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should preserve the label
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("Output Data Lake Object");
    }

    @Test
    void testPrepareDataTransform_CaseInsensitiveIdField() {
        // Given - Id field with different casing
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "Name__c");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "id__c");  // lowercase id__c
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect lowercase id__c as primary key
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"name\":\"id__c\"");
        assertThat(result).contains("\"isPrimaryKey\":true");
    }

    @Test
    void testPrepareDataTransform_EngagementCategory_WithTimestamp() {
        // Given - DLO name suggests engagement with timestamp field
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "web_event__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "event_id__c");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "event_timestamp__c");
        field2.put("type", "Timestamp");
        fields.add(field2);

        Map<String, Object> field3 = new HashMap<>();
        field3.put("name", "user_id__c");
        fields.add(field3);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect Engagement category and event timestamp
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"category\":\"Engagement\"");
        assertThat(result).contains("\"isEventTimestamp\":true");
        assertThat(result).contains("event_timestamp__c");
    }

    @Test
    void testPrepareDataTransform_ProfileCategory_Individual() {
        // Given - DLO name suggests profile
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "individual_profile__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect Profile category
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"category\":\"Profile\"");
    }

    @Test
    void testPrepareDataTransform_DMO_WithDataspace() {
        // Given - DMO transform with dataspace
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "Account__dmo");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT Id__c FROM Account__dlm");

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, "test_dataspace");

        // Then - should detect dataModelObject type
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"type\":\"dataModelObject\"");
    }

    @Test
    void testPrepareDataTransform_DMO_WithoutDataspace_Warning() {
        // Given - DMO transform without dataspace
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "Account__dmo");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT Id__c FROM Account__dlm");

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When - no dataspace provided
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should warn about missing dataspace
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"type\":\"dataModelObject\"");
        assertThat(result).contains("DMO transforms require a dataspace");
    }

    @Test
    void testPrepareDataTransform_DLO_FromDCSQLManifest() {
        // Given - DCSQL manifest with DLO sources
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        Map<String, Object> manifest = new HashMap<>();
        Map<String, Object> nodes = new HashMap<>();
        Map<String, Object> node = new HashMap<>();
        node.put("compiled_code", "SELECT Id__c FROM Account__dll");
        nodes.put("node1", node);
        manifest.put("nodes", nodes);
        definition.setManifest(manifest);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect dataLakeObject type
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_EngagementCategory_MultipleTimestampFields() {
        // Given - Engagement with multiple timestamp fields
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "click_activity__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "activity_id__c");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "created_at__c");
        field2.put("type", "Timestamp");
        fields.add(field2);

        Map<String, Object> field3 = new HashMap<>();
        field3.put("name", "updated_at__c");
        field3.put("type", "Timestamp");
        fields.add(field3);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect Engagement and pick first timestamp as event timestamp
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"category\":\"Engagement\"");
        assertThat(result).contains("created_at__c");
    }

    @Test
    void testPrepareDataTransform_EngagementCategory_AllPatterns() {
        // Test all engagement patterns: event, activity, interaction, engagement, click, view, impression, transaction, session, log, history
        String[] patterns = {"event", "activity", "interaction", "engagement", "click", "view",
                             "impression", "transaction", "session", "log", "history"};

        for (String pattern : patterns) {
            Map<String, Object> validationResponse = new HashMap<>();
            validationResponse.put("issues", Collections.emptyList());

            Map<String, Object> outputDataObjects = new HashMap<>();
            List<Map<String, Object>> dloList = new ArrayList<>();
            Map<String, Object> dlo = new HashMap<>();
            dlo.put("name", pattern + "_data__dll");

            List<Map<String, Object>> fields = new ArrayList<>();
            Map<String, Object> field = new HashMap<>();
            field.put("name", "Id__c");
            fields.add(field);

            Map<String, Object> tsField = new HashMap<>();
            tsField.put("name", "timestamp__c");
            tsField.put("type", "Timestamp");
            fields.add(tsField);

            dlo.put("fields", fields);
            dloList.add(dlo);
            outputDataObjects.put("test", dloList);
            validationResponse.put("outputDataObjects", outputDataObjects);

            when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

            DataTransformPrepareRequest request = new DataTransformPrepareRequest();
            request.setName("test");
            request.setType("batch");
            request.setDefinition(new DataTransformDefinitionInput());

            String result = dataTransformTools.prepareDataTransform(request, null);
            assertThat(result).contains("\"category\":\"Engagement\"");
        }
    }

    @Test
    void testPrepareDataTransform_ProfileCategory_AllPatterns() {
        // Test all profile patterns: individual, account, contact, customer, user, profile, lead, person, member, party
        String[] patterns = {"individual", "account", "contact", "customer", "user",
                             "profile", "lead", "person", "member", "party"};

        for (String pattern : patterns) {
            Map<String, Object> validationResponse = new HashMap<>();
            validationResponse.put("issues", Collections.emptyList());

            Map<String, Object> outputDataObjects = new HashMap<>();
            List<Map<String, Object>> dloList = new ArrayList<>();
            Map<String, Object> dlo = new HashMap<>();
            dlo.put("name", pattern + "_data__dll");

            List<Map<String, Object>> fields = new ArrayList<>();
            Map<String, Object> field = new HashMap<>();
            field.put("name", "Id__c");
            fields.add(field);
            dlo.put("fields", fields);

            dloList.add(dlo);
            outputDataObjects.put("test", dloList);
            validationResponse.put("outputDataObjects", outputDataObjects);

            when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

            DataTransformPrepareRequest request = new DataTransformPrepareRequest();
            request.setName("test");
            request.setType("batch");
            request.setDefinition(new DataTransformDefinitionInput());

            String result = dataTransformTools.prepareDataTransform(request, null);
            assertThat(result).contains("\"category\":\"Profile\"");
        }
    }

    @Test
    void testPrepareDataTransform_EngagementWithoutTimestamp_DefaultsToProfile() {
        // Given - Engagement pattern in name but no timestamp field
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "event_data__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should default to Profile when no timestamp
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"category\":\"Profile\"");
    }

    @Test
    void testPrepareDataTransform_TimestampFieldByType() {
        // Given - Field detected by type not name
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "event_data__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "some_field__c");
        field.put("type", "DateTime");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect timestamp by type
        assertThat(result).contains("\"category\":\"Engagement\"");
    }

    @Test
    void testPrepareDataTransform_DateField() {
        // Given - Date field
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "event_data__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "event_date__c");
        field.put("type", "Date");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"category\":\"Engagement\"");
        assertThat(result).contains("event_date__c");
    }

    @Test
    void testPrepareDataTransform_MixedDLOAndDMO_DefaultsToDLO() {
        // Given - Mixed DLO and DMO sources
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT * FROM Account__dlm JOIN Contact__dll ON 1=1");

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - mixed sources default to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_NullDefinition_DefaultsDLO() {
        // Given - null definition
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        // No manifest, no expression

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - defaults to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_EmptyManifest_DefaultsDLO() {
        // Given - empty manifest
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        definition.setManifest(new HashMap<>());

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_NullDLOName_DefaultsProfile() {
        // Given - null DLO name
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", null);

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - defaults to Profile
        assertThat(result).contains("\"category\":\"Profile\"");
    }

    @Test
    void testPrepareDataTransform_ManifestWithNullNode() {
        // Given - manifest with null compiled_code
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        Map<String, Object> manifest = new HashMap<>();
        Map<String, Object> nodes = new HashMap<>();
        Map<String, Object> node = new HashMap<>();
        node.put("compiled_code", null);
        nodes.put("node1", node);
        manifest.put("nodes", nodes);
        definition.setManifest(manifest);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - defaults to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_DMOWithEmptyDataspace() {
        // Given - DMO with empty string dataspace
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "Account__dmo");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT Id__c FROM Account__dlm");

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When - empty dataspace
        String result = dataTransformTools.prepareDataTransform(request, "  ");

        // Then - should warn
        assertThat(result).contains("DMO transforms require a dataspace");
    }

    @Test
    void testPrepareDataTransform_DetermineOutputType_NullDefinition() {
        // Given - definition is null (triggers exception path in determineOutputType)
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(null); // This will trigger exception handling in determineOutputType

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should succeed with valid status (defaults to DLO for output type)
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("Id__c");
        assertThat(result).contains("Profile");
    }

    @Test
    void testPrepareDataTransform_AnalyzeManifest_ExceptionHandling() {
        // Given - manifest with invalid structure that causes exception
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        Map<String, Object> manifest = new HashMap<>();
        // Create a manifest with invalid node structure to trigger exception
        Map<String, Object> nodes = new HashMap<>();
        nodes.put("node1", "invalid_structure"); // String instead of Map
        manifest.put("nodes", nodes);
        definition.setManifest(manifest);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When - this will catch exception and default to DLO
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should handle exception gracefully and default to DLO
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_NullExpression_DefaultsDLO() {
        // Given - expression is null
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression(null); // null expression

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("streaming");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should default to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_EmptyExpression_DefaultsDLO() {
        // Given - expression is empty
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("  "); // empty/whitespace expression

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("streaming");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should default to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_ManifestWithNullNodes() {
        // Given - manifest with null nodes
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        Map<String, Object> manifest = new HashMap<>();
        manifest.put("nodes", null); // null nodes
        definition.setManifest(manifest);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should default to DLO
        assertThat(result).contains("\"type\":\"dataLakeObject\"");
    }

    @Test
    void testPrepareDataTransform_EngagementCategory_NoTimestampField() {
        // Given - Engagement pattern in name but NO timestamp fields (eventTimestampField is null)
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "click_event__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "event_id__c");
        field1.put("type", "Text");
        fields.add(field1);

        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "user_id__c");
        field2.put("type", "Text");
        fields.add(field2);

        // No timestamp field - should still be Engagement due to name pattern
        // but eventTimestampField will be null
        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should be Profile (no timestamp to confirm engagement)
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("\"category\":\"Profile\"");
        // Should NOT contain isEventTimestamp since no timestamp field found
        assertThat(result).doesNotContain("isEventTimestamp");
    }

    @Test
    void testPrepareDataTransform_EngagementCategory_NullEventTimestamp() {
        // Given - Engagement with timestamp but detectEventTimestampField returns null in edge case
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "activity_log__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field1 = new HashMap<>();
        field1.put("name", "activity_id__c");
        fields.add(field1);

        // Add a timestamp to make it Engagement category
        Map<String, Object> field2 = new HashMap<>();
        field2.put("name", "created_timestamp__c");
        field2.put("type", "Timestamp");
        fields.add(field2);

        dlo.put("fields", fields);
        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        request.setDefinition(new DataTransformDefinitionInput());

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should detect Engagement and eventTimestamp
        assertThat(result).contains("\"category\":\"Engagement\"");
        assertThat(result).contains("\"isEventTimestamp\":true");
        assertThat(result).contains("created_timestamp__c");
    }

    @Test
    void testPrepareDataTransform_PreparedPayloadWithoutDefinition() {
        // Given - scenario where preparedPayload has no definition key
        Map<String, Object> validationResponse = new HashMap<>();
        validationResponse.put("issues", Collections.emptyList());

        Map<String, Object> outputDataObjects = new HashMap<>();
        List<Map<String, Object>> dloList = new ArrayList<>();
        Map<String, Object> dlo = new HashMap<>();
        dlo.put("name", "output__dll");

        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> field = new HashMap<>();
        field.put("name", "Id__c");
        fields.add(field);
        dlo.put("fields", fields);

        dloList.add(dlo);
        outputDataObjects.put("test", dloList);
        validationResponse.put("outputDataObjects", outputDataObjects);

        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(validationResponse);

        // Create request where JsonUtil.toMap might not include definition
        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        request.setName("test");
        request.setType("batch");
        // Don't set definition - it's null

        // When
        String result = dataTransformTools.prepareDataTransform(request, null);

        // Then - should still succeed even though definition is null
        assertThat(result).contains("\"validationStatus\":\"valid\"");
        assertThat(result).contains("Id__c");
    }
}
