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
import com.salesforce.data360.mcp.model.request.activation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationToolsTest {

    @Mock
    private Data360Client client;

    private ActivationTools activationTools;

    @BeforeEach
    void setUp() {
        activationTools = new ActivationTools(client);
    }

    // ========================================================================
    // ACTIVATION TESTS
    // ========================================================================

    @Test
    void testListActivations_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "activations", List.of(
                Map.of("id", "act-123", "name", "Send to Salesforce")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = activationTools.listActivations(null, null, null, null);

        // Then
        assertThat(result).contains("act-123", "Send to Salesforce");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activations");
    }

    @Test
    void testGetActivation_success() {
        // Given
        String activationId = "act-123";
        Map<String, Object> mockResponse = Map.of(
            "id", activationId,
            "name", "Send to Salesforce",
            "segmentId", "seg-456"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = activationTools.getActivation(activationId);

        // Then
        assertThat(result).contains(activationId, "Send to Salesforce");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activations/act-123");
    }

    @Test
    void testCreateActivation_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "act-789",
            "name", "Send High Value to Salesforce"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        ActivationCreateRequest request = new ActivationCreateRequest();
        request.setName("Send High Value to Salesforce");
        request.setRefreshType("FULL");
        request.setDataSpaceName("default");
        request.setActivationTargetName("target-456");
        request.setSegmentApiName("seg-123");
        request.setLimitValue(1000);
        CuratedEntityInput curatedEntity = new CuratedEntityInput();
        curatedEntity.setName("Individual");
        curatedEntity.setLabel("Individual");
        request.setCuratedEntity(curatedEntity);
        ActivationTargetSubjectConfigInput subjectConfig = new ActivationTargetSubjectConfigInput();
        subjectConfig.setDeveloperName("ContactPointEmail");
        request.setActivationTargetSubjectConfig(subjectConfig);
        request.setAttributeLimitingExpressionConfig(java.util.List.of());

        // When
        String result = activationTools.createActivation(request);

        // Then
        assertThat(result).contains("act-789", "Send High Value to Salesforce");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activations");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "Send High Value to Salesforce");
        assertThat(bodyCaptor.getValue()).containsEntry("activationTargetName", "target-456");
        assertThat(bodyCaptor.getValue()).containsEntry("segmentApiName", "seg-123");
        assertThat(bodyCaptor.getValue()).containsEntry("refreshType", "FULL");
    }

    @Test
    void testUpdateActivation_success() {
        // Given
        String activationId = "act-123";
        Map<String, Object> mockResponse = Map.of(
            "id", activationId,
            "refreshType", "INCREMENTAL"
        );

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        ActivationUpdateRequest request = new ActivationUpdateRequest();
        request.setRefreshType("INCREMENTAL");

        // When
        String result = activationTools.updateActivation(activationId, request);

        // Then
        assertThat(result).contains("INCREMENTAL");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activations/act-123");
        assertThat(bodyCaptor.getValue()).containsEntry("refreshType", "INCREMENTAL");
    }

    @Test
    void testDeleteActivation_success() {
        // Given
        String activationId = "act-123";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = activationTools.deleteActivation(activationId);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activations/act-123");
    }

    // ========================================================================
    // ACTIVATION TARGET TESTS
    // ========================================================================

    @Test
    void testListActivationTargets_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "targets", List.of(
                Map.of("id", "target-123", "name", "Salesforce Production")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = activationTools.listActivationTargets(null, null, null, null);

        // Then
        assertThat(result).contains("target-123", "Salesforce Production");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activation-targets");
    }

    @Test
    void testGetActivationTarget_success() {
        // Given
        String targetId = "target-123";
        Map<String, Object> mockResponse = Map.of(
            "id", targetId,
            "name", "Salesforce Production",
            "targetType", "SALESFORCE"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = activationTools.getActivationTarget(targetId);

        // Then
        assertThat(result).contains(targetId, "Salesforce Production");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activation-targets/target-123");
    }

    @Test
    void testCreateActivationTarget_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "target-456",
            "name", "Salesforce Production"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        ConnectorInput connector = new ConnectorInput();
        connector.setName("myConnector");
        connector.setOutputFormat("CSV");

        ActivationTargetCreateRequest request = new ActivationTargetCreateRequest();
        request.setDataSpaceName("default");
        request.setName("Salesforce Production");
        request.setConnector(connector);
        request.setPlatformType("SALESFORCE");
        request.setDescription("Production target");
        request.setIsCappingEnabled(false);

        // When
        String result = activationTools.createActivationTarget(request);

        // Then
        assertThat(result).contains("target-456", "Salesforce Production");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activation-targets");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "Salesforce Production");
        assertThat(bodyCaptor.getValue()).containsEntry("dataSpaceName", "default");
        assertThat(bodyCaptor.getValue()).containsEntry("platformType", "SALESFORCE");
        assertThat(bodyCaptor.getValue()).containsKey("connector");
    }

    @Test
    void testUpdateActivationTarget_success() {
        // Given
        String targetId = "target-123";
        Map<String, Object> mockResponse = Map.of(
            "id", targetId,
            "name", "Updated Target Name"
        );

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        ActivationTargetUpdateRequest request = new ActivationTargetUpdateRequest();
        request.setName("Updated Target Name");

        // When
        String result = activationTools.updateActivationTarget(targetId, request);

        // Then
        assertThat(result).contains("Updated Target Name");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/activation-targets/target-123");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "Updated Target Name");
    }


    // ========================================================================
    // ERROR HANDLING TESTS
    // ========================================================================

    @Test
    void testListActivations_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Server error", "/ssot/activations"));

        // When
        String result = activationTools.listActivations(null, null, null, null);

        // Then
        assertThat(result).contains("error", "Server error", "500");
    }

    @Test
    void testCreateActivation_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid activation definition", "/ssot/activations"));

        ActivationCreateRequest request = new ActivationCreateRequest();
        request.setName("My Act");
        request.setRefreshType("FULL");
        request.setDataSpaceName("default");
        request.setActivationTargetName("target-1");
        request.setSegmentApiName("seg-1");
        request.setLimitValue(1000);
        CuratedEntityInput curatedEntity = new CuratedEntityInput();
        curatedEntity.setName("Individual");
        curatedEntity.setLabel("Individual");
        request.setCuratedEntity(curatedEntity);
        request.setActivationTargetSubjectConfig(new ActivationTargetSubjectConfigInput());
        request.setAttributeLimitingExpressionConfig(java.util.List.of());

        // When
        String result = activationTools.createActivation(request);

        // Then
        assertThat(result).contains("error", "Invalid activation definition", "400");
    }
}
