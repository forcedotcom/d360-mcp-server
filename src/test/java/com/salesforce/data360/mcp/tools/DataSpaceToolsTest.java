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
import com.salesforce.data360.mcp.model.request.dataspace.*;
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
class DataSpaceToolsTest {

    @Mock
    private Data360Client client;

    private DataSpaceTools dataSpaceTools;

    @BeforeEach
    void setUp() {
        dataSpaceTools = new DataSpaceTools(client);
    }

    @Test
    void testListDataSpaces_success() {
        // Given
        Map<String, Object> mockResponse = Map.of(
            "data", java.util.List.of(
                Map.of("name", "marketing_dataspace", "description", "Marketing data")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataSpaceTools.listDataSpaces(null, null, null);

        // Then
        assertThat(result).contains("marketing_dataspace", "Marketing data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces");
    }

    @Test
    void testGetDataSpace_success() {
        // Given
        String dataSpaceName = "marketing_dataspace";
        Map<String, Object> mockResponse = Map.of(
            "name", dataSpaceName,
            "description", "Marketing data models",
            "type", "STANDARD"
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataSpaceTools.getDataSpace(dataSpaceName);

        // Then
        assertThat(result).contains(dataSpaceName, "Marketing data models");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces/marketing_dataspace");
    }

    @Test
    void testCreateDataSpace_success() {
        // Given
        Map<String, Object> mockResponse = Map.of("name", "sales_dataspace", "description", "Sales data");

        when(client.post(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataSpaceCreateRequest request = new DataSpaceCreateRequest();
        request.setName("sales_dataspace");
        request.setLabel("Sales Dataspace");
        request.setDescription("Sales data");

        // When
        String result = dataSpaceTools.createDataSpace(request);

        // Then
        assertThat(result).contains("sales_dataspace", "Sales data");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces");
        assertThat(bodyCaptor.getValue()).containsEntry("name", "sales_dataspace");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "Sales Dataspace");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Sales data");
    }

    @Test
    void testUpdateDataSpace_success() {
        // Given
        String dataSpaceName = "marketing_dataspace";
        Map<String, Object> mockResponse = Map.of("name", dataSpaceName, "description", "Updated marketing description");

        when(client.patch(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataSpacePatchRequest request = new DataSpacePatchRequest();
        request.setDescription("Updated marketing description");

        // When
        String result = dataSpaceTools.updateDataSpace(dataSpaceName, request);

        // Then
        assertThat(result).contains(dataSpaceName, "Updated marketing description");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces/marketing_dataspace");
        assertThat(bodyCaptor.getValue()).containsEntry("description", "Updated marketing description");
        assertThat(bodyCaptor.getValue()).doesNotContainKey("label");
    }

    @Test
    void testListDataSpaceMembers_success() {
        // Given
        String dataSpaceName = "marketing_dataspace";
        Map<String, Object> mockResponse = Map.of(
            "members", java.util.List.of(
                Map.of("memberId", "user-123", "role", "ADMIN")
            )
        );

        when(client.get(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataSpaceTools.listDataSpaceMembers(dataSpaceName, null, null, null);

        // Then
        assertThat(result).contains("user-123", "ADMIN");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).get(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces/marketing_dataspace/members");
    }

    @Test
    void testAddDataSpaceMember_success() {
        // Given
        String dataSpaceName = "marketing_dataspace";
        Map<String, Object> mockResponse = Map.of("memberName", "user-456", "status", "ACTIVE");

        when(client.put(anyString(), any(), eq(Map.class)))
            .thenReturn(mockResponse);

        DataSpaceMemberInput memberInput = new DataSpaceMemberInput();
        memberInput.setMemberName("user-456");

        DataSpaceMemberRequest request = new DataSpaceMemberRequest();
        request.setMembers(List.of(memberInput));

        // When
        String result = dataSpaceTools.addDataSpaceMember(dataSpaceName, request);

        // Then
        assertThat(result).contains("user-456");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).put(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces/marketing_dataspace/members");
        assertThat(bodyCaptor.getValue()).containsKey("members");
    }

    @Test
    void testRemoveDataSpaceMember_success() {
        // Given
        String dataSpaceName = "marketing_dataspace";
        String memberNames = "user-789";
        Map<String, Object> mockResponse = Map.of("success", true);

        when(client.delete(anyString(), eq(Map.class)))
            .thenReturn(mockResponse);

        // When
        String result = dataSpaceTools.removeDataSpaceMember(dataSpaceName, memberNames);

        // Then
        assertThat(result).contains("success");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).delete(pathCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-spaces/marketing_dataspace/members?memberNames=user-789");
    }

    @Test
    void testDataSpace_errorHandling() {
        // Given
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Data space not found", "/ssot/data-spaces/unknown"));

        // When
        String result = dataSpaceTools.getDataSpace("unknown");

        // Then
        assertThat(result).contains("error", "Data space not found", "404");
    }
}
