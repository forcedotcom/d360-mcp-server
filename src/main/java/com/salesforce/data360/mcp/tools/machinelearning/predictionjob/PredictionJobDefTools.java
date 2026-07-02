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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic CRUD, activate/deactivate, and run tools for prediction job
 * definitions. Maps to /ssot/machine-learning/prediction-job-definitions and
 * /ssot/machine-learning/jobs.
 */
@Component
public class PredictionJobDefTools extends AbstractPredictionJobDefTools {

    private static final String JOBS_PATH = "/ssot/machine-learning/jobs";

    public PredictionJobDefTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions", verb = "GET")
    @McpTool(
        name = "d360_prediction_job_def_list",
        description = "List prediction job definitions. Optional modelId filter narrows the list to definitions for a specific configured model."
    )
    public String listPredictionJobDefs(
        @McpToolParam(description = "Filter by configured-model id.", required = false) String modelId
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (modelId != null) params.put("modelId", modelId);
            String path = ToolUtils.buildPath(BASE_PATH, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions/{idOrName}", verb = "GET")
    @McpTool(
        name = "d360_prediction_job_def_get",
        description = "Get a prediction job definition by id or developer name."
    )
    public String getPredictionJobDef(
        @McpToolParam(description = "Prediction job definition id or developer name.") String predictionJobDefIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(predictionJobDefIdOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions", verb = "POST")
    @McpTool(
        name = "d360_prediction_job_def_create",
        description = "Create a prediction job definition (generic). "
            + "PREFER the type-specific tools when possible — they pre-shape the payload and run pre-flight validation: "
            + "d360_prediction_job_def_create_regression, d360_prediction_job_def_create_binary_classification, "
            + "d360_prediction_job_def_create_multiclass_classification, d360_prediction_job_def_create_sentiment_detection, "
            + "d360_prediction_job_def_create_topic_classification, d360_prediction_job_def_create_clustering. "
            + "Use this generic tool only when you need a shape the type-specific tools don't expose. "
            + "TYPICAL LIFECYCLE: "
            + "(1) CREATE — call this tool (or a type-specific create) to define the job; the response carries the new id/apiName with activationStatus=Inactive. "
            + "(2) ACTIVATE — call d360_prediction_job_def_activate; a definition must be Active before it can be run. "
            + "(3) VERIFY ACTIVE — call d360_prediction_job_def_get to confirm activationStatus=Active before submitting. "
            + "(4) RUN — call d360_prediction_job_run with the def's id or name; the response is async and returns an initial status (Submited/Pending). "
            + "(5) MONITOR — call d360_prediction_job_def_get to read lastRunStatus and lastRunDate on the definition. lastRunStatus transitions to Success or Failed when the run completes."
    )
    public String createPredictionJobDef(
        @McpToolParam(description = "Full prediction job definition create payload.") PredictionJobDefCreateRequest request
    ) {
        return super.createPredictionJobDef(request);
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_prediction_job_def_update",
        description = "Update a prediction job definition. Partial-update semantics: send only the fields you want to change. "
            + "IMPORTANT: most fields are immutable while the definition is in Active status — the server will reject the update. "
            + "Deactivate first (d360_prediction_job_def_deactivate), apply the update, then re-activate (d360_prediction_job_def_activate). "
            + "For toggling activationStatus alone, prefer d360_prediction_job_def_activate / d360_prediction_job_def_deactivate."
    )
    public String updatePredictionJobDef(
        @McpToolParam(description = "Prediction job definition id or developer name.") String predictionJobDefIdOrName,
        @McpToolParam(description = "Partial update body. Any field set is changed; unset fields are left unchanged.") PredictionJobDefPatchRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = BASE_PATH + "/" + ToolUtils.encodePath(predictionJobDefIdOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_prediction_job_def_delete",
        description = "Delete a prediction job definition. Irreversible."
    )
    public String deletePredictionJobDef(
        @McpToolParam(description = "Prediction job definition id or api name.") String predictionJobDefIdOrName
    ) {
        try {
            String path = BASE_PATH + "/" + ToolUtils.encodePath(predictionJobDefIdOrName);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_prediction_job_def_activate",
        description = "Activate a prediction job definition (sets activationStatus=Active). "
            + "A definition must be Active before it can be submitted via d360_prediction_job_run."
    )
    public String activatePredictionJobDef(
        @McpToolParam(description = "Prediction job definition id or developer name.") String predictionJobDefIdOrName
    ) {
        return patchActivationStatus(predictionJobDefIdOrName, "Active");
    }

    @ApiEndpoint(path = "/ssot/machine-learning/prediction-job-definitions/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_prediction_job_def_deactivate",
        description = "Deactivate a prediction job definition (sets activationStatus=Inactive). Inactive definitions cannot be run."
    )
    public String deactivatePredictionJobDef(
        @McpToolParam(description = "Prediction job definition id or developer name.") String predictionJobDefIdOrName
    ) {
        return patchActivationStatus(predictionJobDefIdOrName, "Inactive");
    }

    @ApiEndpoint(path = "/ssot/machine-learning/jobs", verb = "POST")
    @McpTool(
        name = "d360_prediction_job_run",
        description = "Submit a prediction job for execution. Async — the response carries a job id and an initial status "
            + "PREREQUISITE: the referenced prediction job definition must be in Active status; call d360_prediction_job_def_activate first if needed."
    )
    public String runPredictionJob(
        @McpToolParam(description = "Prediction job definition reference. Provide either id or name (one is required).") AssetReferenceInput predictionJobDef
    ) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", "Prediction");
            body.put("input", JsonUtil.toMap(predictionJobDef));
            Map result = client.post(JOBS_PATH, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private String patchActivationStatus(String idOrName, String status) {
        try {
            Map<String, Object> body = Map.of("activationStatus", status);
            String path = BASE_PATH + "/" + ToolUtils.encodePath(idOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
