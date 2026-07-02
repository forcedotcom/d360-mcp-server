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
package com.salesforce.data360.mcp.tools.datastream;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamCreateRequest;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamPatchRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Data 360 Data Stream Tools - CRUD and run operations for data streams.
 * Maps to /ssot/data-streams endpoints.
 */
@Component
public class DataStreamTools extends AbstractConnectorDataStreamTools {

    public DataStreamTools(Data360Client client) {
        super(client);
    }

    /**
     * List all Data Streams.
     * Returns a list of data stream configurations in the org.
     */
    @ApiEndpoint(path = "/ssot/data-streams", verb = "GET")
    @McpTool(
        name = "d360_datastream_list",
        description = "List all data streams (ingestion pipelines)."
    )
    public String listDataStreams(
        @McpToolParam(description = "Filter by connection name", required = false) String connectionName,
        @McpToolParam(description = "Filter expression", required = false) String filter,
        @McpToolParam(description = "Whether to include mappings in the response", required = false) String includeMappings,
        @McpToolParam(description = "Maximum number of results to return", required = false) Integer limit,
        @McpToolParam(description = "Offset for pagination", required = false) Integer offset,
        @McpToolParam(description = "Order by clause for sorting results", required = false) String orderBy,
        @McpToolParam(description = "Filter by source object name", required = false) String sourceObjectName
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (connectionName != null) params.put("connectionName", connectionName);
            if (filter != null) params.put("filter", filter);
            if (includeMappings != null) params.put("includeMappings", includeMappings);
            if (limit != null) params.put("limit", limit);
            if (offset != null) params.put("offset", offset);
            if (orderBy != null) params.put("orderBy", orderBy);
            if (sourceObjectName != null) params.put("sourceObjectName", sourceObjectName);

            String path = ToolUtils.buildPath("/ssot/data-streams", params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific Data Stream by ID.
     * Returns the full configuration including source object, target DMO, and field mappings.
     */
    @ApiEndpoint(path = "/ssot/data-streams/{id}", verb = "GET")
    @McpTool(
        name = "d360_datastream_get",
        description = "Get details of a specific data stream."
    )
    public String getDataStream(
        @McpToolParam(description = "The data stream record ID or developer name") String dataStreamIdOrName,
        @McpToolParam(description = "Whether to include mappings in the response", required = false) String includeMappings
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (includeMappings != null) params.put("includeMappings", includeMappings);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamIdOrName), params);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new Data Stream.
     * Required fields: name, label, datasource, datastreamType.
     */
    @ApiEndpoint(path = "/ssot/data-streams", verb = "POST")
    @McpTool(
        name = "d360_datastream_create",
        description = "Create a new data stream (generic). "
                + "For Salesforce CRM data streams, use 'd360_datastream_create_sfdc' instead. "
                + "For AWS S3 data streams, use 'd360_datastream_create_aws_s3' instead. "
                + "Both pre-fill connector-specific defaults and require fewer parameters."
    )
    public String createDataStream(
        @McpToolParam(description = "Data stream creation request body") DataStreamCreateRequest request
    ) {
        return super.createDataStream(request);
    }

    /**
     * Update an existing Data Stream.
     * Supports partial updates via PATCH. Only include fields you want to change.
     */
    @ApiEndpoint(path = "/ssot/data-streams/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_datastream_update",
        description = "Update a data stream. "
            + "Note: Some properties returned by d360_datastream_get are read-only and cannot be set via update "
            + "(e.g., 'shouldFetchImmediately', 'lastRunStatus', 'totalRecords'). "
            + "Updatable properties: refreshConfig (refreshMode, frequency, isAccelerationEnabled), "
            + "label, mappings, and sourceFields."
    )
    public String updateDataStream(
        @McpToolParam(description = "The data stream record ID or developer name") String dataStreamIdOrName,
        @McpToolParam(description = "Data stream update request body") DataStreamPatchRequest request
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = "/ssot/data-streams/" + ToolUtils.encodePath(dataStreamIdOrName);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a Data Stream.
     * Removes the data stream configuration. Does not delete the target DMO or data.
     */
    @ApiEndpoint(path = "/ssot/data-streams/{id}", verb = "DELETE")
    @McpTool(
        name = "d360_datastream_delete",
        description = "Delete a data stream."
    )
    public String deleteDataStream(
        @McpToolParam(description = "The data stream record ID or developer name") String dataStreamIdOrName,
        @McpToolParam(description = "Whether to also delete the associated DLO", required = false) Boolean shouldDeleteDataLakeObject
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (shouldDeleteDataLakeObject != null) params.put("shouldDeleteDataLakeObject", shouldDeleteDataLakeObject);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamIdOrName), params);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Run a Data Stream manually.
     * Triggers an immediate refresh of the data stream. Use for testing or manual syncs.
     */
    @ApiEndpoint(path = "/ssot/data-streams/{id}/actions/run", verb = "POST")
    @McpTool(
        name = "d360_datastream_run",
        description = "Trigger a data stream ingestion run. "
            + "NOTE: Salesforce CRM (SalesforceDotCom) data streams cannot be manually triggered — "
            + "they run on the automatic CRM connector sync schedule. "
            + "This tool works for S3, Snowflake, file upload, and other non-CRM data streams."
    )
    public String runDataStream(
        @McpToolParam(description = "The data stream record ID or developer name") String dataStreamIdOrName,
        @McpToolParam(description = "Whether to run interactively", required = false) Boolean interactive
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (interactive != null) params.put("interactive", interactive);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamIdOrName) + "/actions/run", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
