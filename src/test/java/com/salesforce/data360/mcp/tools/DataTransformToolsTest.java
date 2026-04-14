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

import static com.salesforce.data360.mcp.tools.TestConstants.DEFAULT_DATASPACE;
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
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(
                Map.of("id", "transform-123", "name", "MyTransform")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.listDataTransforms(dataspace);

        // Then
        assertThat(result).contains("transform-123", "MyTransform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms", "dataspace=default");
    }

    @Test
    void testListDataTransforms_noDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", java.util.List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.listDataTransforms(null);

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
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "id", transformId,
            "name", "MyTransform",
            "sql", "SELECT * FROM Source__dlm"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransform(transformId, dataspace);

        // Then
        assertThat(result).contains(transformId, "MyTransform");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123", "dataspace=default");
    }

    @Test
    void testCreateDataTransform_success() {
        // Given
        String dataspace = DEFAULT_DATASPACE;
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
        String result = dataTransformTools.createDataTransform(request, dataspace);

        // Then
        assertThat(result).contains("transform-new");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms", "dataspace=default");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "MyTransform");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "My Transform");
        assertThat(bodyCaptor.getValue()).containsEntry("type", "sql");
        assertThat(bodyCaptor.getValue()).containsKey("definition");
    }

    @Test
    void testUpdateDataTransform_success() {
        // Given
        String transformId = "transform-123";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "id", transformId,
            "description", "Updated description"
        );

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformUpdateRequest request = new DataTransformUpdateRequest();
        request.setDescription("Updated description");

        // When
        String result = dataTransformTools.updateDataTransform(transformId, request, dataspace);

        // Then
        assertThat(result).contains("Updated description");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123", "dataspace=default");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Updated description");
    }

    @Test
    void testDeleteDataTransform_success() {
        // Given
        String transformId = "transform-123";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.deleteDataTransform(transformId, dataspace);

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
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-456",
            "status", "RUNNING"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.runDataTransform(transformId, dataspace);

        // Then
        assertThat(result).contains("job-456", "RUNNING");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-transforms/transform-123/actions/run");
    }

    @Test
    void testValidateDataTransform_success() {
        // Given
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "valid", true,
            "errors", java.util.List.of()
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataTransformValidateRequest request = new DataTransformValidateRequest();
        request.setSql("SELECT 1");

        // When
        String result = dataTransformTools.validateDataTransform(request, dataspace);

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
        request.setSql("SELECT 1");

        String result = dataTransformTools.validateDataTransform(request, null);

        assertThat(result).contains("valid");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsKey("sql");
    }

    @Test
    void testGetDataTransformSchedule_success() {
        // Given
        String transformId = "transform-123";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "frequency", "DAILY",
            "time", "10:00"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataTransformTools.getDataTransformSchedule(transformId, dataspace);

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
        String dataspace = DEFAULT_DATASPACE;
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
        String result = dataTransformTools.setDataTransformSchedule(transformId, request, dataspace);

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
        String result = dataTransformTools.listDataTransforms(null);

        // Then
        assertThat(result).contains("error", "Internal Server Error", "500");
    }
}
