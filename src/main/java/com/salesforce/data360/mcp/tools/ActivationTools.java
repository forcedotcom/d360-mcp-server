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
package com.salesforce.data360.mcp.tools;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.activation.ActivationTargetCreateRequest;
import com.salesforce.data360.mcp.model.request.activation.ActivationTargetUpdateRequest;
import com.salesforce.data360.mcp.model.request.activation.ActivationUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Activation & Activation Target Tools - segment activation to destinations.
 * Maps to /ssot/activations and /ssot/activation-targets endpoints.
 */
@Component
public class ActivationTools {

    private final Data360Client client;

    public ActivationTools(Data360Client client) {
        this.client = client;
    }

    // ========================================================================
    // ACTIVATION METHODS
    // ========================================================================

    /**
     * List all activations in the organization.
     * Activations connect segments to destinations.
     */
    @ApiEndpoint(path = "/ssot/activations", verb = "GET")
    @McpTool(
        name = "d360_activation_list",
        description = "List all activations."
    )
    public String listActivations(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activations", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific activation by ID.
     * Returns activation details, status, and configuration.
     */
    @ApiEndpoint(path = "/ssot/activations/{id}", verb = "GET")
    @McpTool(
        name = "d360_activation_get",
        description = "Get an activation."
    )
    public String getActivation(
        @McpToolParam(description = "The activation ID") String activationId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activations/" + ToolUtils.encodePath(activationId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new activation.
     * Connects a segment to a destination (activation target).
     * Accepts raw JSON body to support the complex nested activation API structure.
     */
    @ApiEndpoint(path = "/ssot/activations", verb = "POST")
    @McpTool(
        name = "d360_activation_create",
        description = "Create an activation. Required fields: name, refreshType (INCREMENTAL or Full), dataSpaceName, "
            + "activationTargetSubjectConfig ({developerName}), activationTargetName OR dataExportDefinitionId, "
            + "segmentApiName OR marketSegmentId. "
            + "Also include attributesConfig with at least the primary entity ID attribute. "
            + "Pass the body as a raw JSON string."
    )
    public String createActivation(
        @McpToolParam(description = "Activation body as JSON string") String body,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> bodyMap = ToolUtils.parseJson(body, Map.class, "body");
            String path = ToolUtils.buildPath("/ssot/activations", dataspace);
            Map result = client.post(path, bodyMap, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing activation.
     * Can update refreshType, filters, and other configuration.
     */
    @ApiEndpoint(path = "/ssot/activations/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_activation_update",
        description = "Update an activation."
    )
    public String updateActivation(
        @McpToolParam(description = "The activation ID to update") String activationId,
        @McpToolParam(description = "Activation update details") ActivationUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/activations/" + ToolUtils.encodePath(activationId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete an activation.
     * Stops sending segment data to the target.
     */
    @ApiEndpoint(path = "/ssot/activations/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_activation_delete",
        description = "Delete an activation."
    )
    public String deleteActivation(
        @McpToolParam(description = "The activation ID to delete") String activationId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activations/" + ToolUtils.encodePath(activationId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ========================================================================
    // ACTIVATION TARGET METHODS
    // ========================================================================

    /**
     * List all activation targets in the organization.
     * Activation targets are destinations for segment data.
     */
    @ApiEndpoint(path = "/ssot/activation-targets", verb = "GET")
    @McpTool(
        name = "d360_activation_target_list",
        description = "List activation targets."
    )
    public String listActivationTargets(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activation-targets", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific activation target by ID.
     * Returns target configuration and available fields.
     */
    @ApiEndpoint(path = "/ssot/activation-targets/{id}", verb = "GET")
    @McpTool(
        name = "d360_activation_target_get",
        description = "Get an activation target."
    )
    public String getActivationTarget(
        @McpToolParam(description = "The activation target ID") String targetId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activation-targets/" + ToolUtils.encodePath(targetId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new activation target.
     * Defines a destination for segment activation.
     */
    @ApiEndpoint(path = "/ssot/activation-targets", verb = "POST")
    @McpTool(
        name = "d360_activation_target_create",
        description = "Create an activation target."
    )
    public String createActivationTarget(
        @McpToolParam(description = "Activation target creation details") ActivationTargetCreateRequest request,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/activation-targets", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing activation target.
     * Can update name, dataSpaceName, and other settings.
     */
    @ApiEndpoint(path = "/ssot/activation-targets/{id}", verb = "PUT")
    @McpTool(
        name = "d360_activation_target_update",
        description = "Update an activation target."
    )
    public String updateActivationTarget(
        @McpToolParam(description = "The activation target ID to update") String targetId,
        @McpToolParam(description = "Activation target update details") ActivationTargetUpdateRequest request,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/activation-targets/" + ToolUtils.encodePath(targetId), dataspace);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete an activation target.
     * Breaks activations using this target.
     */
    @ApiEndpoint(path = "/ssot/activation-targets/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_activation_target_delete",
        description = "Delete an activation target."
    )
    public String deleteActivationTarget(
        @McpToolParam(description = "The activation target ID to delete") String targetId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/activation-targets/" + ToolUtils.encodePath(targetId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
