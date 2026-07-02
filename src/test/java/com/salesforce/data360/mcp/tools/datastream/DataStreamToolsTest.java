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
package com.salesforce.data360.mcp.tools.datastream;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamCreateRequest;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamPatchRequest;
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
class DataStreamToolsTest {

    @Mock
    private Data360Client client;

    private DataStreamTools dataStreamTools;

    @BeforeEach
    void setUp() {
        dataStreamTools = new DataStreamTools(client);
    }

    @Test
    void testListDataStreams_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", List.of(
                Map.of("name", "AccountStream", "category", "profile"),
                Map.of("name", "ClickStream", "category", "engagement")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataStreamTools.listDataStreams(null, null, null, null, null, null, null);

        // Then
        assertThat(result).contains("AccountStream");
        assertThat(result).contains("ClickStream");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams");
    }

    @Test
    void testListDataStreams_withFilters() {
        // Given
        Map<String, Object> mockResponse = Map.of("data", List.of());

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataStreamTools.listDataStreams("myConnection", null, null, null, null, null, "Account");

        // Then
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue())
            .contains("connectionName=myConnection")
            .contains("sourceObjectName=Account");
    }

    @Test
    void testGetDataStream_success() {
        // Given
        String dataStreamId = "MyStream";
        Map<String, Object> mockResponse = Map.of(
            "name", "MyStream",
            "sourceObjectName", "Account",
            "targetObjectName", "Account__dlm",
            "category", "profile"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataStreamTools.getDataStream(dataStreamId, null);

        // Then
        assertThat(result).contains("MyStream");
        assertThat(result).contains("Account");
        assertThat(result).contains("Account__dlm");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams/" + dataStreamId);
    }

    @Test
    void testCreateDataStream_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "id", "stream-123",
            "name", "NewStream"
        );

        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamCreateRequest request = new DataStreamCreateRequest();
        request.setName("NewStream");
        request.setLabel("New Stream Label");
        request.setDatasource("LeadSource");
        request.setDatastreamType("lightning");

        // When
        String result = dataStreamTools.createDataStream(request);

        // Then
        assertThat(result).contains("stream-123");
        assertThat(result).contains("NewStream");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams");
        assertThat(bodyCaptor.getValue())
            .containsEntry("name", "NewStream")
            .containsEntry("label", "New Stream Label")
            .containsEntry("datasource", "LeadSource")
            .containsEntry("datastreamType", "lightning");
    }

    @Test
    void testUpdateDataStream_success() {
        // Given
        String dataStreamId = "MyStream";
        Map<String, Object> mockResponse = Map.of(
            "name", "MyStream",
            "label", "Updated Label"
        );

        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamPatchRequest request = new DataStreamPatchRequest();
        request.setLabel("Updated Label");

        // When
        String result = dataStreamTools.updateDataStream(dataStreamId, request);

        // Then
        assertThat(result).contains("Updated Label");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams/" + dataStreamId);
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Updated Label");
    }

    @Test
    void testDeleteDataStream_success() {
        // Given
        String dataStreamId = "MyStream";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataStreamTools.deleteDataStream(dataStreamId, null);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams/" + dataStreamId);
    }

    @Test
    void testRunDataStream_success() {
        // Given
        String dataStreamId = "MyStream";
        Map<String, Object> mockResponse = Map.of(
            "jobId", "job-456",
            "status", "running"
        );

        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataStreamTools.runDataStream(dataStreamId, null);

        // Then
        assertThat(result).contains("job-456");
        assertThat(result).contains("running");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams/" + dataStreamId + "/actions/run");
    }

    @Test
    void testListDataStreams_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Internal server error", "/ssot/data-streams"));

        // When
        String result = dataStreamTools.listDataStreams(null, null, null, null, null, null, null);

        // Then
        assertThat(result).contains("error");
        assertThat(result).contains("Internal server error");
        assertThat(result).contains("500");
    }

    @Test
    void testGetDataStream_notFound() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Data stream not found", "/ssot/data-streams/NonExistent"));

        // When
        String result = dataStreamTools.getDataStream("NonExistent", null);

        // Then
        assertThat(result).contains("error");
        assertThat(result).contains("Data stream not found");
        assertThat(result).contains("404");
    }

    @Test
    void testCreateDataStream_returnsId() {
        // Given
        Map<String, Object> mockResponse = Map.of("id", "stream-789");

        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        DataStreamCreateRequest request = new DataStreamCreateRequest();
        request.setName("NewStream");
        request.setLabel("New Stream");
        request.setDatasource("src");
        request.setDatastreamType("lightning");

        // When
        String result = dataStreamTools.createDataStream(request);

        // Then
        assertThat(result).contains("stream-789");
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams");
    }

    @Test
    void testCreateDataStream_connectionErrorIsNotReportedAsInvalidJson() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException("Data 360 API connection error on /data-streams", new RuntimeException("timeout")));

        DataStreamCreateRequest request = new DataStreamCreateRequest();
        request.setName("NewStream");
        request.setLabel("New Stream");
        request.setDatasource("src");
        request.setDatastreamType("lightning");

        String result = dataStreamTools.createDataStream(request);

        assertThat(result).contains("Data 360 API connection error");
        assertThat(result).doesNotContain("Invalid JSON body");
    }
}
