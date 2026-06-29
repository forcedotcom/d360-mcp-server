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
package com.salesforce.data360.mcp.tools.machinelearning.setup;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.BucketingStrategyInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.FieldConfigInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.ModelSetupCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.ModelSetupPatchRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.OutcomeInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.SetupVersionCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.TransformationInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.InputSourceInput;
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
class ModelSetupToolsTest {

    @Mock
    private Data360Client client;

    private ModelSetupTools tools;

    @BeforeEach
    void setUp() {
        tools = new ModelSetupTools(client);
    }

    private static InputSourceInput dmoInput(String dmoName) {
        AssetReferenceInput src = new AssetReferenceInput();
        src.setName(dmoName);
        AssetReferenceInput ds = new AssetReferenceInput();
        ds.setName("default");
        InputSourceInput in = new InputSourceInput();
        in.setType("DataModelObject");
        in.setSource(src);
        in.setDataSpace(ds);
        return in;
    }

    private static OutcomeInput binaryOutcome() {
        OutcomeInput o = new OutcomeInput();
        o.setType("Binary");
        o.setName("Attrition__c");
        o.setLabel("Attrition");
        o.setSuccessValue("Yes");
        o.setFailureValue("No");
        return o;
    }

    private static FieldConfigInput numberField(String name) {
        FieldConfigInput f = new FieldConfigInput();
        f.setType("Number");
        f.setName(name);
        return f;
    }

    private static SetupVersionCreateRequest minimalVersionRequest() {
        SetupVersionCreateRequest req = new SetupVersionCreateRequest();
        req.setInput(dmoInput("Attrition__dlm"));
        req.setOutcomes(List.of(binaryOutcome()));
        req.setFields(List.of(numberField("Age__c"), numberField("Monthly_Income__c"), numberField("Years_At_Company__c")));
        return req;
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void list_passesQueryFilters() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("modelSetups", List.of()));

        tools.listModelSetups("attrition", "Predictive", "BinaryClassification", "EdcNoCode", null, 50, 0);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        String captured = path.getValue();
        assertThat(captured).startsWith("/ssot/machine-learning/model-setups?");
        assertThat(captured).contains("search=attrition", "modelType=Predictive", "modelCapability=BinaryClassification", "setupType=EdcNoCode", "limit=50", "offset=0");
    }

    @Test
    void get_urlEncodesIdOrName() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getModelSetup("Attrition Predictor");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition%20Predictor");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_postsContainerBody() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "24sX1234567"));

        ModelSetupCreateRequest req = new ModelSetupCreateRequest();
        req.setLabel("Attrition Risk Predictor");
        req.setModelType("Predictive");
        req.setModelCapability("BinaryClassification");
        req.setSetupType("EdcNoCode");

        String result = tools.createModelSetup(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups");
        assertThat(body.getValue())
            .containsEntry("label", "Attrition Risk Predictor")
            .containsEntry("modelType", "Predictive")
            .containsEntry("modelCapability", "BinaryClassification")
            .containsEntry("setupType", "EdcNoCode");
        assertThat(result).contains("24sX1234567");
    }

    @Test
    void create_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Bad request", "/ssot/machine-learning/model-setups"));

        ModelSetupCreateRequest req = new ModelSetupCreateRequest();
        req.setLabel("X");
        req.setModelType("Predictive");
        req.setModelCapability("BinaryClassification");
        req.setSetupType("EdcNoCode");

        String result = tools.createModelSetup(req);
        assertThat(result).contains("error", "400", "/ssot/machine-learning/model-setups");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_partialBody() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        ModelSetupPatchRequest req = new ModelSetupPatchRequest();
        req.setDescription("updated");

        tools.updateModelSetup("Attrition_Risk_Predictor", req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor");
        assertThat(body.getValue()).containsEntry("description", "updated").doesNotContainKey("label");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void delete_callsExpectedPath() {
        when(client.delete(anyString(), eq(Map.class))).thenReturn(Map.of());

        tools.deleteModelSetup("Attrition_Risk_Predictor");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).delete(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor");
    }

    @Test
    void versionList_buildsExpectedPath() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("setupVersions", List.of()));

        tools.listSetupVersions("Attrition_Risk_Predictor", 25, 0);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor/setup-versions?limit=25&offset=0");
    }

    @Test
    void versionGet_buildsExpectedPath() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "14kX1"));

        tools.getSetupVersion("Attrition_Risk_Predictor", "14kX1");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor/setup-versions/14kX1");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void versionCreate_addsTypeAndWrapsAlgorithmType() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14kX1", "status", "Training"));

        SetupVersionCreateRequest req = minimalVersionRequest();
        req.setAlgorithmType("Catboost");

        tools.createSetupVersion("Attrition_Risk_Predictor", req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor/setup-versions");

        Map<String, Object> b = body.getValue();
        assertThat(b).containsEntry("type", "EdcNoCode");
        assertThat(b).doesNotContainKey("algorithmType");
        Map<String, Object> modelConfig = (Map<String, Object>) b.get("modelConfiguration");
        assertThat(modelConfig).containsEntry("algorithmType", "Catboost");

        Map<String, Object> input = (Map<String, Object>) b.get("input");
        assertThat(input).containsEntry("type", "DataModelObject");
        List<Map<String, Object>> outcomes = (List<Map<String, Object>>) b.get("outcomes");
        assertThat(outcomes).hasSize(1);
        assertThat(outcomes.get(0))
            .containsEntry("type", "Binary")
            .containsEntry("successValue", "Yes")
            .containsEntry("failureValue", "No");
        List<Map<String, Object>> fields = (List<Map<String, Object>>) b.get("fields");
        assertThat(fields).hasSize(3);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void versionCreate_appliesAlgorithmDefault_binary() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14kX1"));

        SetupVersionCreateRequest req = minimalVersionRequest(); // Binary outcome, algorithmType null

        tools.createSetupVersion("Attrition_Risk_Predictor", req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> modelConfig = (Map<String, Object>) body.getValue().get("modelConfiguration");
        assertThat(modelConfig).containsEntry("algorithmType", "Catboost");
    }

    @Test
    void versionCreate_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(400, "Insufficient data", "/ssot/machine-learning/model-setups/Attrition_Risk_Predictor/setup-versions"));

        String result = tools.createSetupVersion("Attrition_Risk_Predictor", minimalVersionRequest());
        assertThat(result).contains("error", "400", "Insufficient data");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void versionUpdate_partialBody() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "14kX1"));

        tools.updateSetupVersion("Attrition_Risk_Predictor", "14kX1", null, "Canceled");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/model-setups/Attrition_Risk_Predictor/setup-versions/14kX1");
        assertThat(body.getValue()).containsEntry("type", "EdcNoCode").containsEntry("status", "Canceled").doesNotContainKey("description");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void numberField_serializesBucketingStrategy() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14kX1"));

        FieldConfigInput num = numberField("Age__c");
        num.setMin(18.0);
        num.setMax(60.0);
        BucketingStrategyInput bucket = new BucketingStrategyInput();
        bucket.setType("Percentile");
        bucket.setNumberOfBuckets(10);
        num.setBucketingStrategy(bucket);

        SetupVersionCreateRequest req = minimalVersionRequest();
        req.setFields(List.of(num, numberField("Monthly_Income__c"), numberField("Years_At_Company__c")));

        tools.createSetupVersion("Attrition_Risk_Predictor", req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        List<Map<String, Object>> fields = (List<Map<String, Object>>) body.getValue().get("fields");
        Map<String, Object> first = fields.get(0);
        assertThat(first).containsEntry("type", "Number").containsEntry("name", "Age__c").containsEntry("min", 18.0).containsEntry("max", 60.0);
        Map<String, Object> bs = (Map<String, Object>) first.get("bucketingStrategy");
        assertThat(bs).containsEntry("type", "Percentile").containsEntry("numberOfBuckets", 10);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void numericalImputationTransformation_serializesImputeMethod() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14kX1"));

        TransformationInput tx = new TransformationInput();
        tx.setType("NumericalImputation");
        tx.setSourceFieldName("Monthly_Income__c");
        tx.setTargetFieldName("Monthly_Income__c");
        tx.setImputeMethod("Median");

        SetupVersionCreateRequest req = minimalVersionRequest();
        req.setTransformations(List.of(tx));

        tools.createSetupVersion("Attrition_Risk_Predictor", req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        List<Map<String, Object>> txs = (List<Map<String, Object>>) body.getValue().get("transformations");
        assertThat(txs).hasSize(1);
        assertThat(txs.get(0))
            .containsEntry("type", "NumericalImputation")
            .containsEntry("sourceFieldName", "Monthly_Income__c")
            .containsEntry("imputeMethod", "Median");
    }
}
