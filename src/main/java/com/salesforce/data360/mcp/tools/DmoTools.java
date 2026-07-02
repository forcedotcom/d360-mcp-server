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
import com.salesforce.data360.mcp.model.request.dmo.DmoCreateRequest;
import com.salesforce.data360.mcp.model.request.dmo.DmoUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 Data Model Object (DMO) Tools - CRUD operations for DMOs.
 * Maps to /ssot/data-model-objects/* endpoints.
 */
@Component
public class DmoTools {

    private final Data360Client client;

    public DmoTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all Data Model Objects (DMOs).
     * Use this to discover all DMOs in the org. Filter by category to narrow results.
     */
    @ApiEndpoint(path = "/ssot/data-model-objects", verb = "GET")
    @McpTool(
        name = "d360_dmo_list",
        description = "List all Data Model Objects (DMOs). Filter by category to narrow results. "
            + "Use category='DirectoryTable' for unstructured file/document DMOs (relevant for search index with fileLevelConfiguration). "
            + "IMPORTANT: isEnabled=false does NOT mean the DMO is disabled — it CAN still be used for creating mappings."
    )
    public String listDataModelObjects(
        @McpToolParam(description = "Filter by category: Profile, Engagement, Other, DirectoryTable. "
            + "Use 'DirectoryTable' for unstructured file DMOs.", required = false) String dataObjectCategory,
        @McpToolParam(description = "Dataspace name", required = false) String dataSpaceName,
        @McpToolParam(description = "Number of results to return (1-50, default 50)", required = false) Integer limit,
        @McpToolParam(description = "Number of results to skip", required = false) Integer offset,
        @McpToolParam(description = "Sorting order: ASC or DESC", required = false) String orderBy,
        @McpToolParam(description = "Filter by creation type", required = false) String creationType,
        @McpToolParam(description = "Filter by DMO status", required = false) String dataModelObjectStatus,
        @McpToolParam(description = "Fields to include in response", required = false) String includeFields,
        @McpToolParam(description = "Include status information", required = false) String includeStatus,
        @McpToolParam(description = "Filter by enabled state", required = false) String isEnabled,
        @McpToolParam(description = "Filter by marketing data connection ID", required = false) String mktDataConnectionId,
        @McpToolParam(description = "Search term to filter results", required = false) String search
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataObjectCategory != null) params.put("dataObjectCategory", dataObjectCategory);
            if (dataSpaceName != null) params.put("dataSpaceName", dataSpaceName);
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (orderBy != null) params.put("orderBy", orderBy);
            if (creationType != null) params.put("creationType", creationType);
            if (dataModelObjectStatus != null) params.put("dataModelObjectStatus", dataModelObjectStatus);
            if (includeFields != null) params.put("includeFields", includeFields);
            if (includeStatus != null) params.put("includeStatus", includeStatus);
            if (isEnabled != null) params.put("isEnabled", isEnabled);
            if (mktDataConnectionId != null) params.put("mktDataConnectionId", mktDataConnectionId);
            if (search != null) params.put("search", search);

            String path = ToolUtils.buildPath("/ssot/data-model-objects", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific Data Model Object by name with all field definitions.
     * Use this to get full schema of a DMO including all fields and their types.
     */
    @ApiEndpoint(path = "/ssot/data-model-objects/{dmoName}", verb = "GET")
    @McpTool(
        name = "d360_dmo_get",
        description = "Get a specific Data Model Object by name with all field definitions. "
            + "The response includes category — 'DirectoryTable' indicates an unstructured file DMO. "
            + "IMPORTANT: isEnabled=false does NOT mean the DMO is disabled — it CAN still be used for creating mappings."
    )
    public String getDataModelObject(
        @McpToolParam(description = "DMO API name (e.g., 'Individual__dlm')") String dmoName,
        @McpToolParam(description = "Filter to active fields only", required = false) String includeActiveFieldsOnly,
        @McpToolParam(description = "Include status information", required = false) String includeStatus
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (includeActiveFieldsOnly != null) params.put("includeActiveFieldsOnly", includeActiveFieldsOnly);
            if (includeStatus != null) params.put("includeStatus", includeStatus);

            String path = ToolUtils.buildPath("/ssot/data-model-objects/" + ToolUtils.encodePath(dmoName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new Data Model Object.
     * Use this to define a custom DMO when standard objects don't cover your data model.
     */
    @ApiEndpoint(path = "/ssot/data-model-objects", verb = "POST")
    @McpTool(
        name = "d360_dmo_create",
        description = "Create a new Data Model Object. "
            + "IMPORTANT: Do NOT include the '__dlm' suffix in the name — it is auto-appended by the system. "
            + "For example, use 'CaseComment' not 'CaseComment__dlm'. "
            + "Valid categories: 'Profile', 'Engagement', 'Other', 'Segment_Membership'. "
            + "For Engagement DMOs, the 'eventDateTimeFieldName' field is required. "
            + "The 'fields' parameter with at least one primary key field is required for creation to succeed."
    )
    public String createDataModelObject(
        @McpToolParam(description = "DMO creation request body") DmoCreateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/data-model-objects";
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing Data Model Object.
     * Use this to modify a DMO's label or add/update field definitions.
     */
    @ApiEndpoint(path = "/ssot/data-model-objects/{dmoName}", verb = "PATCH")
    @McpTool(
        name = "d360_dmo_update",
        description = "Update an existing Data Model Object."
    )
    public String updateDataModelObject(
        @McpToolParam(description = "DMO API name") String dmoName,
        @McpToolParam(description = "DMO update request body") DmoUpdateRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/data-model-objects/" + ToolUtils.encodePath(dmoName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a Data Model Object.
     * Use this to remove a custom DMO. Ensure no data streams, mappings, CIs, or segments reference it before deleting.
     */
    @ApiEndpoint(path = "/ssot/data-model-objects/{dmoName}", verb = "DELETE")
    @McpTool(
        name = "d360_dmo_delete",
        description = "Delete a Data Model Object."
    )
    public String deleteDataModelObject(
        @McpToolParam(description = "DMO API name") String dmoName
    ) {
        try {
            String path = "/ssot/data-model-objects/" + ToolUtils.encodePath(dmoName);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
