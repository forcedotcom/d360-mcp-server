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
import com.salesforce.data360.mcp.model.request.dlo.DloCreateRequest;
import com.salesforce.data360.mcp.model.request.dlo.DloPatchRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data 360 Data Lake Object (DLO) Tools - CRUD operations for DLOs.
 * Maps to /ssot/data-lake-objects/* endpoints.
 */
@Component
public class DloTools {

    private final Data360Client client;

    public DloTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all Data Lake Objects (DLOs) - raw ingested data sources.
     * Essential for discovering DLO names and field API names before creating mappings.
     */
    @McpTool(
        name = "d360_dlo_list",
        description = "List all Data Lake Objects (DLOs) - raw ingested data sources. Essential for discovering DLO names and field API names before creating mappings. Filter by category (Profile, Engagement, Other) to narrow results."
    )
    public String listDataLakeObjects(
        @McpToolParam(description = "Filter by category: Profile, Engagement, Other", required = false) String category,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (category != null) params.put("category", category);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-lake-objects", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get ONE DLO with all field definitions by API name.
     * PREREQUISITE for creating mappings - returns exact field API names needed for sourceFieldName values.
     */
    @McpTool(
        name = "d360_dlo_get",
        description = "Get ONE DLO with all field definitions by API name. PREREQUISITE for creating mappings - returns exact field API names needed for sourceFieldName values. Pair with d360_dmo_get for target field names."
    )
    public String getDataLakeObject(
        @McpToolParam(description = "DLO API name (e.g., 'Account_00D000000000000__dll')") String dloName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-lake-objects/" + ToolUtils.encodePath(dloName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new Data Lake Object.
     * Typically created automatically via data stream ingestion, but can be created manually for custom use cases.
     */
    @McpTool(
        name = "d360_dlo_create",
        description = "Create a new Data Lake Object. Typically created automatically via data stream ingestion, but can be created manually for custom use cases. Specify fields, types, and category."
    )
    public String createDataLakeObject(
        @McpToolParam(description = "DLO creation request body") DloCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-lake-objects", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing Data Lake Object.
     * Use this to modify a DLO's schema - add or change fields, update labels, or adjust field types.
     */
    @McpTool(
        name = "d360_dlo_update",
        description = "Update an existing Data Lake Object. Use this to modify a DLO's schema - add or change fields, update labels, or adjust field types."
    )
    public String updateDataLakeObject(
        @McpToolParam(description = "DLO API name") String dloName,
        @McpToolParam(description = "DLO update request body") DloPatchRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-lake-objects/" + ToolUtils.encodePath(dloName), params);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a Data Lake Object.
     * Use this to remove a DLO and its data from the data lake. Ensure no mappings or data streams reference it before deleting.
     */
    @McpTool(
        name = "d360_dlo_delete",
        description = "Delete a Data Lake Object. Use this to remove a DLO and its data from the data lake. Ensure no mappings or data streams reference it before deleting."
    )
    public String deleteDataLakeObject(
        @McpToolParam(description = "DLO API name") String dloName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-lake-objects/" + ToolUtils.encodePath(dloName), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
