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
import com.salesforce.data360.mcp.model.request.datastream.DataLakeFieldInput;
import com.salesforce.data360.mcp.model.request.dlo.DloCreateRequest;
import com.salesforce.data360.mcp.model.request.dlo.DloPatchRequest;
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
class DloToolsTest {

    @Mock
    private Data360Client client;

    private DloTools dloTools;

    @BeforeEach
    void setUp() {
        dloTools = new DloTools(client);
    }

    @Test
    void testListDataLakeObjects_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("name", "Account_00D000000000000__dll", "category", "Profile")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.listDataLakeObjects(null, null);

        // Then
        assertThat(result).contains("Account_00D000000000000__dll");
        verify(client).get("/ssot/data-lake-objects", Map.class);
    }

    @Test
    void testListDataLakeObjects_withCategory() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.listDataLakeObjects("Engagement", null);

        // Then
        verify(client).get("/ssot/data-lake-objects?category=Engagement", Map.class);
    }

    @Test
    void testListDataLakeObjects_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.listDataLakeObjects(null, DEFAULT_DATASPACE);

        // Then
        verify(client).get("/ssot/data-lake-objects?dataspace=default", Map.class);
    }

    @Test
    void testListDataLakeObjects_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/ssot/data-lake-objects"));

        // When
        String result = dloTools.listDataLakeObjects(null, null);

        // Then
        assertThat(result).contains("error", "500", "Internal Server Error");
    }

    @Test
    void testGetDataLakeObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "Account_00D000000000000__dll",
            "category", "Profile",
            "fields", List.of(
                Map.of("name", "Id", "dataType", "Text"),
                Map.of("name", "Name", "dataType", "Text")
            )
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.getDataLakeObject("Account_00D000000000000__dll", null);

        // Then
        assertThat(result).contains("Account_00D000000000000__dll", "Id", "Name");
        verify(client).get("/ssot/data-lake-objects/Account_00D000000000000__dll", Map.class);
    }

    @Test
    void testGetDataLakeObject_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "Account_00D000000000000__dll");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.getDataLakeObject("Account_00D000000000000__dll", DEFAULT_DATASPACE);

        // Then
        verify(client).get("/ssot/data-lake-objects/Account_00D000000000000__dll?dataspace=default", Map.class);
    }

    @Test
    void testGetDataLakeObject_error() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "DLO not found", "/ssot/data-lake-objects/NonExistent__dll"));

        // When
        String result = dloTools.getDataLakeObject("NonExistent__dll", null);

        // Then
        assertThat(result).contains("error", "404", "not found");
    }

    @Test
    void testCreateDataLakeObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "name", "CustomDLO__dll",
            "category", "Other"
        );
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataLakeFieldInput field = new DataLakeFieldInput();
        field.setName("field1");
        field.setLabel("Field 1");
        field.setDataType("Text");
        field.setIsPrimaryKey(false);

        DloCreateRequest request = new DloCreateRequest();
        request.setName("CustomDLO__dll");
        request.setLabel("Custom DLO");
        request.setCategory("Other");
        request.setDataLakeFieldInputRepresentations(List.of(field));

        // When
        String result = dloTools.createDataLakeObject(request, null);

        // Then
        assertThat(result).contains("CustomDLO__dll");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-lake-objects");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "CustomDLO__dll");
        assertThat(bodyCaptor.getValue()).containsEntry("category", "Other");
    }

    @Test
    void testCreateDataLakeObject_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "CustomDLO__dll");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataLakeFieldInput field = new DataLakeFieldInput();
        field.setName("field1");
        field.setLabel("Field 1");
        field.setDataType("Text");
        field.setIsPrimaryKey(false);

        DloCreateRequest request = new DloCreateRequest();
        request.setName("CustomDLO__dll");
        request.setLabel("Custom DLO");
        request.setCategory("Other");
        request.setDataLakeFieldInputRepresentations(List.of(field));

        // When
        String result = dloTools.createDataLakeObject(request, DEFAULT_DATASPACE);

        // Then
        verify(client).post(eq("/ssot/data-lake-objects?dataspace=default"), any(), eq(Map.class));
    }

    @Test
    void testCreateDataLakeObject_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid DLO definition", "/ssot/data-lake-objects"));

        DataLakeFieldInput field = new DataLakeFieldInput();
        field.setName("f");
        field.setLabel("F");
        field.setDataType("Text");
        field.setIsPrimaryKey(false);

        DloCreateRequest request = new DloCreateRequest();
        request.setName("Invalid");
        request.setLabel("Invalid");
        request.setCategory("Other");
        request.setDataLakeFieldInputRepresentations(List.of(field));

        // When
        String result = dloTools.createDataLakeObject(request, null);

        // Then
        assertThat(result).contains("error", "400", "Invalid DLO definition");
    }

    @Test
    void testUpdateDataLakeObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "CustomDLO__dll");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataLakeFieldInput field = new DataLakeFieldInput();
        field.setName("newField");
        field.setLabel("New Field");
        field.setDataType("Number");
        field.setIsPrimaryKey(false);

        DloPatchRequest request = new DloPatchRequest();
        request.setDataLakeFieldInputRepresentations(List.of(field));

        // When
        String result = dloTools.updateDataLakeObject("CustomDLO__dll", request, null);

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-lake-objects/CustomDLO__dll");
        assertThat(bodyCaptor.getValue()).containsKey("dataLakeFieldInputRepresentations");
    }

    @Test
    void testUpdateDataLakeObject_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "CustomDLO__dll");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DloPatchRequest request = new DloPatchRequest();
        request.setLabel("Updated Label");

        // When
        String result = dloTools.updateDataLakeObject("CustomDLO__dll", request, DEFAULT_DATASPACE);

        // Then
        verify(client).patch(eq("/ssot/data-lake-objects/CustomDLO__dll?dataspace=default"), any(), eq(Map.class));
    }

    @Test
    void testUpdateDataLakeObject_error() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "DLO not found", "/ssot/data-lake-objects/NonExistent__dll"));

        DloPatchRequest request = new DloPatchRequest();
        request.setLabel("Label");

        // When
        String result = dloTools.updateDataLakeObject("NonExistent__dll", request, null);

        // Then
        assertThat(result).contains("error", "404");
    }

    @Test
    void testDeleteDataLakeObject_basic() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.deleteDataLakeObject("CustomDLO__dll", null);

        // Then
        assertThat(result).contains("success");
        verify(client).delete("/ssot/data-lake-objects/CustomDLO__dll", Map.class);
    }

    @Test
    void testDeleteDataLakeObject_withDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("success", true);
        when(client.delete(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dloTools.deleteDataLakeObject("CustomDLO__dll", DEFAULT_DATASPACE);

        // Then
        verify(client).delete("/ssot/data-lake-objects/CustomDLO__dll?dataspace=default", Map.class);
    }

    @Test
    void testDeleteDataLakeObject_error() {
        // Given
        when(client.delete(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(409, "DLO has active mappings", "/ssot/data-lake-objects/CustomDLO__dll"));

        // When
        String result = dloTools.deleteDataLakeObject("CustomDLO__dll", null);

        // Then
        assertThat(result).contains("error", "409", "active mappings");
    }
}
