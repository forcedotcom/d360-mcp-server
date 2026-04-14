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
class SalesforceCrmDataStreamToolsTest {

    @Mock
    private Data360Client client;

    private SalesforceCrmDataStreamTools tools;

    @BeforeEach
    void setUp() {
        tools = new SalesforceCrmDataStreamTools(client);
    }

    @Test
    void testCreateSfdcDataStream_success() {
        Map<String, Object> mockResponse = Map.of("id", "stream-123", "name", "Benefit_Home");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createSfdcDataStream("Benefit", "Home", null, null, null, null, null);

        assertThat(result).contains("stream-123");
        assertThat(result).contains("Benefit_Home");

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(pathCaptor.capture(), bodyCaptor.capture(), eq(Map.class));

        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-streams?dataspace=default");

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("name", "Benefit_Home");
        assertThat(body).containsEntry("label", "Benefit_Home");
        assertThat(body).containsEntry("datastreamType", "SFDC");

        Map<String, Object> connectorInfo = (Map<String, Object>) body.get("connectorInfo");
        assertThat(connectorInfo).containsEntry("connectorType", "SalesforceDotCom");
        Map<String, Object> connectorDetails = (Map<String, Object>) connectorInfo.get("connectorDetails");
        assertThat(connectorDetails).containsEntry("name", "SalesforceDotCom_Home");
        assertThat(connectorDetails).containsEntry("sourceObject", "Benefit");

        Map<String, Object> dloInfo = (Map<String, Object>) body.get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("name", "Benefit_Home");
        assertThat(dloInfo).containsEntry("category", "Other");
    }

    @Test
    void testCreateSfdcDataStream_withEngagementCategory() {
        Map<String, Object> mockResponse = Map.of("id", "stream-456");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createSfdcDataStream("Click", "Home", "Engagement", null, null, null, "EventDateTime__c");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("category", "Engagement");
        assertThat(dloInfo).containsEntry("eventDateTimeFieldName", "EventDateTime__c");
    }

    @Test
    void testCreateSfdcDataStream_withCustomDloName() {
        Map<String, Object> mockResponse = Map.of("id", "stream-789");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createSfdcDataStream("Account", "Home", null, null, "CustomAccountDLO", null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("name", "CustomAccountDLO");
    }

    @Test
    void testCreateSfdcDataStream_withCustomDataStreamName() {
        Map<String, Object> mockResponse = Map.of("id", "stream-name-override");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createSfdcDataStream("Account", "Home", null, "MyCustomStream", null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        assertThat(bodyCaptor.getValue()).containsEntry("name", "MyCustomStream");
        assertThat(bodyCaptor.getValue()).containsEntry("label", "MyCustomStream");
    }

    @Test
    void testCreateSfdcDataStream_withDataspace() {
        Map<String, Object> mockResponse = Map.of("id", "stream-101");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        String result = tools.createSfdcDataStream("Lead", "Home", null, null, null, "custom-dataspace", null);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), any(Map.class), eq(Map.class));
        assertThat(pathCaptor.getValue()).contains("dataspace=custom-dataspace");
    }

    @Test
    void testCreateSfdcDataStream_apiError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/data-streams"));

        String result = tools.createSfdcDataStream("BadObj", "Home", null, null, null, null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("Bad request");
        assertThat(result).contains("400");
    }

    @Test
    void testCreateSfdcDataStream_connectionError() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException("Connection failed", new RuntimeException("timeout")));

        String result = tools.createSfdcDataStream("TimeoutObj", "Home", null, null, null, null, null);

        assertThat(result).contains("Connection failed");
    }

    @Test
    void testDefaultDataspaceInBody() {
        Map<String, Object> mockResponse = Map.of("id", "stream-202");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createSfdcDataStream("Test", "Home", null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> dloInfo = (Map<String, Object>) bodyCaptor.getValue().get("dataLakeObjectInfo");
        List<Map<String, Object>> dataspaceInfo = (List<Map<String, Object>>) dloInfo.get("dataspaceInfo");
        assertThat(dataspaceInfo).hasSize(1);
        assertThat(dataspaceInfo.get(0)).containsEntry("name", "default");
    }

    @Test
    void testCreateSfdcDataStream_engagementWithoutEventDateTimeFieldName_returnsError() {
        String result = tools.createSfdcDataStream("Click", "Home", "Engagement", null, null, null, null);

        assertThat(result).contains("error");
        assertThat(result).contains("eventDateTimeFieldName is required when category is Engagement");
        verifyNoInteractions(client);
    }

    @Test
    void testAutoDerivesNamesFromSourceObjectAndConnection() {
        Map<String, Object> mockResponse = Map.of("id", "stream-auto");
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(mockResponse);

        tools.createSfdcDataStream("Contact", "MyOrg_Alias", null, null, null, null, null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map<String, Object> body = bodyCaptor.getValue();
        assertThat(body).containsEntry("name", "Contact_MyOrg_Alias");
        assertThat(body).containsEntry("label", "Contact_MyOrg_Alias");

        Map<String, Object> dloInfo = (Map<String, Object>) body.get("dataLakeObjectInfo");
        assertThat(dloInfo).containsEntry("name", "Contact_MyOrg_Alias");

        Map<String, Object> connectorInfo = (Map<String, Object>) body.get("connectorInfo");
        Map<String, Object> connectorDetails = (Map<String, Object>) connectorInfo.get("connectorDetails");
        assertThat(connectorDetails).containsEntry("name", "SalesforceDotCom_MyOrg_Alias");
    }
}
