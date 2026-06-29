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
package com.salesforce.data360.mcp.tools.machinelearning.modelartifact;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.modelartifact.ModelArtifactPatchRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.modelartifact.ModelFeatureOverrideValueInput;
import com.salesforce.data360.mcp.model.request.machinelearning.modelartifact.ModelOutputFieldOverrideInput;
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
class ModelArtifactToolsTest {

    @Mock
    private Data360Client client;

    private ModelArtifactTools tools;

    @BeforeEach
    void setUp() {
        tools = new ModelArtifactTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_passesAllQueryFilters() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("modelArtifacts", List.of()));

        tools.listModelArtifacts("Predictive", "EdcNoCode", "Local", 25, 0);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        String captured = path.getValue();
        assertThat(captured).startsWith("/ssot/machine-learning/model-artifacts?");
        assertThat(captured).contains(
                "modelType=Predictive",
                "sourceType=EdcNoCode",
                "dataCloudOneVisibility=Local",
                "limit=25",
                "offset=0");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_omitsUnsetFilters() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("modelArtifacts", List.of()));

        tools.listModelArtifacts(null, null, null, null, null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-artifacts");
    }

    @Test
    void get_urlEncodesIdOrName() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getModelArtifact("Attrition Predictor", null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-artifacts/Attrition%20Predictor");
    }

    @Test
    void get_appendsFilterGroup() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getModelArtifact("Attrition_Predictor", "Small");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-artifacts/Attrition_Predictor?filterGroup=Small");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_partialBody_doesNotCarryUnsetFields() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        ModelArtifactPatchRequest req = new ModelArtifactPatchRequest();
        req.setDescription("retired");
        req.setStatus("Disabled");

        tools.updateModelArtifact("Attrition_Predictor", req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-artifacts/Attrition_Predictor");
        assertThat(body.getValue())
                .containsEntry("description", "retired")
                .containsEntry("status", "Disabled")
                .doesNotContainKey("label")
                .doesNotContainKey("outputFields");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_outputFieldsOverrides_passesArrayAsIs() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        ModelFeatureOverrideValueInput v0 = new ModelFeatureOverrideValueInput();
        v0.setSourceValue("Cluster 0");
        v0.setOverrideValue("High-Risk Segment");
        ModelFeatureOverrideValueInput v1 = new ModelFeatureOverrideValueInput();
        v1.setSourceValue("Cluster 1");
        v1.setOverrideValue("Stable Segment");

        ModelOutputFieldOverrideInput field = new ModelOutputFieldOverrideInput();
        field.setId("0jrSB00000ABC123YAA");
        field.setFeatureOverrideValues(List.of(v0, v1));

        ModelArtifactPatchRequest req = new ModelArtifactPatchRequest();
        req.setOutputFields(List.of(field));

        tools.updateModelArtifact("Attrition_Predictor", req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(anyString(), body.capture(), eq(Map.class));
        List<Map<String, Object>> outputFields = (List<Map<String, Object>>) body.getValue().get("outputFields");
        assertThat(outputFields).hasSize(1);
        assertThat(outputFields.get(0)).containsEntry("id", "0jrSB00000ABC123YAA");

        List<Map<String, Object>> overrides = (List<Map<String, Object>>) outputFields.get(0).get("featureOverrideValues");
        assertThat(overrides).hasSize(2);
        assertThat(overrides.get(0))
                .containsEntry("sourceValue", "Cluster 0")
                .containsEntry("overrideValue", "High-Risk Segment");
        assertThat(overrides.get(1))
                .containsEntry("sourceValue", "Cluster 1")
                .containsEntry("overrideValue", "Stable Segment");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_apiError_returnsStructuredJson() {
        when(client.patch(anyString(), any(), eq(Map.class)))
                .thenThrow(new ApiException(400, "Update is not allowed for Salesforce enabled model artifact",
                        "/ssot/machine-learning/model-artifacts/SomeOOTBModel"));

        ModelArtifactPatchRequest req = new ModelArtifactPatchRequest();
        req.setDescription("anything");

        Map<String, Object> parsed = JsonUtil.fromJson(
                tools.updateModelArtifact("SomeOOTBModel", req), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 400)
                .containsEntry("path", "/ssot/machine-learning/model-artifacts/SomeOOTBModel");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 400")
                .contains("Update is not allowed for Salesforce enabled model artifact");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete_callsExpectedPath() {
        when(client.delete(anyString(), eq(Map.class))).thenReturn(Map.of());

        tools.deleteModelArtifact("Attrition Predictor");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).delete(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-artifacts/Attrition%20Predictor");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_apiError_returnsStructuredJson() {
        when(client.get(anyString(), eq(Map.class)))
                .thenThrow(new ApiException(500, "[{\"errorCode\":\"UNKNOWN_EXCEPTION\"}]",
                        "/ssot/machine-learning/model-artifacts"));

        Map<String, Object> parsed = JsonUtil.fromJson(
                tools.listModelArtifacts(null, null, null, null, null), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 500)
                .containsEntry("path", "/ssot/machine-learning/model-artifacts");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 500")
                .contains("UNKNOWN_EXCEPTION");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void get_apiError_returnsStructuredJson() {
        when(client.get(anyString(), eq(Map.class)))
                .thenThrow(new ApiException(404, "[{\"errorCode\":\"ITEM_NOT_FOUND\"}]",
                        "/ssot/machine-learning/model-artifacts/missing"));

        Map<String, Object> parsed = JsonUtil.fromJson(
                tools.getModelArtifact("missing", null), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 404)
                .containsEntry("path", "/ssot/machine-learning/model-artifacts/missing");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 404")
                .contains("ITEM_NOT_FOUND");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete_apiError_returnsStructuredJson() {
        when(client.delete(anyString(), eq(Map.class)))
                .thenThrow(new ApiException(400, "Deletion is not allowed for Salesforce enabled foundational model",
                        "/ssot/machine-learning/model-artifacts/SomeOOTBModel"));

        Map<String, Object> parsed = JsonUtil.fromJson(
                tools.deleteModelArtifact("SomeOOTBModel"), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 400)
                .containsEntry("path", "/ssot/machine-learning/model-artifacts/SomeOOTBModel");
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 400")
                .contains("Deletion is not allowed");
    }

}
