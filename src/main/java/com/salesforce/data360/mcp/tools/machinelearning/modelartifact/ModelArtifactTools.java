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
package com.salesforce.data360.mcp.tools.machinelearning.modelartifact;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.modelartifact.ModelArtifactPatchRequest;
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
 * Read/edit tools for ML model artifacts.
 */
@Component
public class ModelArtifactTools {

    private static final String BASE = "/ssot/machine-learning/model-artifacts";

    private final Data360Client client;

    public ModelArtifactTools(Data360Client client) {
        this.client = client;
    }

    @ApiEndpoint(path = BASE, verb = "GET")
    @McpTool(
        name = "d360_ml_model_artifact_list",
        description = "List trained ML model artifacts. Artifacts are auto-created when a setup-version reaches Published. "
            + "Filter by modelType (Predictive, Generative, Transcribe, SpeechSynthesis, Summarization, Unknown) or "
            + "sourceType (EdcNoCode, OutOfTheBox, ModelConnector)."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String listModelArtifacts(
        @McpToolParam(description = "Filter by model type. One of: Predictive, Generative, Transcribe, SpeechSynthesis, Summarization, Unknown.", required = false) String modelType,
        @McpToolParam(description = "Filter by setup source type. One of: EdcNoCode, OutOfTheBox, ModelConnector.", required = false) String sourceType,
        @McpToolParam(description = "Data Cloud One visibility filter. One of: All, Local, Remote, Consumable.", required = false) String dataCloudOneVisibility,
        @McpToolParam(description = "Page size.", required = false) Integer limit,
        @McpToolParam(description = "Pagination offset.", required = false) Integer offset
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (modelType != null) params.put("modelType", modelType);
            if (sourceType != null) params.put("sourceType", sourceType);
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
        name = "d360_ml_model_artifact_get",
        description = "Get a model artifact by id or developer name. Notable fields: status, modelType, sourceType, "
            + "runtimeType, source (link to setup-version), setupContainer (link to model-setup), parameters, inputFields, "
            + "outputFields, modelCapabilities."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String getModelArtifact(
        @NotBlank @McpToolParam(description = "Model artifact id or developer name.") String modelArtifactIdOrName,
        @McpToolParam(description = "Optional Connect-API filter group (e.g. Small, Supplemental) to trim the response shape.", required = false) String filterGroup
    ) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            if (filterGroup != null) params.put("filterGroup", filterGroup);
            String path = ToolUtils.buildPath(BASE + "/" + ToolUtils.encodePath(modelArtifactIdOrName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "PATCH")
    @McpTool(
        name = "d360_ml_model_artifact_update",
        description = "Update a model artifact (label, description, status, or clustering output-field overrides). "
            + "Partial update — send only fields to change."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String updateModelArtifact(
        @NotBlank @McpToolParam(description = "Model artifact id or developer name.") String modelArtifactIdOrName,
        @Valid @NotNull @McpToolParam(description = "Patch body. Any subset of label, description, status, outputFields.") ModelArtifactPatchRequest request
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelArtifactIdOrName);
            Map result = client.patch(path, JsonUtil.toMap(request), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @ApiEndpoint(path = BASE + "/{idOrName}", verb = "DELETE")
    @McpTool(
        name = "d360_ml_model_artifact_delete",
        description = "Delete a model artifact."
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    public String deleteModelArtifact(
        @NotBlank @McpToolParam(description = "Model artifact id or developer name.") String modelArtifactIdOrName
    ) {
        try {
            String path = BASE + "/" + ToolUtils.encodePath(modelArtifactIdOrName);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
