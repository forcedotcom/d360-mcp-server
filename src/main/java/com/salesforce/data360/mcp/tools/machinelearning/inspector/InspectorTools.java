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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inspector tools for ML setup versions — surfaces training-time evaluation
 * (metrics) for an authored setup version. Pairs with {@code ModelArtifactTools}
 * and {@code AlertTools} as the post-training evaluation surface.
 */
@Component
public class InspectorTools {

    private static final String BASE = "/ssot/machine-learning/model-setups";
    private static final String DEFAULT_GAUGES = "Overview";

    private final Data360Client client;

    public InspectorTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/setup-versions/{versionId}/inspector/metrics", verb = "GET")
    @McpTool(
        name = "d360_ml_setup_version_inspector_metrics_get",
        description = "Training-time inspector metrics for a setup version. Returns the trained model's evaluation — "
            + "AUC, gini, MCC, MSE, RMSE, 100-threshold confusion matrices, gain table, outcome distribution. "
            + "gauges is a comma-separated subset of: All, Overview, GainTable, Residuals, Correlations, FeatureImportances, "
            + "Coefficients, DataSegments, CrossValidations, OutcomeDistribution, PairwiseCorrelations. "
            + "Defaults to 'Overview' if omitted. "
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getSetupVersionInspectorMetrics(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName,
        @NotBlank @McpToolParam(description = "Setup version id.") String versionId,
        @McpToolParam(description = "Comma-separated gauges. Defaults to 'Overview' when omitted.", required = false) String gauges
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("gauges", (gauges == null || gauges.isBlank()) ? DEFAULT_GAUGES : gauges);
            String path = ToolUtils.buildPath(
                BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName)
                    + "/setup-versions/" + ToolUtils.encodePath(versionId)
                    + "/inspector/metrics",
                params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
