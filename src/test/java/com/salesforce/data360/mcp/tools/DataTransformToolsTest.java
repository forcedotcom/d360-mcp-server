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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataTransformToolsTest {

    @Mock
    private Data360Client client;

    private DataTransformTools dataTransformTools;

    @BeforeEach
    void setUp() {
        dataTransformTools = new DataTransformTools(client);
    }

    @Test
    void testListDataTransforms_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(
                Map.of("id", "transform-123", "name", "MyTransform")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.listDataTransforms("myFilterGroup", null, null, null);

        // Then
        assertThat(result).contains("transform-123", "MyTransform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms", "filterGroup=myFilterGroup");
    }

    @Test
    void testListDataTransforms_noFilterGroup() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", java.util.List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.listDataTransforms(null, null, null, null);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms");
    }

    @Test
    void testGetDataTransform_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of(
            "id", transformId,
            "name", "MyTransform",
            "sql", "SELECT * FROM Source__dlm"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransform(transformId, null);

        // Then
        assertThat(result).contains(transformId, "MyTransform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms/transform-123");
    }

    @Test
    void testCreateDataTransform_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "transform-new",
            "name", "MyTransform"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("sql");
        definition.setVersion("1");

        DataTransformCreateRequest request = new DataTransformCreateRequest();
        request.setLabel("My Transform");
        request.setName("MyTransform");
        request.setType("sql");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.createDataTransform(request, null, null);

        // Then
        assertThat(result).contains("transform-new");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "MyTransform");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "My Transform");
        assertThat(bodyCaptor.getValue()).containsEntry("type", "sql");
        assertThat(bodyCaptor.getValue()).containsKey("definition");
    }

    @Test
    void testUpdateDataTransform_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of(
            "id", transformId,
            "description", "Updated description"
        );

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformUpdateRequest request = new DataTransformUpdateRequest();
        request.setDescription("Updated description");

        // When
        String result = dataTransformTools.updateDataTransform(transformId, request, null, null);

        // Then
        assertThat(result).contains("Updated description");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms/transform-123");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Updated description");
    }

    @Test
    void testDeleteDataTransform_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.deleteDataTransform(transformId, null);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123");
    }

    @Test
    void testRunDataTransform_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-456",
            "status", "RUNNING"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.runDataTransform(transformId, null, null);

        // Then
        assertThat(result).contains("job-456", "RUNNING");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123/actions/run");
    }

    @Test
    void testValidateDataTransform_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "valid", true,
            "errors", java.util.List.of()
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformValidateRequest request = new DataTransformValidateRequest();
        request.setName("my_transform");
        request.setLabel("My Transform");
        request.setType("streaming");
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT 1");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.validateDataTransform(request);

        // Then
        assertThat(result).contains("valid");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms-validation");
    }

    @Test
    void testValidateDataTransform_sendsBody() {
        Map<String, Object> mockResponse = Map.of("valid", true);
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataTransformValidateRequest request = new DataTransformValidateRequest();
        request.setName("my_transform");
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT 1");
        request.setDefinition(definition);

        String result = dataTransformTools.validateDataTransform(request);

        assertThat(result).contains("valid");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsKey("name");
        assertThat(bodyCaptor.getValue()).containsKey("definition");
    }

    @Test
    void testGetDataTransformSchedule_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of(
            "frequency", "DAILY",
            "time", "10:00"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransformSchedule(transformId);


        // Then
        assertThat(result).contains("DAILY", "10:00");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123/schedule");
    }

    @Test
    void testSetDataTransformSchedule_success() {
        // Given
        String transformId = "transform-123";
        Map<String, Object> mockResponse = Map.of(
            "frequency", "DAILY",
            "time", "10:00"
        );

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformTimeInput time = new DataTransformTimeInput();
        time.setHour(10);
        time.setMinute(0);
        time.setTimeZone("UTC");

        DataTransformScheduleRequest request = new DataTransformScheduleRequest();
        request.setFrequency("DAILY");
        request.setTime(time);

        // When
        String result = dataTransformTools.setDataTransformSchedule(transformId, request, null);

        // Then
        assertThat(result).contains("DAILY");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123/schedule");
        assertThat(bodyCaptor.getValue()).containsEntry("frequency", "DAILY");
        assertThat(bodyCaptor.getValue()).containsKey("time");
    }

    @Test
    void testListDataTransforms_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/ssot/data-transforms"));

        // When
        String result = dataTransformTools.listDataTransforms(null, null, null, null);

        // Then
        assertThat(result).contains("error", "Internal Server Error", "500");
    }

    @Test
    void testGetDataTransform_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Transform not found", "/ssot/data-transforms/invalid-id"));

        // When
        String result = dataTransformTools.getDataTransform("invalid-id", null);

        // Then
        assertThat(result).contains("error", "Transform not found", "404");
    }

    @Test
    void testCreateDataTransform_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid request", "/ssot/data-transforms"));

        DataTransformCreateRequest request = new DataTransformCreateRequest();
        request.setName("test");

        // When
        String result = dataTransformTools.createDataTransform(request, null, null);

        // Then
        assertThat(result).contains("error", "Invalid request", "400");
    }

    @Test
    void testUpdateDataTransform_errorHandling() {
        // Given
        when(client.put(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Transform not found", "/ssot/data-transforms/missing"));

        DataTransformUpdateRequest request = new DataTransformUpdateRequest();
        request.setDescription("Updated");

        // When
        String result = dataTransformTools.updateDataTransform("missing", request, null, null);

        // Then
        assertThat(result).contains("error", "Transform not found", "404");
    }

    @Test
    void testDeleteDataTransform_errorHandling() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(403, "Forbidden", "/ssot/data-transforms/protected"));

        // When
        String result = dataTransformTools.deleteDataTransform("protected", null);

        // Then
        assertThat(result).contains("error", "Forbidden", "403");
    }

    @Test
    void testRunDataTransform_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(409, "Transform already running", "/ssot/data-transforms/123/actions/run"));

        // When
        String result = dataTransformTools.runDataTransform("123", null, null);

        // Then
        assertThat(result).contains("error", "Transform already running", "409");
    }

    @Test
    void testValidateDataTransform_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid SQL", "/ssot/data-transforms-validation"));

        DataTransformValidateRequest request = new DataTransformValidateRequest();
        request.setName("my_transform");
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("INVALID SQL");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.validateDataTransform(request);

        // Then
        assertThat(result).contains("error", "Invalid SQL", "400");
    }

    @Test
    void testGetDataTransformSchedule_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Schedule not found", "/ssot/data-transforms/123/schedule"));

        // When
        String result = dataTransformTools.getDataTransformSchedule("123");

        // Then
        assertThat(result).contains("error", "Schedule not found", "404");
    }

    @Test
    void testSetDataTransformSchedule_errorHandling() {
        // Given
        when(client.put(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid schedule", "/ssot/data-transforms/123/schedule"));

        DataTransformScheduleRequest request = new DataTransformScheduleRequest();
        request.setFrequency("INVALID");

        // When
        String result = dataTransformTools.setDataTransformSchedule("123", request, null);

        // Then
        assertThat(result).contains("error", "Invalid schedule", "400");
    }

    @Test
    void testGetDataTransform_withFilterGroup() {
        // Given
        String transformId = "transform-456";
        String filterGroup = "custom_group";
        Map<String, Object> mockResponse = Map.of(
            "id", transformId,
            "name", "CustomTransform"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransform(transformId, filterGroup);

        // Then
        assertThat(result).contains(transformId, "CustomTransform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("filterGroup=custom_group");
    }

    @Test
    void testCreateDataTransform_withFilterGroup() {
        // Given
        String filterGroup = "prod_group";
        Map<String, Object> mockResponse = Map.of("id", "new-transform");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformCreateRequest request = new DataTransformCreateRequest();
        request.setName("test");
        request.setLabel("Test");
        request.setType("batch");

        // When
        String result = dataTransformTools.createDataTransform(request, filterGroup, null);

        // Then
        assertThat(result).contains("new-transform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("filterGroup=prod_group");
    }

    @Test
    void testUpdateDataTransform_withFilterGroup() {
        // Given
        String transformId = "transform-789";
        String filterGroup = "test_group";
        Map<String, Object> mockResponse = Map.of("id", transformId);

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformUpdateRequest request = new DataTransformUpdateRequest();
        request.setDescription("Updated");

        // When
        String result = dataTransformTools.updateDataTransform(transformId, request, filterGroup, null);

        // Then
        assertThat(result).contains(transformId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).put(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("filterGroup=test_group");
    }

    @Test
    void testDeleteDataTransform_withRequestedBy() {
        // Given
        String transformId = "transform-delete";
        String requestedBy = "user-1";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.deleteDataTransform(transformId, requestedBy);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("requestedBy=user-1");
    }

    @Test
    void testRunDataTransform_withRequestedBy() {
        // Given
        String transformId = "transform-run";
        String requestedBy = "user-1";
        Map<String, Object> mockResponse = Map.of("jobId", "job-123");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.runDataTransform(transformId, requestedBy, null);

        // Then
        assertThat(result).contains("job-123");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("requestedBy=user-1");
    }

    @Test
    void testValidateDataTransform_sendsRequestBody() {
        // Given
        Map<String, Object> mockResponse = Map.of("valid", true);

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformValidateRequest request = new DataTransformValidateRequest();
        request.setName("my_transform");
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT 1");
        request.setDefinition(definition);

        // When
        String result = dataTransformTools.validateDataTransform(request);

        // Then
        assertThat(result).contains("valid");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms-validation");
    }

    @Test
    void testGetDataTransformSchedule_path() {
        // Given
        String transformId = "transform-sched";
        Map<String, Object> mockResponse = Map.of("frequency", "DAILY");

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransformSchedule(transformId);

        // Then
        assertThat(result).contains("DAILY");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-transforms/transform-sched/schedule");
    }

    @Test
    void testSetDataTransformSchedule_withRequestedBy() {
        // Given
        String transformId = "transform-set-sched";
        String requestedBy = "user-1";
        Map<String, Object> mockResponse = Map.of("frequency", "WEEKLY");

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformScheduleRequest request = new DataTransformScheduleRequest();
        request.setFrequency("WEEKLY");

        // When
        String result = dataTransformTools.setDataTransformSchedule(transformId, request, requestedBy);

        // Then
        assertThat(result).contains("WEEKLY");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).put(pathCaptor.capture(), any(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("requestedBy=user-1");
    }
}
