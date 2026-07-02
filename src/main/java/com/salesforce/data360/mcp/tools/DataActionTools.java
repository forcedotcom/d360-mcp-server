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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
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
    @ApiEndpoint(path = "/ssot/data-actions", verb = "GET")
    @McpTool(
        name = "d360_dataaction_list",
        description = "List all data actions."
    )
    public String listDataActions(
        @McpToolParam(description = "Dataspace name", required = false) String dataspace,
        @McpToolParam(description = "Maximum number of records to return", required = false) Integer batchSize,
        @McpToolParam(description = "Number of records to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            params.put("dataspace", dataspace);
            params.put("batchSize", batchSize);
            params.put("offset", offset);
            params.put("orderby", orderBy);
            String path = ToolUtils.buildPath("/ssot/data-actions", params);
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
    @ApiEndpoint(path = "/ssot/data-actions/{id}", verb = "GET")
    @McpTool(
        name = "d360_dataaction_get",
        description = "Get a data action."
    )
    public String getDataAction(
        @McpToolParam(description = "The data action developer name") String actionDeveloperName,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-actions/" + ToolUtils.encodePath(actionDeveloperName), dataspace);
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
    @ApiEndpoint(path = "/ssot/data-actions", verb = "POST")
    @McpTool(
        name = "d360_dataaction_create",
        description = "Create a data action."
    )
    public String createDataAction(
        @McpToolParam(description = "Data action creation request body") DataActionCreateRequest request,
        @McpToolParam(description = "Dataspace name", required = false) String dataspace
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
    @ApiEndpoint(path = "/ssot/data-action-targets", verb = "GET")
    @McpTool(
        name = "d360_dataaction_target_list",
        description = "List data action targets."
    )
    public String listDataActionTargets(
        @McpToolParam(description = "Maximum number of records to return", required = false) Integer batchSize,
        @McpToolParam(description = "Number of records to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            params.put("batchSize", batchSize);
            params.put("offset", offset);
            params.put("orderby", orderBy);
            String path = ToolUtils.buildPath("/ssot/data-action-targets", params);
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
    @ApiEndpoint(path = "/ssot/data-action-targets/{id}", verb = "GET")
    @McpTool(
        name = "d360_dataaction_target_get",
        description = "Get a data action target."
    )
    public String getDataActionTarget(
        @McpToolParam(description = "The data action target API name") String targetApiName
    ) {
        try {
            String path = "/ssot/data-action-targets/" + ToolUtils.encodePath(targetApiName);
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
    @ApiEndpoint(path = "/ssot/data-action-targets", verb = "POST")
    @McpTool(
        name = "d360_dataaction_target_create",
        description = "Create a data action target."
    )
    public String createDataActionTarget(
        @McpToolParam(description = "Data action target creation request body") DataActionTargetCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/data-action-targets";
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
    @ApiEndpoint(path = "/ssot/data-action-targets/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_dataaction_target_update",
        description = "Update a data action target."
    )
    public String updateDataActionTarget(
        @McpToolParam(description = "The data action target API name") String targetApiName,
        @McpToolParam(description = "Data action target update request body") DataActionTargetUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = "/ssot/data-action-targets/" + ToolUtils.encodePath(targetApiName);
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
    @ApiEndpoint(path = "/ssot/data-action-targets/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_dataaction_target_delete",
        description = "Delete a data action target."
    )
    public String deleteDataActionTarget(
        @McpToolParam(description = "The data action target API name") String targetApiName
    ) {
        try {
            String path = "/ssot/data-action-targets/" + ToolUtils.encodePath(targetApiName);
            client.delete(path);
            return JsonUtil.toJson(Map.of("success", true));
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
