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
import com.salesforce.data360.mcp.model.request.segment.SegmentCreateRequest;
import com.salesforce.data360.mcp.model.request.segment.SegmentUpdateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 Segment Tools - segment management and publishing.
 * Maps to /ssot/segments endpoints.
 */
@Component
public class SegmentTools {

    private final Data360Client client;

    public SegmentTools(Data360Client client) {
        this.client = client;
    }

    /**
     * List all segments in the organization.
     */
    @ApiEndpoint(path = "/ssot/segments", verb = "GET")
    @McpTool(
        name = "d360_segment_list",
        description = "List all segments."
    )
    public String listSegments(
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments", dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Get a specific segment by ID.
     * Returns segment definition, status, and configuration.
     */
    @ApiEndpoint(path = "/ssot/segments/{id}", verb = "GET")
    @McpTool(
        name = "d360_segment_get",
        description = "Get a segment by ID."
    )
    public String getSegment(
        @McpToolParam(description = "The segment ID") String segmentId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentId), dataspace);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create a new segment.
     * Requires displayName, segmentOnApiName, segmentType, and other configuration.
     */
    @ApiEndpoint(path = "/ssot/segments", verb = "POST")
    @McpTool(
        name = "d360_segment_create",
        description = "Create a new segment. DEPENDENCY: All CIs referenced in segment filters must be in ACTIVE status before creation. To create UI-type segments via API, use this pattern: { displayName, segmentOnApiName, segmentType: 'Ui', segmentCreationFlow: 'Datakit', publishScheduleStartDateTime, publishScheduleEndDate, includeDbt.models.models[].sql }. Key rules: segmentType must be 'Ui' with segmentCreationFlow 'Datakit', SQL goes in includeDbt.models.models[].sql (nested models wrapper), publishSchedule values: NoRefresh, One, Two, Four, Six, Twelve, TwentyFour, dataSpace goes as query param (dataspace tool parameter), NOT in the body"
    )
    public String createSegment(
        SegmentCreateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = ToolUtils.buildPath("/ssot/segments", dataspace);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Update an existing segment.
     * Can update displayName, publishSchedule, and other metadata.
     */
    @ApiEndpoint(path = "/ssot/segments/{id}", verb = "PATCH")
    @McpTool(
        name = "d360_segment_update",
        description = "Update a segment."
    )
    public String updateSegment(
        @McpToolParam(description = "The segment ID to update") String segmentId,
        SegmentUpdateRequest request,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);

            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentId), dataspace);
            Map result = client.patch(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Delete a segment by API name.
     * The path requires the segment API name (developer name), not the ID.
     * Irreversible.
     */
    @ApiEndpoint(path = "/ssot/segments/{apiName}", verb = "DELETE")
    @McpTool(
        name = "d360_segment_delete",
        description = "Delete a segment by API name (developer name). Irreversible."
    )
    public String deleteSegment(
        @McpToolParam(description = "The segment API name (developer name)") String segmentApiName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentApiName), dataspace);
            Map result = client.delete(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Publish/activate a segment.
     * Triggers segment calculation and activation.
     */
    @ApiEndpoint(path = "/ssot/segments/{id}/actions/publish", verb = "POST")
    @McpTool(
        name = "d360_segment_publish",
        description = "Publish/activate a segment."
    )
    public String publishSegment(
        @McpToolParam(description = "The segment ID to publish") String segmentId,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentId) + "/actions/publish", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Deactivate a segment by API name. Inverse of publish.
     */
    @ApiEndpoint(path = "/ssot/segments/{apiName}/actions/deactivate", verb = "POST")
    @McpTool(
        name = "d360_segment_deactivate",
        description = "Deactivate a segment by API name (developer name). Inverse of publish."
    )
    public String deactivateSegment(
        @McpToolParam(description = "The segment API name (developer name)") String segmentApiName,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            String path = ToolUtils.buildPath("/ssot/segments/" + ToolUtils.encodePath(segmentApiName) + "/actions/deactivate", dataspace);
            Map result = client.post(path, Map.of(), Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
