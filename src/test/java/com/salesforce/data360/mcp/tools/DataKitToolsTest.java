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
import com.salesforce.data360.mcp.model.request.datakit.DataKitDeployComponentDetails;
import com.salesforce.data360.mcp.model.request.datakit.DataKitDeployRequest;
import com.salesforce.data360.mcp.model.request.datakit.DataKitUndeployComponentDetails;
import com.salesforce.data360.mcp.model.request.datakit.DataKitUndeployRequest;
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
class DataKitToolsTest {

    @Mock
    private Data360Client client;

    private DataKitTools dataKitTools;

    @BeforeEach
    void setUp() {
        dataKitTools = new DataKitTools(client);
    }

    @Test
    void testListDataKits_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(
                Map.of("id", "datakit-123", "name", "MyDataKit")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.listDataKits("myNamespace");

        // Then
        assertThat(result).contains("datakit-123", "MyDataKit");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits", "namespace=myNamespace");
    }

    @Test
    void testListDataKits_noNamespace() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", java.util.List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.listDataKits(null);

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-kits");
    }

    @Test
    void testGetDataKit_success() {
        // Given
        String dataKitId = "datakit-123";
        Map<String, Object> mockResponse = Map.of(
            "id", dataKitId,
            "name", "MyDataKit",
            "version", "1.0.0"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.getDataKit(dataKitId, null);

        // Then
        assertThat(result).contains(dataKitId, "MyDataKit");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-kits/datakit-123");
    }

    @Test
    void testGetDataKitManifest_success() {
        // Given
        String dataKitId = "datakit-123";
        Map<String, Object> mockResponse = Map.of(
            "components", java.util.List.of(
                Map.of("id", "comp-1", "type", "DMO")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.getDataKitManifest(dataKitId);

        // Then
        assertThat(result).contains("components", "comp-1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/datakit/datakit-123/manifest");
    }

    @Test
    void testDeployDataKit_success() {
        // Given
        String dataKitDevName = "my_datakit";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-456",
            "status", "IN_PROGRESS"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataKitDeployComponentDetails component = new DataKitDeployComponentDetails();
        component.setType("CalculatedInsight");
        component.setConfig(Map.of("name", "comp-1"));

        DataKitDeployRequest request = new DataKitDeployRequest();
        request.setComponents(List.of(component));

        // When
        String result = dataKitTools.deployDataKit(dataKitDevName, request, dataspace, null, null);

        // Then
        assertThat(result).contains("job-456", "IN_PROGRESS");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/my_datakit", "dataspace=default");
        assertThat(bodyCaptor.getValue()).containsKey("components");
    }

    @Test
    void testUndeployDataKit_success() {
        // Given
        String dataKitId = "datakit-123";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-789",
            "status", "IN_PROGRESS"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataKitUndeployComponentDetails component = new DataKitUndeployComponentDetails();
        component.setName("comp-1");
        component.setType("DMO");

        DataKitUndeployRequest request = new DataKitUndeployRequest();
        request.setComponents(List.of(component));

        // When
        String result = dataKitTools.undeployDataKit(dataKitId, request, dataspace, null);

        // Then
        assertThat(result).contains("job-789");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/datakit-123/undeploy");
    }

    @Test
    void testGetDataKitDeploymentStatus_success() {
        // Given
        String jobId = "job-456";
        Map<String, Object> mockResponse = Map.of(
            "jobId", jobId,
            "status", "COMPLETED",
            "progress", 100
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.getDataKitDeploymentStatus(jobId);

        // Then
        assertThat(result).contains("COMPLETED", "100");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/deployment-jobs/job-456");
    }

    @Test
    void testGetDataKitComponentStatus_success() {
        // Given
        String dataKitId = "datakit-123";
        String componentId = "comp-1";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "componentId", componentId,
            "status", "DEPLOYED"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.getDataKitComponentStatus(dataKitId, componentId, dataspace);

        // Then
        assertThat(result).contains("DEPLOYED");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/datakit-123/components/comp-1/deployment-status");
    }

    @Test
    void testListDataKitComponents_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "components", java.util.List.of(
                Map.of("id", "comp-1", "type", "DMO")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.listDataKitComponents(null, null, null, null);

        // Then
        assertThat(result).contains("components", "comp-1");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/available-components");
    }

    @Test
    void testGetDataKitComponentDependencies_success() {
        // Given
        String dataKitId = "datakit-123";
        String componentId = "comp-1";
        String dataspace = DEFAULT_DATASPACE;
        Map<String, Object> mockResponse = Map.of(
            "dependencies", java.util.List.of(
                Map.of("id", "comp-2", "type", "DMO")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataKitTools.getDataKitComponentDependencies(dataKitId, componentId, dataspace, null);

        // Then
        assertThat(result).contains("dependencies", "comp-2");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).contains("/ssot/data-kits/datakit-123/components/comp-1/dependencies");
    }

    @Test
    void testListDataKits_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal Server Error", "/ssot/data-kits"));

        // When
        String result = dataKitTools.listDataKits(null);

        // Then
        assertThat(result).contains("error", "Internal Server Error", "500");
    }
}
