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
package com.salesforce.data360.mcp.tools.machinelearning.inspector;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectorToolsTest {

    @Mock
    private Data360Client client;

    private InspectorTools tools;

    @BeforeEach
    void setUp() {
        tools = new InspectorTools(client);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void inspectorMetrics_appliesDefaultGaugesWhenOmitted() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("inspectorResponses", Map.of()));

        tools.getSetupVersionInspectorMetrics("setup1", "14kSB000000C3mvYAC", null);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).isEqualTo(
                "/ssot/machine-learning/model-setups/setup1/setup-versions/14kSB000000C3mvYAC/inspector/metrics?gauges=Overview");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void inspectorMetrics_appliesDefaultGaugesWhenBlank() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("inspectorResponses", Map.of()));

        tools.getSetupVersionInspectorMetrics("setup1", "14kSB000000C3mvYAC", "  ");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).contains("gauges=Overview");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void inspectorMetrics_passesExplicitGauges() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of("inspectorResponses", Map.of()));

        tools.getSetupVersionInspectorMetrics("setup1", "14kSB000000C3mvYAC", "All");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue()).contains("gauges=All");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void inspectorMetrics_urlEncodesPathParams() {
        when(client.get(anyString(), eq(Map.class))).thenReturn(Map.of());

        tools.getSetupVersionInspectorMetrics("setup with space", "ver/id", "Overview");

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).get(path.capture(), eq(Map.class));
        assertThat(path.getValue())
                .contains("/model-setups/setup%20with%20space/")
                .contains("/setup-versions/ver%2Fid/");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void inspectorMetrics_apiError_returnsStructuredJson() {
        String path = "/ssot/machine-learning/model-setups/x/setup-versions/y/inspector/metrics";
        when(client.get(anyString(), eq(Map.class)))
                .thenThrow(new ApiException(500, "[{\"errorCode\":\"UNKNOWN_EXCEPTION\"}]", path));

        Map<String, Object> parsed = JsonUtil.fromJson(
                tools.getSetupVersionInspectorMetrics("x", "y", "FeatureImportances"), Map.class);
        assertThat(parsed)
                .containsEntry("statusCode", 500)
                .containsEntry("path", path);
        assertThat((String) parsed.get("error"))
                .contains("Data 360 API error 500")
                .contains("UNKNOWN_EXCEPTION");
    }
}
