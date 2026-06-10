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
    public String listDataSpaces() {
        try {
            Map result = client.get("/ssot/data-spaces", Map.class);
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
        @McpToolParam(description = "The data space name") String dataSpaceName
    ) {
        try {
            Map result = client.get("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName), Map.class);
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
        @McpToolParam(description = "The data space name") String dataSpaceName,
        @McpToolParam(description = "Data space update request body") DataSpacePatchRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.patch("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName), body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a data space.
     * Deletes dataspace and all contents. Irreversible.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}", verb = "DELETE")
    @McpTool(
        name = "d360_dataspace_delete",
        description = "Delete a data space."
    )
    public String deleteDataSpace(
        @McpToolParam(description = "The data space name") String dataSpaceName
    ) {
        try {
            Map result = client.delete("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName), Map.class);
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
        @McpToolParam(description = "The data space name") String dataSpaceName
    ) {
        try {
            Map result = client.get("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName) + "/members", Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Add a member to a data space.
     * Grant dataspace access to a user with specified role.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}/members", verb = "POST")
    @McpTool(
        name = "d360_dataspace_member_add",
        description = "Add a member to a data space."
    )
    public String addDataSpaceMember(
        @McpToolParam(description = "The data space name") String dataSpaceName,
        @McpToolParam(description = "Data space member request body") DataSpaceMemberRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            Map result = client.put("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName) + "/members", body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Remove a member from a data space.
     * Revoke dataspace access from a user.
     */
    @ApiEndpoint(path = "/ssot/data-spaces/{name}/members/{memberId}", verb = "DELETE")
    @McpTool(
        name = "d360_dataspace_member_remove",
        description = "Remove a member from a data space."
    )
    public String removeDataSpaceMember(
        @McpToolParam(description = "The data space name") String dataSpaceName,
        @McpToolParam(description = "The member ID to remove") String memberId
    ) {
        try {
            Map result = client.delete("/ssot/data-spaces/" + ToolUtils.encodePath(dataSpaceName) + "/members/" + ToolUtils.encodePath(memberId), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
