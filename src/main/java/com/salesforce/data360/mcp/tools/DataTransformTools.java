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
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformCreateRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformScheduleRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformUpdateRequest;
import com.salesforce.data360.mcp.model.request.datatransform.DataTransformValidateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Data Transform Tools - CRUD operations for data transformation jobs.
 * Maps to /ssot/data-transforms endpoints.
 */
@Component
public class DataTransformTools {

    private final Data360Client client;

    public DataTransformTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all data transforms in the org.
     * Discover all transformation jobs.
     */
    @McpTool(
        name = "d360_transform_list",
        description = "List all data transforms."
    )
    public String listDataTransforms(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific data transform by ID.
     * Returns transform definition, SQL, target DMO, and schedule.
     */
    @McpTool(
        name = "d360_transform_get",
        description = "Get a data transform."
    )
    public String getDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new data transform.
     * Requires label, name, type, and definition.
     */
    @McpTool(
        name = "d360_transform_create",
        description = "Create a data transform."
    )
    public String createDataTransform(
        @McpToolParam(description = "Data transform creation request body") DataTransformCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing data transform.
     * Can update description, SQL, and other properties.
     */
    @McpTool(
        name = "d360_transform_update",
        description = "Update a data transform."
    )
    public String updateDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Data transform update request body") DataTransformUpdateRequest request,
        @McpToolParam(description = "Optional dataspace query parameter", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a data transform.
     * Deletes transform and stops any scheduled runs.
     */
    @McpTool(
        name = "d360_transform_delete",
        description = "Delete a data transform."
    )
    public String deleteDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Run a data transform manually.
     * Manually trigger transform execution outside normal schedule.
     */
    @McpTool(
        name = "d360_transform_run",
        description = "Run a data transform."
    )
    public String runDataTransform(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/actions/run", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Validate a data transform SQL query.
     * Validate SQL syntax and semantics before creating or updating a transform.
     */
    @McpTool(
        name = "d360_transform_validate",
        description = "Validate a data transform."
    )
    public String validateDataTransform(
        @McpToolParam(description = "Data transform validation request body") DataTransformValidateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms-validation", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get the schedule for a data transform.
     * Check when transform runs (frequency, time, day of week).
     */
    @McpTool(
        name = "d360_transform_schedule_get",
        description = "Get transform schedule."
    )
    public String getDataTransformSchedule(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/schedule", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Set or update the schedule for a data transform.
     * Configure when transform runs (frequency: DAILY, WEEKLY, MONTHLY).
     */
    @McpTool(
        name = "d360_transform_schedule_set",
        description = "Set transform schedule."
    )
    public String setDataTransformSchedule(
        @McpToolParam(description = "The transform ID") String transformId,
        @McpToolParam(description = "Data transform schedule request body") DataTransformScheduleRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/data-transforms/" + ToolUtils.encodePath(transformId) + "/schedule", dataspace);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
