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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tools for ML data alerts (training-time data-quality findings and runtime drift).
 */
@Component
public class AlertTools {

    private static final String BASE = "/ssot/machine-learning/alerts";

    private final Data360Client client;

    public AlertTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE, verb = "POST")
    @McpTool(
        name = "d360_ml_alerts_query",
        description = "Query data alerts for a model artifact. Returns training-time findings (e.g. ObviousPredictor, "
            + "CollinearFields) and runtime drift alerts. Each alert carries structured directives[] (e.g. "
            + "'Exclude-FieldName__c-Label') that the platform auto-applies if you accept the alert and create a new "
            + "setup-version with sourceSetupVersionNumber set to the current version. "
            + "asset is required (artifact id or developer name)."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String queryAlerts(
        @Valid @NotNull @McpToolParam(description = "Query body: asset (required), assetType (required, eg: 'ModelArtifact'), " +
                "and optional sourceTypes filter.") AlertQueryRequest request
    ) {
        try {
            Map result = client.post(BASE, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{alertId}", verb = "PATCH")
    @McpTool(
        name = "d360_ml_alert_update",
        description = "Triage a data alert. accepted=true flags the alert's directives for auto-application on the "
            + "next setup-version (when created with sourceSetupVersionNumber pointing to the current version). "
            + "dismissed=true suppresses the alert from active triage without applying directives. "
            + "Send only ONE of accepted/dismissed per call."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String updateAlert(
        @NotBlank @McpToolParam(description = "Alert id.") String alertId,
        @McpToolParam(description = "Mark accepted — directives auto-apply on retrain.", required = false) Boolean accepted,
        @McpToolParam(description = "Mark dismissed — suppresses without applying.", required = false) Boolean dismissed
    ) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            if (accepted != null) body.put("accepted", accepted);
            if (dismissed != null) body.put("dismissed", dismissed);
            String path = BASE + "/" + ToolUtils.encodePath(alertId);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
