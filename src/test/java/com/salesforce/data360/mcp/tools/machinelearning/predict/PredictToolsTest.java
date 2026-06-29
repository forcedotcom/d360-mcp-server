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
package com.salesforce.data360.mcp.tools.machinelearning.predict;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.predict.PredictRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.predict.PredictSettings;
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
class PredictToolsTest {

    @Mock
    private Data360Client client;

    private PredictTools tools;

    @BeforeEach
    void setUp() {
        tools = new PredictTools(client);
    }

    private static PredictRequest sampleRequest() {
        AssetReferenceInput model = new AssetReferenceInput();
        model.setId("0jrSB00000ABC123YAA");
        PredictRequest req = new PredictRequest();
        req.setModel(model);
        req.setFieldNames(List.of("age", "income", "tenure_months"));
        req.setRows(List.of(
            List.of("25", "50000", "12"),
            List.of("54", "120000", "240")
        ));
        return req;
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void predict_passesFlatBodyAndFixedTypeRawData() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of("predictions", List.of()));

        tools.predict(sampleRequest());

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/predict");
        assertThat(body.getValue())
            .containsEntry("type", "RawData")
            .containsEntry("fieldNames", List.of("age", "income", "tenure_months"))
            .containsKey("rows")
            .doesNotContainKey("settings");
        Map<String, Object> model = (Map<String, Object>) body.getValue().get("model");
        assertThat(model).containsEntry("id", "0jrSB00000ABC123YAA").doesNotContainKey("name");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void predict_acceptsModelByName() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of());

        AssetReferenceInput model = new AssetReferenceInput();
        model.setName("Predicted_Attrition");
        PredictRequest req = sampleRequest();
        req.setModel(model);

        tools.predict(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> bodyModel = (Map<String, Object>) body.getValue().get("model");
        assertThat(bodyModel).containsEntry("name", "Predicted_Attrition").doesNotContainKey("id");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void predict_passesAllSettingsFields() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of());

        PredictSettings settings = new PredictSettings();
        settings.setMaxPrescriptions(5);
        settings.setMaxTopFactors(3);
        settings.setPrescriptionImpactPercentage(20);
        PredictRequest req = sampleRequest();
        req.setSettings(settings);

        tools.predict(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> serializedSettings = (Map<String, Object>) body.getValue().get("settings");
        assertThat(serializedSettings)
            .containsEntry("maxPrescriptions", 5)
            .containsEntry("maxTopFactors", 3)
            .containsEntry("prescriptionImpactPercentage", 20);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void predict_omitsUnsetSettingsFields() {
        when(client.post(anyString(), any(Map.class), eq(Map.class))).thenReturn(Map.of());

        PredictSettings settings = new PredictSettings();
        settings.setMaxTopFactors(2);
        PredictRequest req = sampleRequest();
        req.setSettings(settings);

        tools.predict(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> serializedSettings = (Map<String, Object>) body.getValue().get("settings");
        assertThat(serializedSettings)
            .containsEntry("maxTopFactors", 2)
            .doesNotContainKey("maxPrescriptions")
            .doesNotContainKey("prescriptionImpactPercentage");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void predict_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(405,
                "[{\"errorCode\":\"METHOD_NOT_ALLOWED\",\"message\":\"Generative models are not supported for predict\"}]",
                "/ssot/machine-learning/predict"));

        Map<String, Object> parsed = JsonUtil.fromJson(tools.predict(sampleRequest()), Map.class);
        assertThat(parsed)
            .containsEntry("statusCode", 405)
            .containsEntry("path", "/ssot/machine-learning/predict");
        assertThat((String) parsed.get("error"))
            .contains("Data 360 API error 405")
            .contains("Generative models are not supported");
    }
}
