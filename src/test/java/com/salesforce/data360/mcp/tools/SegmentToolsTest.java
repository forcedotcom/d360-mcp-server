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
import com.salesforce.data360.mcp.model.request.segment.SegmentCreateRequest;
import com.salesforce.data360.mcp.model.request.segment.SegmentDbtInput;
import com.salesforce.data360.mcp.model.request.segment.SegmentDbtModelInput;
import com.salesforce.data360.mcp.model.request.segment.SegmentDbtModelsWrapper;
import com.salesforce.data360.mcp.model.request.segment.SegmentScheduleInput;
import com.salesforce.data360.mcp.model.request.segment.SegmentScheduleTimeInput;
import com.salesforce.data360.mcp.model.request.segment.SegmentUpdateRequest;
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
class SegmentToolsTest {

    @Mock
    private Data360Client client;

    private SegmentTools segmentTools;

    @BeforeEach
    void setUp() {
        segmentTools = new SegmentTools(client);
    }

    @Test
    void testListSegments_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "segments", List.of(
                Map.of("id", "seg-123", "displayName", "High Value Customers")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.listSegments(DEFAULT_DATASPACE, null, null, null, null);

        // Then
        assertThat(result).contains("seg-123", "High Value Customers");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments?dataspace=default");
    }

    @Test
    void testListSegments_noDataspace() {
        // Given
        Map<String, Object> mockResponse = Map.of("segments", List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.listSegments(null, null, null, null, null);

        // Then
        assertThat(result).contains("segments");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments");
    }

    @Test
    void testGetSegment_success() {
        // Given
        String segmentId = "seg-123";
        Map<String, Object> mockResponse = Map.of(
            "id", segmentId,
            "displayName", "High Value Customers",
            "segmentStatus", "ACTIVE"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.getSegment(segmentId, DEFAULT_DATASPACE, null, null, null, null);

        // Then
        assertThat(result).contains(segmentId, "ACTIVE");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments/seg-123?dataspace=default");
    }

    @Test
    void testCreateSegment_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "seg-456",
            "displayName", "Test Segment"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setDisplayName("Test Segment");
        request.setSegmentOnApiName("UnifiedIndividual__dlm");
        request.setSegmentType("Ui");
        request.setDescription("Test Description");

        String result = segmentTools.createSegment(request, DEFAULT_DATASPACE);

        // Then
        assertThat(result).contains("seg-456", "Test Segment");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments?dataspace=default");
        assertThat(bodyCaptor.getValue()).containsEntry("displayName", "Test Segment");
    }

    @Test
    void testUpdateSegment_success() {
        // Given
        String segmentApiName = "My_Segment__seg";
        Map<String, Object> mockResponse = Map.of(
            "apiName", segmentApiName,
            "displayName", "Updated Segment Name"
        );

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        SegmentUpdateRequest request = new SegmentUpdateRequest();
        request.setDisplayName("Updated Segment Name");
        request.setPublishSchedule("TwentyFour");

        String result = segmentTools.updateSegment(segmentApiName, request, DEFAULT_DATASPACE);

        // Then
        assertThat(result).contains("Updated Segment Name");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments/My_Segment__seg?dataspace=default");
        assertThat(bodyCaptor.getValue()).containsEntry("displayName", "Updated Segment Name");
    }

    @Test
    void testDeleteSegment_success() {
        // Given
        String segmentApiName = "High_Value_Customers__seg";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.deleteSegment(segmentApiName, DEFAULT_DATASPACE);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments/High_Value_Customers__seg?dataspace=default");
    }

    @Test
    void testDeactivateSegment_success() {
        // Given
        String segmentApiName = "High_Value_Customers__seg";
        Map<String, Object> mockResponse = Map.of(
            "id", "seg-123",
            "segmentStatus", "INACTIVE"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.deactivateSegment(segmentApiName);

        // Then
        assertThat(result).contains("INACTIVE");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments/High_Value_Customers__seg/actions/deactivate");
    }

    @Test
    void testPublishSegment_success() {
        // Given
        String segmentId = "seg-123";
        Map<String, Object> mockResponse = Map.of(
            "id", segmentId,
            "segmentStatus", "PUBLISHING"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = segmentTools.publishSegment(segmentId);

        // Then
        assertThat(result).contains("PUBLISHING");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/segments/seg-123/actions/publish");
    }

    @Test
    void testCreateSegment_nestedDbtShape() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(Map.of("id", "seg-789"));

        SegmentDbtModelInput model = new SegmentDbtModelInput();
        model.setName("model-1");
        model.setSql("SELECT 1");
        SegmentDbtModelsWrapper wrapper = new SegmentDbtModelsWrapper();
        wrapper.setModels(List.of(model));
        SegmentDbtInput dbt = new SegmentDbtInput();
        dbt.setModels(wrapper);

        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setDisplayName("With Dbt");
        request.setSegmentType("Ui");
        request.setSegmentCreationFlow("Datakit");
        request.setIncludeDbt(dbt);

        segmentTools.createSegment(request, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        Map<String, Object> includeDbt = (Map<String, Object>) body.get("includeDbt");
        assertThat(includeDbt).isNotNull();
        // Server expects doubly-nested: includeDbt.models.models[]
        Map<String, Object> outerModels = (Map<String, Object>) includeDbt.get("models");
        assertThat(outerModels).isNotNull();
        Object innerModels = outerModels.get("models");
        assertThat(innerModels).isInstanceOf(List.class);
        assertThat((List<?>) innerModels).hasSize(1);
        Map<String, Object> first = (Map<String, Object>) ((List<?>) innerModels).get(0);
        assertThat(first).containsEntry("name", "model-1").containsEntry("sql", "SELECT 1");
    }

    @Test
    void testCreateSegment_typedScheduleInfo() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(Map.of("id", "seg-987"));

        SegmentScheduleTimeInput timeInfo = new SegmentScheduleTimeInput();
        timeInfo.setHour(2);
        timeInfo.setMinute(30);
        timeInfo.setTimeZone("America/Los_Angeles");
        SegmentScheduleInput schedule = new SegmentScheduleInput();
        schedule.setDefinitionName("nightly");
        schedule.setFrequency("DAILY");
        schedule.setTimeInfo(timeInfo);

        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setDisplayName("Scheduled");
        request.setPublishScheduleInfo(schedule);

        segmentTools.createSegment(request, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        Map<String, Object> info = (Map<String, Object>) body.get("publishScheduleInfo");
        assertThat(info)
            .containsEntry("definitionName", "nightly")
            .containsEntry("frequency", "DAILY");
        Map<String, Object> time = (Map<String, Object>) info.get("timeInfo");
        assertThat(time)
            .containsEntry("hour", 2)
            .containsEntry("minute", 30)
            .containsEntry("timeZone", "America/Los_Angeles");
    }

    @Test
    void testErrorHandling() {
        // Test GET error (list segments)
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Server error", "/ssot/segments"));

        String listResult = segmentTools.listSegments(DEFAULT_DATASPACE, null, null, null, null);
        assertThat(listResult).contains("error", "Server error", "500");

        // Test POST error (create segment)
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid segment definition", "/ssot/segments"));

        SegmentCreateRequest request = new SegmentCreateRequest();
        request.setDisplayName("Bad Segment");
        request.setSegmentOnApiName("UnifiedIndividual__dlm");
        request.setSegmentType("Ui");
        request.setDescription("Bad Description");

        String createResult = segmentTools.createSegment(request, null);
        assertThat(createResult).contains("error", "Invalid segment definition", "400");
    }
}
