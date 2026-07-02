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
package com.salesforce.data360.mcp.tools.machinelearning.configuredmodel;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.CustomizableFieldInput;
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.ConfiguredModelCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.ConfiguredModelPatchRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.ModelParameterOverrideInput;
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
class ConfiguredModelToolsTest {

    @Mock
    private Data360Client client;

    private ConfiguredModelTools tools;

    @BeforeEach
    void setUp() {
        tools = new ConfiguredModelTools(client);
    }

    private static AssetReferenceInput artifactRef() {
        AssetReferenceInput a = new AssetReferenceInput();
        a.setName("Attrition_Risk_Predictor_v1__mla");
        return a;
    }

    private static ConfiguredModelCreateRequest minimalCreateRequest() {
        ConfiguredModelCreateRequest req = new ConfiguredModelCreateRequest();
        req.setArtifact(artifactRef());
        return req;
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_passesAllQueryFilters() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("configuredModels", List.of()));

        tools.listConfiguredModels(
                "attrition", "Predictive", "BinaryClassification,Regression",
                "EdcNoCode", null,
                "0gjSB00000SkgqXYAR", "ModelArtifact",
                false, "Local",
                25, 0);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        String captured = path.getValue();
        assertThat(captured).startsWith("/ssot/machine-learning/configured-models?");
        assertThat(captured).contains(
                "search=attrition",
                "modelType=Predictive",
                "capabilities=BinaryClassification%2CRegression",
                "sourceType=EdcNoCode",
                "assetIdOrName=0gjSB00000SkgqXYAR",
                "assetType=ModelArtifact",
                "outOfTheBox=false",
                "dataCloudOneVisibility=Local",
                "limit=25",
                "offset=0");
    }

    @Test
    void get_urlEncodesIdOrName() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getConfiguredModel("Attrition Predictor", null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition%20Predictor");
    }

    @Test
    void get_appendsFilterGroup() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getConfiguredModel("Attrition_Predictor", "Small");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition_Predictor?filterGroup=Small");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_buildsPostBody_withArtifactAndOverrides() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
                .thenReturn(Map.of("id", "23PSB00000abcd1"));

        ConfiguredModelCreateRequest req = minimalCreateRequest();
        req.setLabel("Attrition Predictor (Catboost)");
        req.setCapability("BinaryClassification");
        req.setVisibility("Shown");

        ModelParameterOverrideInput override = new ModelParameterOverrideInput();
        override.setParameterName("decisionThreshold");
        override.setContinuousValue(0.5);
        req.setParameterOverrides(List.of(override));

        CustomizableFieldInput af = new CustomizableFieldInput();
        af.setName("Over_Time__c");
        af.setType("ActionableVariable");
        req.setActionableFields(List.of(af));

        tools.createConfiguredModel(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models");

        Map<String, Object> b = body.getValue();
        assertThat(b)
                .containsEntry("label", "Attrition Predictor (Catboost)")
                .containsEntry("capability", "BinaryClassification")
                .containsEntry("visibility", "Shown");

        Map<String, Object> artifact = (Map<String, Object>) b.get("artifact");
        assertThat(artifact).containsEntry("name", "Attrition_Risk_Predictor_v1__mla");

        List<Map<String, Object>> overrides = (List<Map<String, Object>>) b.get("parameterOverrides");
        assertThat(overrides).hasSize(1);
        assertThat(overrides.get(0))
                .containsEntry("parameterName", "decisionThreshold")
                .containsEntry("continuousValue", 0.5);

        List<Map<String, Object>> actionable = (List<Map<String, Object>>) b.get("actionableFields");
        assertThat(actionable).hasSize(1);
        assertThat(actionable.get(0))
                .containsEntry("name", "Over_Time__c")
                .containsEntry("type", "ActionableVariable");
    }

    @Test
    void create_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
                .thenThrow(new ApiException(400, "Invalid modelArtifact input representation",
                        "/ssot/machine-learning/configured-models"));

        String result = tools.createConfiguredModel(minimalCreateRequest());
        assertThat(result).contains("error", "400", "Invalid modelArtifact");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_partialBody_doesNotCarryUnsetFields() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        ConfiguredModelPatchRequest req = new ConfiguredModelPatchRequest();
        req.setDescription("updated");
        req.setStatus("Disabled");

        tools.updateConfiguredModel("Attrition_Predictor", req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition_Predictor");
        assertThat(body.getValue())
                .containsEntry("description", "updated")
                .containsEntry("status", "Disabled")
                .doesNotContainKey("label")
                .doesNotContainKey("capability")
                .doesNotContainKey("visibility");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_parameterOverrides_passesArrayAsIs() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        ModelParameterOverrideInput a = new ModelParameterOverrideInput();
        a.setParameterName("threshold");
        a.setContinuousValue(0.7);
        ModelParameterOverrideInput b = new ModelParameterOverrideInput();
        b.setParameterName("strategy");
        b.setDiscreteValue("balanced");

        ConfiguredModelPatchRequest req = new ConfiguredModelPatchRequest();
        req.setParameterOverrides(List.of(a, b));

        tools.updateConfiguredModel("Attrition_Predictor", req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(anyString(), body.capture(), eq(Map.class));
        List<Map<String, Object>> overrides = (List<Map<String, Object>>) body.getValue().get("parameterOverrides");
        assertThat(overrides).hasSize(2);
        assertThat(overrides.get(0)).containsEntry("parameterName", "threshold").containsEntry("continuousValue", 0.7);
        assertThat(overrides.get(1)).containsEntry("parameterName", "strategy").containsEntry("discreteValue", "balanced");
    }

    @Test
    void delete_callsExpectedPathAndReturnsSuccess() {
        String json = tools.deleteConfiguredModel("Attrition Predictor");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).delete(path.capture());
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition%20Predictor");
        assertThat(json).isEqualTo("{\"success\":true}");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void activate_sendsEnabledStatusOnly() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
                .thenReturn(Map.of("id", "abc", "status", "Enabled"));

        tools.activateConfiguredModel("Attrition_Predictor");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition_Predictor");
        assertThat(body.getValue()).containsExactly(Map.entry("status", "Enabled"));
    }

    @Test
    void historyList_pagination() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("histories", List.of()));

        tools.listHistories("Attrition_Predictor", 10, 5);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition_Predictor/histories?limit=10&offset=5");
    }

    @Test
    void historyGet_pathConstruction() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "13zSB1"));

        tools.getHistory("Attrition_Predictor", "13zSB00000abcd1");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/configured-models/Attrition_Predictor/histories/13zSB00000abcd1");
    }
}
