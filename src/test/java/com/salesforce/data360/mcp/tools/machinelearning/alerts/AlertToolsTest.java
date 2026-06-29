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
package com.salesforce.data360.mcp.tools.machinelearning.alerts;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.alerts.AlertQueryRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertToolsTest {

    @Mock
    private Data360Client client;

    private AlertTools tools;

    @BeforeEach
    void setUp() {
        tools = new AlertTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void query_passesFlatBodyAtTopLevel() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("alerts", List.of()));

        AlertQueryRequest req = new AlertQueryRequest();
        req.setAsset("model_artifact_label_MA_12l_t1d4de5c954");
        req.setAssetType("ModelArtifact");
        req.setSourceTypes(List.of("ModelTraining"));

        tools.queryAlerts(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/alerts");
        assertThat(body.getValue())
                .containsEntry("asset", "model_artifact_label_MA_12l_t1d4de5c954")
                .containsEntry("assetType", "ModelArtifact")
                .containsEntry("sourceTypes", List.of("ModelTraining"))
                .doesNotContainKey("query");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void query_omitsSourceTypesWhenNull() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("alerts", List.of()));

        AlertQueryRequest req = new AlertQueryRequest();
        req.setAsset("artifact1");
        req.setAssetType("ModelArtifact");

        tools.queryAlerts(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        assertThat(body.getValue()).doesNotContainKey("sourceTypes");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void query_returnsParsedResponseFromCreatedStatus() {
        // Data360Client.post is 2xx-tolerant — RestClient.body() handles 201 the same as 200.
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of(
                "alerts", List.of(Map.of("id", "24p", "alertType", "ObviousPredictor")),
                "totalSize", 1));

        AlertQueryRequest req = new AlertQueryRequest();
        req.setAsset("artifact1");
        req.setAssetType("ModelArtifact");

        String json = tools.queryAlerts(req);
        assertThat(json).contains("\"alertType\":\"ObviousPredictor\"");
        assertThat(json).contains("\"totalSize\":1");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void query_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
                .thenThrow(new ApiException(404, "[{\"errorCode\":\"ITEM_NOT_FOUND\"}]",
                        "/ssot/machine-learning/alerts"));

        AlertQueryRequest req = new AlertQueryRequest();
        req.setAsset("missing");
        req.setAssetType("ModelArtifact");

        Map<String, Object> parsed = JsonUtil.fromJson(tools.queryAlerts(req), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 404)
                .containsEntry("path", "/ssot/machine-learning/alerts");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 404")
                .contains("ITEM_NOT_FOUND");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_passesOnlySetBooleans() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "24p"));

        tools.updateAlert("24pSB0000005W0rYAE", true, null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/alerts/24pSB0000005W0rYAE");
        assertThat(body.getValue())
                .containsEntry("accepted", true)
                .doesNotContainKey("dismissed");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_passesDismissedOnly() {
        // Per server contract, callers should send only one of accepted/dismissed per call.
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "24p"));

        tools.updateAlert("24p", null, true);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(anyString(), body.capture(), eq(Map.class));
        assertThat(body.getValue())
                .containsEntry("dismissed", true)
                .doesNotContainKey("accepted");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_urlEncodesAlertId() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "x"));

        tools.updateAlert("alert with space", true, null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).patch(path.capture(), any(Map.class), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/alerts/alert%20with%20space");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_apiError_returnsStructuredJson() {
        when(client.patch(anyString(), any(), eq(Map.class)))
                .thenThrow(new ApiException(404, "[{\"errorCode\":\"ITEM_NOT_FOUND\"}]",
                        "/ssot/machine-learning/alerts/missing"));

        Map<String, Object> parsed = JsonUtil.fromJson(tools.updateAlert("missing", true, null), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 404)
                .containsEntry("path", "/ssot/machine-learning/alerts/missing");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 404")
                .contains("ITEM_NOT_FOUND");
    }
}
