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

import com.salesforce.data360.mcp.model.request.smart.FieldInput;
import com.salesforce.data360.mcp.model.request.smart.FieldOverrideInput;
import com.salesforce.data360.mcp.service.SmartService;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Smart Tools - Higher-level tools that add intelligence on top of raw Data 360 APIs.
 * Thin MCP adapter that delegates all business logic to {@link SmartService}.
 *
 * Smart Data Stream: auto-selects the best event date column for engagement streams
 * using date field heuristics.
 *
 * Standard mapping preview and bulk creation are handled by {@link StandardMappingTools}.
 */
@Component
public class SmartTools {

    private final SmartService smartService;

    public SmartTools(SmartService smartService) {
        this.smartService = smartService;
    }

    /**
     * Suggest field mappings between DLO and DMO using Jaccard similarity.
     * Returns analysis and mapping payload ready for d360_dmo_mapping_create.
     */
    @McpTool(
        name = "d360_smart_mapping_suggest",
        description = "Auto-generate DLO-to-DMO field mappings by matching fields using name/label similarity. "
            + "Returns a ready-to-use mapping payload. "
            + "IMPORTANT: Call d360_standard_mapping_preview first — if a standard mapping exists for the source object, "
            + "use d360_standard_mapping_create instead. Only use this tool for custom objects without standard mappings."
    )
    public String smartMappingSuggest(
        @McpToolParam(description = "Source DLO fields") List<FieldInput> sourceFields,
        @McpToolParam(description = "Target DMO fields") List<FieldInput> targetFields,
        @McpToolParam(description = "Source DLO name") String sourceDloName,
        @McpToolParam(description = "Target DMO name") String targetDmoName,
        @McpToolParam(description = "Minimum similarity threshold (0.0-1.0)", required = false) Double threshold,
        @McpToolParam(description = "Field overrides for explicit source-to-target mappings", required = false) List<FieldOverrideInput> fieldOverrides
    ) {
        return smartService.smartMappingSuggest(
            toFieldMaps(sourceFields), toFieldMaps(targetFields),
            sourceDloName, targetDmoName, threshold, toOverrideMaps(fieldOverrides));
    }

    /**
     * Preview DLO-to-DMO field matches with confidence scores without creating anything.
     * Use before d360_smart_mapping_suggest to validate.
     */
    @McpTool(
        name = "d360_preview_field_matches",
        description = "Preview DLO-to-DMO field matches with confidence scores without creating anything. Use before d360_smart_mapping_suggest to validate."
    )
    public String previewFieldMatches(
        @McpToolParam(description = "Source DLO fields") List<FieldInput> sourceFields,
        @McpToolParam(description = "Target DMO fields") List<FieldInput> targetFields,
        @McpToolParam(description = "Source DLO name") String sourceDloName,
        @McpToolParam(description = "Target DMO name") String targetDmoName,
        @McpToolParam(description = "Minimum similarity threshold (0.0-1.0)", required = false) Double threshold
    ) {
        return smartService.previewFieldMatches(
            toFieldMaps(sourceFields), toFieldMaps(targetFields),
            sourceDloName, targetDmoName, threshold);
    }

    /**
     * Smart data stream creation with auto-selected event date column.
     * Analyzes fields and recommends the best event date column for engagement streams.
     */
    @McpTool(
        name = "d360_smart_datastream_create",
        description = "Enhance a data stream creation body with intelligent event date column selection. Critical for Engagement category streams. "
            + "For DLO-to-DMO field mappings, use d360_standard_mapping_preview to review and d360_standard_mapping_create to create all mappings in one call."
    )
    public String smartDatastreamCreate(
        @McpToolParam(description = "Data stream body as JSON string") String bodyJson,
        @McpToolParam(description = "Auto-select event date column (default: true)", required = false) Boolean autoSelectEventDate
    ) {
        return smartService.smartDatastreamCreate(bodyJson, autoSelectEventDate);
    }

    /**
     * Recommend the best event date column for a given set of fields.
     */
    @McpTool(
        name = "d360_event_date_recommend",
        description = "Analyze an existing data stream and recommend the best event date column. Shows all date fields ranked by suitability."
    )
    public String eventDateRecommend(
        @McpToolParam(description = "Fields as JSON array string") String fieldsJson,
        @McpToolParam(description = "Category (e.g., 'Engagement', 'Profile')", required = false) String category
    ) {
        return smartService.eventDateRecommend(fieldsJson, category);
    }

    private static List<Map<String, Object>> toFieldMaps(List<FieldInput> fields) {
        return fields.stream().map(JsonUtil::toMap).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> toOverrideMaps(List<FieldOverrideInput> overrides) {
        if (overrides == null) return null;
        return overrides.stream().map(JsonUtil::toMap).collect(Collectors.toList());
    }
}
