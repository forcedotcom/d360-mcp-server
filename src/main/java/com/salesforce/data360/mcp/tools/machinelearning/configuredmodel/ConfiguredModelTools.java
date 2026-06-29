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
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.ConfiguredModelCreateRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel.ConfiguredModelPatchRequest;
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
 * Configured-model authoring tools — bridge between a trained
 * {@code MlModelArtifact} and the runtime layer.
 */
@Component
public class ConfiguredModelTools {

    private static final String BASE = "/ssot/machine-learning/configured-models";

    private final Data360Client client;

    public ConfiguredModelTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE, verb = "GET")
    @McpTool(
        name = "d360_ml_configured_model_list",
        description = "List configured models. When looking for a specific predictive model prefer narrowing with search "
            + "(free-text) and capabilities (e.g. BinaryClassification, Regression). To find a configured model wrapping a specific "
            + "artifact or model-setup, use assetIdOrName + assetType (ModelArtifact|ModelSetup). Other filters: modelType, "
            + "sourceType, connectorType, dataCloudOneVisibility."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String listConfiguredModels(
        @McpToolParam(description = "Free-text search across labels and descriptions.", required = false) String search,
        @McpToolParam(description = "Filter by model type. One of: Predictive, Generative, Transcribe, SpeechSynthesis, Summarization, Unknown.", required = false) String modelType,
        @McpToolParam(description = "Filter by capability. Comma-separated when multiple. Values: ChatCompletion, Completion, " +
                "Embedding, Regression, BinaryClassification, MulticlassClassification, Clustering, Generic, Transcribe, " +
                "SpeechSynthesis, Summarization, Forecast, SentimentAnalysis, TopicClassification.", required = false) String capabilities,
        @McpToolParam(description = "Filter by setup source type. One of: EdcNoCode, OutOfTheBox, ModelConnector.", required = false) String sourceType,
        @McpToolParam(description = "Filter by connector type (e.g. SageMaker, OpenAI, Bedrock).", required = false) String connectorType,
        @McpToolParam(description = "Filter to configured models bound to a specific asset (id or developer name). Pair with assetType.", required = false) String assetIdOrName,
        @McpToolParam(description = "Asset type for the assetIdOrName filter. One of: ModelSetup, ModelArtifact.", required = false) String assetType,
        @McpToolParam(description = "Filter by out-of-the-box flag (true returns only Salesforce-provided models).", required = false) Boolean outOfTheBox,
        @McpToolParam(description = "Data Cloud One visibility filter. One of: All, Local, Remote, Consumable.", required = false) String dataCloudOneVisibility,
        @McpToolParam(description = "Page size.", required = false) Integer limit,
        @McpToolParam(description = "Pagination offset.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (search != null) params.put("search", search);
            if (modelType != null) params.put("modelType", modelType);
            if (capabilities != null) params.put("capabilities", capabilities);
            if (sourceType != null) params.put("sourceType", sourceType);
            if (connectorType != null) params.put("connectorType", connectorType);
            if (assetIdOrName != null) params.put("assetIdOrName", assetIdOrName);
            if (assetType != null) params.put("assetType", assetType);
            if (outOfTheBox != null) params.put("outOfTheBox", outOfTheBox);
            if (dataCloudOneVisibility != null) params.put("dataCloudOneVisibility", dataCloudOneVisibility);
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
        name = "d360_ml_configured_model_get",
        description = "Get a configured model by id or developer name. Notable fields: status, "
            + "setupContainer (link back to model-setup), artifact, runtimeType, parameters, inputFields, outputFields."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getConfiguredModel(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName,
        @McpToolParam(description = "Optional Connect-API filter group (e.g. Small, Supplemental) to trim the response shape.", required = false) String filterGroup
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (filterGroup != null) params.put("filterGroup", filterGroup);
            String path = ToolUtils.buildPath(BASE + "/" + ToolUtils.encodePath(configuredModelIdOrName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE, verb = "POST")
    @McpTool(
        name = "d360_ml_configured_model_create",
        description = "Create a configured model wrapping an existing model artifact. The artifact reference is REQUIRED — "
            + "Newly created configured need to Activated (Enabled) before they can be deployed."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String createConfiguredModel(
        @Valid @NotNull
        @McpToolParam(description = "Configured model body. Required: artifact (id or developer name reference). " +
                "Optional: label, description, status, visibility, capability, parameterOverrides, actionableFields.")
        ConfiguredModelCreateRequest request
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
        name = "d360_ml_configured_model_update",
        description = "Update a configured model. Partial update — send only fields you want to change. "
            + "Setting status=Enabled here activates the model (same effect as d360_ml_configured_model_activate)."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String updateConfiguredModel(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName,
        @Valid @NotNull @McpToolParam(description = "Patch body — any subset of label, description, status, visibility, capability, parameterOverrides, actionableFields.") ConfiguredModelPatchRequest request
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(configuredModelIdOrName);
            Map result = client.patch(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_ml_configured_model_delete",
        description = "Delete a configured model."
    )
    public String deleteConfiguredModel(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(configuredModelIdOrName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("success", true));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_ml_configured_model_activate",
        description = "Activate a configured model (sets status=Enabled). Required before model can be used for inference."
    )
    public String activateConfiguredModel(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName
    ) {
        return patchStatus(configuredModelIdOrName, "Enabled");
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/histories", verb = "GET")
    @McpTool(
        name = "d360_ml_configured_model_history_list",
        description = "List history snapshots for a configured model."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String listHistories(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName,
        @McpToolParam(description = "Page size.", required = false) Integer limit,
        @McpToolParam(description = "Pagination offset.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            String path = ToolUtils.buildPath(BASE + "/" + ToolUtils.encodePath(configuredModelIdOrName) + "/histories", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}/histories/{historyId}", verb = "GET")
    @McpTool(
        name = "d360_ml_configured_model_history_get",
        description = "Get one configured-model history snapshot. The 'configuredModel' field on the response carries the "
            + "snapshot of the model as it was at the time the history entry was recorded."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getHistory(
        @NotBlank @McpToolParam(description = "Configured model id or developer name.") String configuredModelIdOrName,
        @NotBlank @McpToolParam(description = "History entry id (15- or 18-character Salesforce id).") String historyId
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(configuredModelIdOrName)
                    + "/histories/" + ToolUtils.encodePath(historyId);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String patchStatus(String idOrName, String status) {
        try {
            Map<String, Object> body = Map.of("status", status);
            String path = BASE + "/" + ToolUtils.encodePath(idOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
