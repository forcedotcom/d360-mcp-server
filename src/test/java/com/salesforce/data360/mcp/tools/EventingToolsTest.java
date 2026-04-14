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
import com.salesforce.data360.mcp.model.request.eventing.EventBatchPublishRequest;
import com.salesforce.data360.mcp.model.request.eventing.EventPublishRequest;
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
class EventingToolsTest {

    @Mock
    private Data360Client client;

    private EventingTools eventingTools;

    @BeforeEach
    void setUp() {
        eventingTools = new EventingTools(client);
    }

    @Test
    void testPublishEvent_success() {
        // Given
        String schema = "clickEvent";
        String payload = "{\"userId\":\"user123\",\"action\":\"click\"}";
        String traceId = "trace-123";

        Map<String, Object> mockResponse = Map.of(
            "eventId", "event-456",
            "status", "accepted"
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        EventPublishRequest request = new EventPublishRequest();
        request.setSchema(schema);
        request.setPayload(payload);
        request.setTraceId(traceId);
        String result = eventingTools.publishEvent(request);

        // Then
        assertThat(result).contains("event-456", "accepted");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/a360/event");
        assertThat(bodyCaptor.getValue())
            .containsEntry("schema", schema)
            .containsEntry("traceId", traceId);
        assertThat(bodyCaptor.getValue().get("payload")).isInstanceOf(Map.class);
    }

    @Test
    void testPublishEvent_withoutTraceId() {
        // Given
        String schema = "clickEvent";
        String payload = "{\"userId\":\"user123\"}";

        Map<String, Object> mockResponse = Map.of("eventId", "event-789");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        EventPublishRequest request = new EventPublishRequest();
        request.setSchema(schema);
        request.setPayload(payload);
        String result = eventingTools.publishEvent(request);

        // Then
        assertThat(result).contains("event-789");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).doesNotContainKey("traceId");
    }

    @Test
    void testPublishEvent_nullBodyIsTreatedAsSuccess() {
        // Given
        String schema = "clickEvent";
        String payload = "{\"userId\":\"user123\"}";

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(null);

        // When
        EventPublishRequest request = new EventPublishRequest();
        request.setSchema(schema);
        request.setPayload(payload);
        String result = eventingTools.publishEvent(request);

        // Then
        assertThat(result).contains("success", "true");
    }

    @Test
    void testPublishEvent_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid event schema", "/a360/event"));

        // When
        EventPublishRequest request = new EventPublishRequest();
        request.setSchema("invalidSchema");
        request.setPayload("{}");
        String result = eventingTools.publishEvent(request);

        // Then
        assertThat(result).contains("error", "Invalid event schema", "400");
    }

    @Test
    void testPublishBatchEvents_success() {
        // Given
        String events = "[{\"userId\":\"user1\"},{\"userId\":\"user2\"}]";
        String schemas = "[{\"name\":\"clickEvent\"},{\"name\":\"clickEvent\"}]";
        String schemaVersion = "1.0";

        Map<String, Object> mockResponse = Map.of(
            "batchId", "batch-123",
            "accepted", 2
        );

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        EventBatchPublishRequest request = new EventBatchPublishRequest();
        request.setEvents(events);
        request.setSchemas(schemas);
        request.setSchemaVersion(schemaVersion);
        String result = eventingTools.publishBatchEvents(request);

        // Then
        assertThat(result).contains("batch-123", "accepted");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/a360/events");
        assertThat(bodyCaptor.getValue())
            .containsEntry("count", 2)
            .containsEntry("schemaVersion", schemaVersion)
            .containsKey("creationDateTime");
        assertThat(bodyCaptor.getValue().get("events")).isInstanceOf(List.class);
        assertThat(bodyCaptor.getValue().get("schemas")).isInstanceOf(List.class);
    }

    @Test
    void testPublishBatchEvents_withoutSchemaVersion() {
        // Given
        String events = "[{\"userId\":\"user1\"}]";
        String schemas = "[{\"name\":\"clickEvent\"}]";

        Map<String, Object> mockResponse = Map.of("batchId", "batch-456");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        EventBatchPublishRequest request = new EventBatchPublishRequest();
        request.setEvents(events);
        request.setSchemas(schemas);
        String result = eventingTools.publishBatchEvents(request);

        // Then
        assertThat(result).contains("batch-456");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).doesNotContainKey("schemaVersion");
        assertThat(bodyCaptor.getValue()).containsEntry("count", 1);
    }

    @Test
    void testPublishBatchEvents_nullBodyIsTreatedAsSuccess() {
        // Given
        String events = "[{\"userId\":\"user1\"}]";
        String schemas = "[{\"name\":\"clickEvent\"}]";

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(null);

        // When
        EventBatchPublishRequest request = new EventBatchPublishRequest();
        request.setEvents(events);
        request.setSchemas(schemas);
        String result = eventingTools.publishBatchEvents(request);

        // Then
        assertThat(result).contains("success", "count");
    }

    @Test
    void testPublishBatchEvents_errorHandling() {
        // Given
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Invalid batch format", "/a360/events"));

        // When
        EventBatchPublishRequest request = new EventBatchPublishRequest();
        request.setEvents("[]");
        request.setSchemas("[]");
        String result = eventingTools.publishBatchEvents(request);

        // Then
        assertThat(result).contains("error", "Invalid batch format", "400");
    }
}
