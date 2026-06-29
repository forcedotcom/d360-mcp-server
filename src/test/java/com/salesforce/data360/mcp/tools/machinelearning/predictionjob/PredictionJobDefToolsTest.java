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
package com.salesforce.data360.mcp.tools.machinelearning.predictionjob;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionJobDefPatchRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predictionjob.PredictionsConfigInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionJobDefToolsTest {

    @Mock
    private Data360Client client;

    private PredictionJobDefTools tools;

    @BeforeEach
    void setUp() {
        tools = new PredictionJobDefTools(client);
    }

    @Test
    void list_passesModelIdQueryParam() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("predictionJobDefinitions", java.util.List.of()));

        tools.listPredictionJobDefs("12lSG0000005K7lYAE");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions?modelId=12lSG0000005K7lYAE");
    }

    @Test
    void list_omitsQueryWhenNoFilter() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of());

        tools.listPredictionJobDefs(null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions");
    }

    @Test
    void get_urlEncodesIdOrName() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        tools.getPredictionJobDef("pred job test");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions/pred%20job%20test");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void create_postsRequestBodyToBaseEndpoint() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "106SG00000IMvoTYAT"));

        AssetReferenceInput model = new AssetReferenceInput();
        model.setId("12lSG0000005K7lYAE");
        PredictionsConfigInput output = new PredictionsConfigInput();
        output.setObjectName("regression_job");

        PredictionJobDefCreateRequest req = new PredictionJobDefCreateRequest();
        req.setType("Regression");
        req.setApiName("pred_job_test");
        req.setModel(model);
        req.setOutputConfig(output);
        req.setScoringMode("Batch");

        String result = tools.createPredictionJobDef(req);

        assertThat(result).contains("106SG00000IMvoTYAT");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));

        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions");
        assertThat(body.getValue())
            .containsEntry("type", "Regression")
            .containsEntry("apiName", "pred_job_test")
            .containsEntry("scoringMode", "Batch");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void update_partialBodyAndPath() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("id", "abc"));

        PredictionJobDefPatchRequest req = new PredictionJobDefPatchRequest();
        req.setDescription("updated");

        tools.updatePredictionJobDef("pred_job_test", req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions/pred_job_test");
        assertThat(body.getValue()).containsEntry("description", "updated");
        assertThat(body.getValue()).doesNotContainKey("activationStatus");
    }

    @Test
    void delete_callsExpectedPath() {
        when(client.delete(anyString(), eq(Map.class))).thenReturn(Map.of());

        tools.deletePredictionJobDef("pred_job_test");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).delete(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions/pred_job_test");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void activate_sendsActiveStatus() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("activationStatus", "Active"));

        tools.activatePredictionJobDef("pred_job_test");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/prediction-job-definitions/pred_job_test");
        assertThat(body.getValue()).containsEntry("activationStatus", "Active");
        assertThat(body.getValue()).hasSize(1);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void deactivate_sendsInactiveStatus() {
        when(client.patch(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("activationStatus", "Inactive"));

        tools.deactivatePredictionJobDef("pred_job_test");

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).patch(anyString(), body.capture(), eq(Map.class));
        assertThat(body.getValue()).containsEntry("activationStatus", "Inactive");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void run_byName_putsNameInInputBlock() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14qX1", "status", "Submited"));

        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName("pred_job_test");
        tools.runPredictionJob(ref);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/jobs");
        assertThat(body.getValue()).containsEntry("type", "Prediction");
        Map<String, Object> input = (Map<String, Object>) body.getValue().get("input");
        assertThat(input).containsEntry("name", "pred_job_test");
        assertThat(input).doesNotContainKey("id");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void run_byId_putsIdInInputBlock() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("id", "14qX1", "status", "Submited"));

        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setId("106SG00000IMvoTYAT");
        tools.runPredictionJob(ref);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> input = (Map<String, Object>) body.getValue().get("input");
        assertThat(input).containsEntry("id", "106SG00000IMvoTYAT");
        assertThat(input).doesNotContainKey("name");
    }

    @Test
    void run_apiError_propagatesAsStructuredJson() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/machine-learning/jobs"));

        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName("missing");
        String result = tools.runPredictionJob(ref);

        assertThat(result).contains("error", "404", "/ssot/machine-learning/jobs");
    }

    @Test
    void list_apiError_returnsStructuredError() {
        when(client.get(anyString(), eq(Map.class)))
            .thenThrow(new ApiException(500, "Server boom", "/ssot/machine-learning/prediction-job-definitions"));

        String result = tools.listPredictionJobDefs(null);

        assertThat(result).contains("error", "500");
        verify(client, never()).post(anyString(), any(), any());
    }
}
