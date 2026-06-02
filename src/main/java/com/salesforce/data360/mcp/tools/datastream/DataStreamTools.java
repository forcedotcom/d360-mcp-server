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
        @McpToolParam(description = "Filter by category (e.g., 'profile', 'engagement')", required = false) String category,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (category != null) params.put("category", category);
            if (dataspace != null) params.put("dataspace", dataspace);

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
        @McpToolParam(description = "The data stream ID or name") String dataStreamId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamId), params);
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
        @McpToolParam(description = "Data stream creation request body") DataStreamCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        return super.createDataStream(request, dataspace);
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
        @McpToolParam(description = "The data stream ID or name") String dataStreamId,
        @McpToolParam(description = "Data stream update request body") DataStreamPatchRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamId), params);
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
        @McpToolParam(description = "The data stream record ID (e.g. 1dsaj000000qaH7AAI)") String dataStreamId,
        @McpToolParam(description = "Whether to also delete the associated DLO. Defaults to true.", required = false) Boolean shouldDeleteDataLakeObject,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shouldDeleteDataLakeObject", shouldDeleteDataLakeObject != null ? shouldDeleteDataLakeObject : true);
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamId), params);
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
    @ApiEndpoint(path = "/ssot/data-streams/{id}/run", verb = "POST")
    @McpTool(
        name = "d360_datastream_run",
        description = "Trigger a data stream ingestion run. "
            + "NOTE: Salesforce CRM (SalesforceDotCom) data streams cannot be manually triggered — "
            + "they run on the automatic CRM connector sync schedule. "
            + "This tool works for S3, Snowflake, file upload, and other non-CRM data streams."
    )
    public String runDataStream(
        @McpToolParam(description = "The data stream ID or name") String dataStreamId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-streams/" + ToolUtils.encodePath(dataStreamId) + "/actions/run", params);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
