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
import com.salesforce.data360.mcp.model.request.machinelearning.setup.ModelSetupCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.ModelSetupPatchRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.OutcomeInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.SetupVersionCreateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authoring tools for ML model setups. Pairs with the setup-helper query tools
 * ({@code d360_ml_query_setup_fields}, {@code _data_profile}, {@code _outcome},
 * {@code _row_count}) — agents discover the data shape with helpers, then use
 * these tools to create a container and a setup version that triggers training.
 *
 */
@Component
public class ModelSetupTools {

    private static final String BASE = "/ssot/machine-learning/model-setups";

    private final Data360Client client;

    public ModelSetupTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE, verb = "GET")
    @McpTool(
        name = "d360_ml_model_setup_list",
        description = "List model-setup containers. Optional filters: search (free-text), modelType, modelCapability, setupType, connectorType. Pagination via limit/offset."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String listModelSetups(
        @McpToolParam(description = "Free-text search across labels and descriptions.", required = false) String search,
        @McpToolParam(description = "Filter by model type. One of: Predictive, Generative, Transcribe, SpeechSynthesis, Summarization, Unknown.", required = false) String modelType,
        @McpToolParam(description = "Filter by model capability (e.g. BinaryClassification, Regression).", required = false) String modelCapability,
        @McpToolParam(description = "Filter by setup type. One of: EdcNoCode, OutOfTheBox, ModelConnector.", required = false) String setupType,
        @McpToolParam(description = "Filter by connector type (e.g. SageMaker, OpenAI).", required = false) String connectorType,
        @McpToolParam(description = "Page size (default platform-defined).", required = false) Integer limit,
        @McpToolParam(description = "Pagination offset.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (search != null) params.put("search", search);
            if (modelType != null) params.put("modelType", modelType);
            if (modelCapability != null) params.put("modelCapability", modelCapability);
            if (setupType != null) params.put("setupType", setupType);
            if (connectorType != null) params.put("connectorType", connectorType);
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            String path = ToolUtils.buildPath(BASE, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "GET")
    @McpTool(
        name = "d360_ml_model_setup_get",
        description = "Get a model-setup container by id or developer name."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getModelSetup(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE, verb = "POST")
    @McpTool(
        name = "d360_ml_model_setup_create",
        description = "Create a model-setup container (the parent record that groups setup versions). "
            + "STEP 1 OF 3 in the authoring flow: "
            + "(1) d360_ml_model_setup_create — create the container. "
            + "(2) d360_ml_setup_version_create — author a setup version (status=Draft, no training yet). "
            + "(3) d360_ml_setup_version_update with status='Training' — start training. "
            + "Then poll d360_ml_setup_version_get until status transitions to Published or Failed."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String createModelSetup(
        @Valid @NotNull
        @McpToolParam(description = "Container metadata (label, modelType, modelCapability, setupType — all required; description, connectorType, outcomeDefinition optional).")
        ModelSetupCreateRequest request
    ) {
        try {
            Map result = client.post(BASE, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_ml_model_setup_update",
        description = "Update a model-setup container. Partial update — send only fields you want to change."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String updateModelSetup(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName,
        @Valid @NotNull @McpToolParam(description = "Patch body (label, description, modelCapability, outcomeDefinition — any subset).") ModelSetupPatchRequest request
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName);
            Map result = client.patch(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_ml_model_setup_delete",
        description = "Delete a model-setup container. Cascades to all versions. Irreversible."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String deleteModelSetup(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/setup-versions", verb = "GET")
    @McpTool(
        name = "d360_ml_setup_version_list",
        description = "List versions of a model-setup container. Pagination via limit/offset."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String listSetupVersions(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName,
        @McpToolParam(description = "Page size.", required = false) Integer limit,
        @McpToolParam(description = "Pagination offset.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            String path = ToolUtils.buildPath(BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName) + "/setup-versions", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/setup-versions/{versionId}", verb = "GET")
    @McpTool(
        name = "d360_ml_setup_version_get",
        description = "Get one setup version with its full setup body. Lifecycle: Draft (after create) → Training (after user PATCH status='Training') → Published or Failed (set asynchronously by the platform when the EDC trainer reports back). Use this tool to poll for the terminal state."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getSetupVersion(
        @NotBlank @McpToolParam(description = "Model-setup id or developer name.") String modelSetupIdOrName,
        @NotBlank @McpToolParam(description = "Setup version id.") String versionId
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName) + "/setup-versions/" + ToolUtils.encodePath(versionId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/setup-versions", verb = "POST")
    @McpTool(
        name = "d360_ml_setup_version_create",
        description = "Author a new EdcNoCode setup version on an existing container. "
            + "STEP 2 OF 3: this only saves the configuration in status=Draft — it DOES NOT START TRAINING. "
            + "After this returns, call d360_ml_setup_version_update with status='Training' to start training, then poll d360_ml_setup_version_get for status. "
            + "Required: input (data source), outcomes (single Binary/DiscreteVariable/ContinuousVariable/Clustering), fields (3+ field configurations). "
            + "Pass each field configuration as returned by d360_ml_query_setup_fields. "
            + "Platform requirements: input dataset must have ≥400 rows after filters/joins; ≥3 fields. "
            + "If algorithmType is omitted, this tool applies a default based on the outcome type."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String createSetupVersion(
        @NotBlank @McpToolParam(description = "Model-setup container id or developer name.") String modelSetupIdOrName,
        @Valid @NotNull @McpToolParam(description = "Setup version body (input, outcomes, fields required; algorithmType, transformations, filter, joins optional).") SetupVersionCreateRequest request
    ) {
        try {
            applyAlgorithmDefault(request);
            Map<String, Object> body = JsonUtil.toMap(request);
            // Wire: hardcode discriminator and wrap algorithmType under modelConfiguration
            body.put("type", "EdcNoCode");
            Object algorithmType = body.remove("algorithmType");
            if (algorithmType != null) {
                Map<String, Object> modelConfig = new HashMap<>();
                modelConfig.put("algorithmType", algorithmType);
                body.put("modelConfiguration", modelConfig);
            }
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName) + "/setup-versions";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/setup-versions/{versionId}", verb = "PATCH")
    @McpTool(
        name = "d360_ml_setup_version_update",
        description = "Update a setup version. Only 'description' and 'status' are mutable; setup body (input, outcomes, fields, algorithm) is immutable. "
            + "STEP 3 OF 3: send status='Training' to start training a Draft version (the d360_ml_setup_version_create tool leaves new versions in Draft). "
            + "To change the setup body, create a new version (POST) with sourceSetupVersionNumber pointing to the prior version."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String updateSetupVersion(
        @NotBlank @McpToolParam(description = "Model-setup container id or developer name.") String modelSetupIdOrName,
        @NotBlank @McpToolParam(description = "Setup version id.") String versionId,
        @McpToolParam(description = "New free-form description.", required = false) String description,
        @McpToolParam(description = "New status. 'Training' starts training on a Draft version .", required = false) String status
    ) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("type", "EdcNoCode");
            if (description != null) body.put("description", description);
            if (status != null) body.put("status", status);
            String path = BASE + "/" + ToolUtils.encodePath(modelSetupIdOrName) + "/setup-versions/" + ToolUtils.encodePath(versionId);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Apply Studio-equivalent default algorithm when caller didn't specify one.
     * Mirrors {@code edcSetupPickAlgoContainer.js#getAutoSelectAlgorithm}: picks
     * by outcome type. Assumes V2 runtime (default for new setups).
     */
    private static void applyAlgorithmDefault(SetupVersionCreateRequest request) {
        if (request.getAlgorithmType() != null) return;
        if (request.getOutcomes() == null || request.getOutcomes().isEmpty()) return;
        OutcomeInput first = request.getOutcomes().get(0);
        if (first == null || first.getType() == null) return;

        switch (first.getType()) {
            case "Binary":
            case "ContinuousVariable":
                request.setAlgorithmType("Catboost");
                break;
            case "DiscreteVariable":
                request.setAlgorithmType("XgboostV2");
                break;
            case "Clustering":
                request.setAlgorithmType(first.getNClusters() != null ? "Kmeans" : "Hdbscan");
                break;
            default:
                // Unknown outcome — let the platform reject, don't default
        }
    }
}
