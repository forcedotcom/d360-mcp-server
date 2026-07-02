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
import com.salesforce.data360.mcp.model.request.dataspace.DataSpaceCreateRequest;
import com.salesforce.data360.mcp.model.request.dataspace.DataSpaceMemberRequest;
import com.salesforce.data360.mcp.model.request.dataspace.DataSpacePatchRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Data Space Tools - Manage data spaces and their members.
 * Maps to /data-spaces endpoints.
 */
@Component
public class DataSpaceTools {

    private final Data360Client client;

    public DataSpaceTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all data spaces.
     * Discover all dataspaces in the org.
     */
    @ApiEndpoint(path = "/ssot/data-spaces", verb = "GET")
    @McpTool(
        name = "d360_dataspace_list",
        description = "List all data spaces."
    )
    public String listDataSpaces(
        @McpToolParam(description = "Maximum number of records to return", required = false) Integer batchSize,
        @McpToolParam(description = "Number of records to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            params.put("batchSize", batchSize);
            params.put("offset", offset);
            params.put("orderBy", orderBy);
            String path = ToolUtils.buildPath("/ssot/data-spaces", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a data space.
     * Get dataspace configuration and members.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}", verb = "GET")
    @McpTool(
        name = "d360_dataspace_get",
        description = "Get a data space."
    )
    public String getDataSpace(
        @McpToolParam(description = "The data space name or ID") String dataSpaceNameOrId
    ) {
        try {
            Map result = client.get("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceNameOrId), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a data space.
     * Creates isolated workspace. Tools from other dataspaces must reference it explicitly.
     */
    @ApiEndpoint(path = "/ssot/data-spaces", verb = "POST")
    @McpTool(
        name = "d360_dataspace_create",
        description = "Create a data space."
    )
    public String createDataSpace(
        @McpToolParam(description = "Data space creation request body") DataSpaceCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.post("/ssot/data-spaces", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update a data space.
     * Update metadata.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}", verb = "PATCH")
    @McpTool(
        name = "d360_dataspace_update",
        description = "Update a data space."
    )
    public String updateDataSpace(
        @McpToolParam(description = "The data space name or ID") String dataSpaceNameOrId,
        @McpToolParam(description = "Data space update request body") DataSpacePatchRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.patch("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceNameOrId), body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * List data space members.
     * See who has access to this dataspace.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}/members", verb = "GET")
    @McpTool(
        name = "d360_dataspace_member_list",
        description = "List data space members."
    )
    public String listDataSpaceMembers(
        @McpToolParam(description = "The data space name or ID") String dataSpaceNameOrID,
        @McpToolParam(description = "Maximum number of records to return", required = false) Integer batchSize,
        @McpToolParam(description = "Number of records to skip for pagination", required = false) Integer offset,
        @McpToolParam(description = "Field to order results by", required = false) String orderBy
    ) {
        try {
            Map<String, Object> params = new java.util.LinkedHashMap<>();
            params.put("batchSize", batchSize);
            params.put("offset", offset);
            params.put("orderBy", orderBy);
            String basePath = "/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceNameOrID) + "/members";
            String path = ToolUtils.buildPath(basePath, params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Add a member to a data space.
     * Grant dataspace access to a user with specified role.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}/members", verb = "PUT")
    @McpTool(
        name = "d360_dataspace_member_add",
        description = "Add a member to a data space."
    )
    public String addDataSpaceMember(
        @McpToolParam(description = "The data space name or ID") String dataSpaceNameOrId,
        @McpToolParam(description = "Data space member request body") DataSpaceMemberRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.put("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceNameOrId) + "/members", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Remove members from a data space.
     * Revoke dataspace access from a user.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}/members", verb = "DELETE")
    @McpTool(
        name = "d360_dataspace_member_remove",
        description = "Remove members from a data space (max 10)."
    )
    public String removeDataSpaceMember(
        @McpToolParam(description = "The data space name or ID") String dataSpaceNameOrId,
        @McpToolParam(description = "Comma-separated DLO names to remove (max 10)") String memberNames
    ) {
        try {
            String path = "/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceNameOrId)
                + "/members?memberNames=" + ToolUtils.encodePath(memberNames);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
