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
import com.salesforce.data360.mcp.model.request.dataaction.*;
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
class DataActionToolsTest {

    @Mock
    private Data360Client client;

    private DataActionTools dataActionTools;

    @BeforeEach
    void setUp() {
        dataActionTools = new DataActionTools(client);
    }

    @Test
    void testListDataActions_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(Map.of("id", "da-1", "dataActionName", "MyAction"))
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dataActionTools.listDataActions(null);

        // Then
        assertThat(result).contains("MyAction");
        verify(client).get("/ssot/data-actions", Map.class);
    }

    @Test
    void testGetDataAction_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "da-1", "dataActionName", "MyAction");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dataActionTools.getDataAction("da-1", null);

        // Then
        assertThat(result).contains("da-1");
        verify(client).get("/ssot/data-actions/da-1", Map.class);
    }

    @Test
    void testCreateDataAction_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "da-new", "dataActionName", "NewAction");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataActionCreateRequest request = new DataActionCreateRequest();
        request.setDataActionName("NewAction");
        request.setDeveloperName("New_Action");
        request.setDataspace(DEFAULT_DATASPACE);
        request.setDescription("A test action");

        // When
        String result = dataActionTools.createDataAction(request, null);

        // Then
        assertThat(result).contains("da-new");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(eq("/ssot/data-actions"), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).containsEntry("dataActionName", "NewAction");
        assertThat(bodyCaptor.getValue()).containsEntry("developerName", "New_Action");
        assertThat(bodyCaptor.getValue()).containsEntry("dataspace", DEFAULT_DATASPACE);
        assertThat(bodyCaptor.getValue()).containsEntry("description", "A test action");
    }

    @Test
    void testCreateDataAction_withNestedJson() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "da-new");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataActionCreateRequest request = new DataActionCreateRequest();
        request.setDataActionName("NewAction");
        request.setDeveloperName("New_Action");
        request.setDataspace(DEFAULT_DATASPACE);
        request.setDataActionSources(List.of(Map.of("sourceType", "DLO")));

        // When
        String result = dataActionTools.createDataAction(request, null);

        // Then
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(eq("/ssot/data-actions"), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsKey("dataActionSources");
    }

    @Test
    void testCreateDataAction_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/data-actions"));

        DataActionCreateRequest request = new DataActionCreateRequest();
        request.setDataActionName("NewAction");
        request.setDeveloperName("New_Action");
        request.setDataspace(DEFAULT_DATASPACE);

        // When
        String result = dataActionTools.createDataAction(request, null);

        // Then
        assertThat(result).contains("error", "400");
    }

    @Test
    void testListDataActionTargets_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(Map.of("id", "dat-1", "apiName", "MyTarget"))
        );
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dataActionTools.listDataActionTargets(null);

        // Then
        assertThat(result).contains("MyTarget");
        verify(client).get("/ssot/data-action-targets", Map.class);
    }

    @Test
    void testGetDataActionTarget_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "dat-1", "apiName", "MyTarget");
        when(client.get(anyString(), eq(Map.class))).thenReturn(mockResponse);

        // When
        String result = dataActionTools.getDataActionTarget("dat-1", null);

        // Then
        assertThat(result).contains("dat-1");
        verify(client).get("/ssot/data-action-targets/dat-1", Map.class);
    }

    @Test
    void testCreateDataActionTarget_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "dat-new", "apiName", "NewTarget");
        when(client.post(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataActionTargetConfig config = new DataActionTargetConfig();
        config.setTargetEndpoint("https://example.com");

        DataActionTargetCreateRequest request = new DataActionTargetCreateRequest();
        request.setApiName("NewTarget");
        request.setLabel("New Target");
        request.setType("Webhook");
        request.setConfig(config);

        // When
        String result = dataActionTools.createDataActionTarget(request, null);

        // Then
        assertThat(result).contains("dat-new");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(eq("/ssot/data-action-targets"), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).containsEntry("apiName", "NewTarget");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "New Target");
        assertThat(bodyCaptor.getValue()).containsEntry("type", "Webhook");
        assertThat(bodyCaptor.getValue()).containsKey("config");
    }

    @Test
    void testCreateDataActionTarget_error() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid target", "/ssot/data-action-targets"));

        DataActionTargetCreateRequest request = new DataActionTargetCreateRequest();
        request.setApiName("Bad");
        request.setLabel("Bad");
        request.setType("Webhook");

        // When
        String result = dataActionTools.createDataActionTarget(request, null);

        // Then
        assertThat(result).contains("error", "400");
    }

    @Test
    void testUpdateDataActionTarget_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "dat-1", "label", "Updated");
        when(client.patch(anyString(), any(), eq(Map.class))).thenReturn(mockResponse);

        DataActionTargetUpdateRequest request = new DataActionTargetUpdateRequest();
        request.setLabel("Updated");

        // When
        String result = dataActionTools.updateDataActionTarget("dat-1", request, null);

        // Then
        assertThat(result).contains("Updated");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(eq("/ssot/data-action-targets/dat-1"), bodyCaptor.capture(), eq(Map.class));
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Updated");
    }

    @Test
    void testUpdateDataActionTarget_error() {
        // Given
        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Target not found", "/ssot/data-action-targets/bad"));

        DataActionTargetUpdateRequest request = new DataActionTargetUpdateRequest();
        request.setLabel("Updated");

        // When
        String result = dataActionTools.updateDataActionTarget("bad", request, null);

        // Then
        assertThat(result).contains("error", "404");
    }

    @Test
    void testDeleteDataActionTarget_success() {
        // Given - delete returns void

        // When
        String result = dataActionTools.deleteDataActionTarget("dat-1", null);

        // Then
        assertThat(result).contains("success");
        verify(client).delete("/ssot/data-action-targets/dat-1");
    }

    @Test
    void testDeleteDataActionTarget_error() {
        // Given
        doThrow(new ApiException(404, "Not found", "/ssot/data-action-targets/bad"))
            .when(client).delete(anyString());

        // When
        String result = dataActionTools.deleteDataActionTarget("bad", null);

        // Then
        assertThat(result).contains("error", "404");
    }
}
