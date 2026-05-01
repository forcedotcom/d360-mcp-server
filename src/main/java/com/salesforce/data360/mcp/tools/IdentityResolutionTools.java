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
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionCreateRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionFullUpdateRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionPatchRequest;
import com.salesforce.data360.mcp.model.request.identityresolution.IdentityResolutionRunRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Identity Resolution Tools - Manage identity resolution rulesets.
 * Maps to /ssot/identity-resolutions endpoints.
 */
@Component
public class IdentityResolutionTools {

    private final Data360Client client;

    public IdentityResolutionTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all identity resolution rulesets.
     * Discover identity resolution configurations.
     */
    @McpTool(
        name = "d360_ir_list",
        description = "List all identity resolution rulesets."
    )
    public String listIdentityResolutions(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/identity-resolutions", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get an identity resolution ruleset.
     * Get full ruleset definition.
     */
    @McpTool(
        name = "d360_ir_get",
        description = "Get an identity resolution ruleset."
    )
    public String getIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create an identity resolution ruleset.
     * Defines how individuals are matched across sources. Rules execute in priority order.
     */
    @McpTool(
        name = "d360_ir_create",
        description = "Create an identity resolution ruleset."
    )
    public String createIdentityResolution(
        IdentityResolutionCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/identity-resolutions", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an identity resolution ruleset.
     * Partial updates. Changes apply after republish.
     */
    @McpTool(
        name = "d360_ir_update",
        description = "Update an identity resolution ruleset."
    )
    public String updateIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        IdentityResolutionPatchRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Full replacement of an identity resolution ruleset.
     * Replaces entire ruleset. More destructive than PATCH.
     */
    @McpTool(
        name = "d360_ir_full_update",
        description = "Full replacement of an identity resolution ruleset."
    )
    public String fullUpdateIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        IdentityResolutionFullUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId), dataspace);
            Map result = client.put(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete an identity resolution ruleset.
     * Deletes ruleset. Active resolutions may be affected.
     */
    @McpTool(
        name = "d360_ir_delete",
        description = "Delete an identity resolution ruleset."
    )
    public String deleteIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Publish identity resolution config.
     * Activates ruleset for identity resolution.
     */
    @McpTool(
        name = "d360_ir_publish",
        description = "Publish identity resolution config."
    )
    public String publishIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId) + "/actions/publish", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Run identity resolution.
     * Trigger immediate identity resolution matching.
     */
    @McpTool(
        name = "d360_ir_run",
        description = "Run identity resolution."
    )
    public String runIdentityResolution(
        @McpToolParam(description = "The identity resolution ID") String identityResolutionId,
        IdentityResolutionRunRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            String path = ToolUtils.buildPath("/ssot/identity-resolutions/" + ToolUtils.encodePath(identityResolutionId) + "/actions/run-now", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
