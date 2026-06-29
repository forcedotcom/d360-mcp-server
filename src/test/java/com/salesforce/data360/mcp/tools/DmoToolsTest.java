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
import com.salesforce.data360.mcp.model.request.dmo.DataObjectFieldInput;
import com.salesforce.data360.mcp.model.request.dmo.DmoCreateRequest;
import com.salesforce.data360.mcp.model.request.dmo.DmoUpdateRequest;
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
class DmoToolsTest {

    @Mock
    private Data360Client client;

    private DmoTools dmoTools;

    @BeforeEach
    void setUp() {
        dmoTools = new DmoTools(client);
    }

    @Test
    void testListDataModelObjects_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("name", "Individual__dlm", "label", "Individual", "category", "Profile")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.listDataModelObjects(null, null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(result).contains("Individual__dlm");
        verify(client).get("/ssot/data-model-objects", Map.class);
    }

    @Test
    void testListDataModelObjects_withCategory() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.listDataModelObjects("Profile", null, null, null, null, null, null, null, null, null, null, null);

        // Then
        verify(client).get("/ssot/data-model-objects?dataObjectCategory=Profile", Map.class);
    }

    @Test
    void testListDataModelObjects_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.listDataModelObjects(null, DEFAULT_DATASPACE, null, null, null, null, null, null, null, null, null, null);

        // Then
        verify(client).get("/ssot/data-model-objects?dataSpaceName=default", Map.class);
    }

    @Test
    void testListDataModelObjects_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/ssot/data-model-objects"));

        // When
        String result = dmoTools.listDataModelObjects(null, null, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(result).contains("error", "500", "Internal Server Error");
    }

    @Test
    void testGetDataModelObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "Individual__dlm",
            "label", "Individual",
            "fields", List.of(
                Map.of("name", "ssot__Id__c", "dataType", "Text")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.getDataModelObject("Individual__dlm", null, null);

        // Then
        assertThat(result).contains("Individual__dlm", "ssot__Id__c");
        verify(client).get("/ssot/data-model-objects/Individual__dlm", Map.class);
    }

    @Test
    void testGetDataModelObject_withParams() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "Individual__dlm");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.getDataModelObject("Individual__dlm", "true", "true");

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("includeActiveFieldsOnly=true");
        assertThat(pathCaptor.getValue()).contains("includeStatus=true");
    }

    @Test
    void testGetDataModelObject_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not Found", "/ssot/data-model-objects/NonExistent__dlm"));

        // When
        String result = dmoTools.getDataModelObject("NonExistent__dlm", null, null);

        // Then
        assertThat(result).contains("error", "404", "Not Found");
    }

    @Test
    void testCreateDataModelObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "MyCustomObject__dlm",
            "label", "My Custom Object"
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        DmoCreateRequest request = new DmoCreateRequest();
        request.setName("MyCustomObject__dlm");
        request.setLabel("My Custom Object");
        String result = dmoTools.createDataModelObject(request);

        // Then
        assertThat(result).contains("MyCustomObject__dlm");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-objects");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("name")).isEqualTo("MyCustomObject__dlm");
        assertThat(body.get("label")).isEqualTo("My Custom Object");
    }

    @Test
    void testCreateDataModelObject_withFields() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "MyCustomObject__dlm");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataObjectFieldInput field = new DataObjectFieldInput();
        field.setName("field1__c");
        field.setLabel("Field 1");
        field.setDataType("Text");

        // When
        DmoCreateRequest request = new DmoCreateRequest();
        request.setName("MyCustomObject__dlm");
        request.setLabel("My Custom Object");
        request.setCategory("Profile");
        request.setFields(List.of(field));
        String result = dmoTools.createDataModelObject(request);

        // Then
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("category")).isEqualTo("Profile");
        assertThat(body.get("fields")).isInstanceOf(List.class);
    }

    @Test
    void testCreateDataModelObject_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid DMO name", "/ssot/data-model-objects"));

        // When
        DmoCreateRequest request = new DmoCreateRequest();
        request.setName("InvalidName");
        request.setLabel("Invalid");
        String result = dmoTools.createDataModelObject(request);

        // Then
        assertThat(result).contains("error", "400", "Invalid DMO name");
    }

    @Test
    void testUpdateDataModelObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "MyCustomObject__dlm");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        DmoUpdateRequest request = new DmoUpdateRequest();
        request.setLabel("Updated Label");
        String result = dmoTools.updateDataModelObject("MyCustomObject__dlm", request);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-objects/MyCustomObject__dlm");
        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("label")).isEqualTo("Updated Label");
    }

    @Test
    void testUpdateDataModelObject_withFields() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "MyCustomObject__dlm");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataObjectFieldInput field = new DataObjectFieldInput();
        field.setName("field2__c");
        field.setLabel("Field 2");

        // When
        DmoUpdateRequest request = new DmoUpdateRequest();
        request.setFields(List.of(field));
        String result = dmoTools.updateDataModelObject("MyCustomObject__dlm", request);

        // Then
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body.get("fields")).isInstanceOf(List.class);
    }

    @Test
    void testUpdateDataModelObject_error() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "DMO not found", "/ssot/data-model-objects/NonExistent__dlm"));

        // When
        DmoUpdateRequest request = new DmoUpdateRequest();
        request.setLabel("Label");
        String result = dmoTools.updateDataModelObject("NonExistent__dlm", request);

        // Then
        assertThat(result).contains("error", "404");
    }

    @Test
    void testDeleteDataModelObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dmoTools.deleteDataModelObject("MyCustomObject__dlm");

        // Then
        assertThat(result).contains("success");
        verify(client).delete("/ssot/data-model-objects/MyCustomObject__dlm", Map.class);
    }

    @Test
    void testDeleteDataModelObject_error() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(409, "DMO has dependencies", "/ssot/data-model-objects/MyCustomObject__dlm"));

        // When
        String result = dmoTools.deleteDataModelObject("MyCustomObject__dlm");

        // Then
        assertThat(result).contains("error", "409", "has dependencies");
    }
}
