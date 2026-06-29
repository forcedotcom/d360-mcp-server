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
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionCreateRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionFullUpdateRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionPatchRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionRunRequest;
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
class IdentityResolutionToolsTest {

    @Mock
    private Data360Client client;

    private IdentityResolutionTools identityResolutionTools;

    @BeforeEach
    void setUp() {
        identityResolutionTools = new IdentityResolutionTools(client);
    }

    @Test
    void testListIdentityResolutions_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(
                Map.of("id", "ir-123", "name", "Email Matching")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = identityResolutionTools.listIdentityResolutions(null);

        // Then
        assertThat(result).contains("ir-123", "Email Matching");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions");
    }

    @Test
    void testListIdentityResolutions_withFilterGroup() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", java.util.List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = identityResolutionTools.listIdentityResolutions("myFilterGroup");

        // Then
        assertThat(result).contains("data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions?filterGroup=myFilterGroup");
    }

    @Test
    void testGetIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of(
            "id", identityResolutionId,
            "name", "Email and Phone Matching",
            "rules", java.util.List.of()
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = identityResolutionTools.getIdentityResolution(identityResolutionId, null);

        // Then
        assertThat(result).contains(identityResolutionId, "Email and Phone Matching");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123");
    }

    @Test
    void testCreateIdentityResolution_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "ir-456", "label", "Email Matching");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        IdentityResolutionCreateRequest request = new IdentityResolutionCreateRequest();
        request.setLabel("Email Matching");
        request.setConfigurationType("STANDARD");
        request.setReconciliationRules(java.util.List.of());

        String result = identityResolutionTools.createIdentityResolution(request);

        // Then
        assertThat(result).contains("ir-456", "Email Matching");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Email Matching");
        assertThat(bodyCaptor.getValue()).containsEntry("configurationType", "STANDARD");
    }

    @Test
    void testUpdateIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("id", identityResolutionId);

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        IdentityResolutionPatchRequest request = new IdentityResolutionPatchRequest();
        request.setDescription("Updated description");

        String result = identityResolutionTools.updateIdentityResolution(identityResolutionId, request);

        // Then
        assertThat(result).contains(identityResolutionId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Updated description");
    }

    @Test
    void testFullUpdateIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("id", identityResolutionId);

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        IdentityResolutionFullUpdateRequest request = new IdentityResolutionFullUpdateRequest();
        request.setLabel("Updated Name");
        request.setConfigurationType("STANDARD");
        request.setReconciliationRules(java.util.List.of());

        String result = identityResolutionTools.fullUpdateIdentityResolution(identityResolutionId, request);

        // Then
        assertThat(result).contains(identityResolutionId);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Updated Name");
        assertThat(bodyCaptor.getValue()).containsEntry("configurationType", "STANDARD");
    }

    @Test
    void testDeleteIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = identityResolutionTools.deleteIdentityResolution(identityResolutionId);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123");
    }

    @Test
    void testPublishIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("status", "PUBLISHED");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = identityResolutionTools.publishIdentityResolution(identityResolutionId);

        // Then
        assertThat(result).contains("PUBLISHED");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123/actions/publish");
    }

    @Test
    void testRunIdentityResolution_success() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("jobId", "job-789", "status", "RUNNING");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        IdentityResolutionRunRequest request = new IdentityResolutionRunRequest();
        request.setCallingApp("myApp");
        request.setCallingAppInfo("info");

        String result = identityResolutionTools.runIdentityResolution(identityResolutionId, request);

        // Then
        assertThat(result).contains("job-789", "RUNNING");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/identity-resolutions/ir-123/actions/run-now");
        assertThat(bodyCaptor.getValue()).containsEntry("callingApp", "myApp");
        assertThat(bodyCaptor.getValue()).containsEntry("callingAppInfo", "info");
    }

    @Test
    void testRunIdentityResolution_noBody() {
        // Given
        String identityResolutionId = "ir-123";
        Map<String, Object> mockResponse = Map.of("jobId", "job-789");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        IdentityResolutionRunRequest request = new IdentityResolutionRunRequest();

        String result = identityResolutionTools.runIdentityResolution(identityResolutionId, request);

        // Then
        assertThat(result).contains("job-789");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).isEmpty();
    }

    @Test
    void testIdentityResolution_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Identity resolution not found", "/ssot/identity-resolutions/ir-999"));

        // When
        String result = identityResolutionTools.getIdentityResolution("ir-999", null);

        // Then
        assertThat(result).contains("error", "Identity resolution not found", "404");
    }
}
