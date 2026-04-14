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
import com.salesforce.data360.mcp.model.request.dataaction.DataActionCreateRequest;
import com.salesforce.data360.mcp.model.request.dataaction.DataActionTargetCreateRequest;
import com.salesforce.data360.mcp.model.request.dataaction.DataActionTargetUpdateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Data Action & Data Action Target Tools.
 * Maps to /ssot/data-actions* and /ssot/data-action-targets* endpoints.
 */
@Component
public class DataActionTools {

    private final Data360Client client;

    public DataActionTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all data actions.
     * Use this to discover existing data actions that share Data 360 data with external systems.
     */
    @McpTool(
        name = "d360_dataaction_list",
        description = "List all data actions."
    )
    public String listDataActions(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-actions", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a data action.
     * Use this to inspect a data action's full configuration — trigger conditions, payload schema, target, and execution history.
     */
    @McpTool(
        name = "d360_dataaction_get",
        description = "Get a data action."
    )
    public String getDataAction(
        @McpToolParam(description = "Data action ID") String actionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-actions/" + ToolUtils.encodePath(actionId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a data action.
     * Use this to define a new action that sends data from Data 360 to an external system.
     */
    @McpTool(
        name = "d360_dataaction_create",
        description = "Create a data action."
    )
    public String createDataAction(
        @McpToolParam(description = "Data action creation request body") DataActionCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-actions", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * List destinations for EVENT-TRIGGERED data shares.
     * These targets receive real-time data action payloads. NOT for segment audience pushes.
     */
    @McpTool(
        name = "d360_dataaction_target_list",
        description = "List data action targets."
    )
    public String listDataActionTargets(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-action-targets", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get one DATA ACTION target's configuration.
     * These targets receive event-triggered payloads. NOT for activation targets.
     */
    @McpTool(
        name = "d360_dataaction_target_get",
        description = "Get a data action target."
    )
    public String getDataActionTarget(
        @McpToolParam(description = "Data action target ID") String targetId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-action-targets/" + ToolUtils.encodePath(targetId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a destination for EVENT-TRIGGERED data shares.
     * Use to register webhooks, APIs, or S3 buckets that receive real-time payloads.
     */
    @McpTool(
        name = "d360_dataaction_target_create",
        description = "Create a data action target."
    )
    public String createDataActionTarget(
        @McpToolParam(description = "Data action target creation request body") DataActionTargetCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-action-targets", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update a DATA ACTION target's endpoint URL, credentials, or payload config.
     * NOT for activation targets.
     */
    @McpTool(
        name = "d360_dataaction_target_update",
        description = "Update a data action target."
    )
    public String updateDataActionTarget(
        @McpToolParam(description = "Data action target ID") String targetId,
        @McpToolParam(description = "Data action target update request body") DataActionTargetUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-action-targets/" + ToolUtils.encodePath(targetId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a DATA ACTION target.
     * Ensure no data actions reference it first. NOT for activation targets.
     */
    @McpTool(
        name = "d360_dataaction_target_delete",
        description = "Delete a data action target."
    )
    public String deleteDataActionTarget(
        @McpToolParam(description = "Data action target ID") String targetId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-action-targets/" + ToolUtils.encodePath(targetId), dataspace);
            client.delete(path);
            return JsonUtil.toJson(Map.of("success", true));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
