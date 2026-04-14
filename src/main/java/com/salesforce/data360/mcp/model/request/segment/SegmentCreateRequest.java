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
package com.salesforce.data360.mcp.model.request.segment;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Request body for creating a segment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentCreateRequest {

    @NotBlank
    @McpToolParam(description = "Display name for the segment")
    private String displayName;

    @NotBlank
    @McpToolParam(description = "API name of the entity to segment on (e.g., 'UnifiedIndividual__dlm')")
    private String segmentOnApiName;

    @NotBlank
    @McpToolParam(description = "Segment type: Dbt, Dynamic, EinsteinGptSegmentsUI, Lookalike, Realtimez, Waterfall")
    private String segmentType;

    @McpToolParam(description = "Description of the segment")
    private String description;

    @McpToolParam(description = "Developer name", required = false)
    private String developerName;

    @McpToolParam(description = "Data space", required = false)
    private String dataSpace;

    @McpToolParam(description = "Publish schedule: NoRefresh, One, Two, Four, Six, Twelve, TwentyFour", required = false)
    private String publishSchedule;

    @McpToolParam(description = "Publish schedule start date/time (ISO 8601)", required = false)
    private String publishScheduleStartDateTime;

    @McpToolParam(description = "Publish schedule end date (ISO 8601)", required = false)
    private String publishScheduleEndDate;

    @McpToolParam(description = "Publish schedule end date/time (ISO 8601)", required = false)
    private String publishScheduleEndDateTime;

    @McpToolParam(description = "Publish schedule info: {interval, frequency, daysOfWeek: {daysOfWeek: []}}", required = false)
    private Map<String, Object> publishScheduleInfo;

    @McpToolParam(description = "Creation flow: Datakit, EinsteinGpt, Visual", required = false)
    private String segmentCreationFlow;

    @McpToolParam(description = "Include criteria", required = false)
    private String includeCriteria;

    @McpToolParam(description = "Exclude criteria", required = false)
    private String excludeCriteria;

    @McpToolParam(description = "Group sort limit filter criteria", required = false)
    private String groupSortLimitFilterCriteria;

    @McpToolParam(description = "Lookback period", required = false)
    private String lookbackPeriod;

    @McpToolParam(description = "Segment on data graph", required = false)
    private String segmentOnDataGraph;

    @McpToolParam(description = "Component template name", required = false)
    private String componentTemplateName;

    @McpToolParam(description = "Data kit developer name", required = false)
    private String dataKitDevName;

    @McpToolParam(description = "Additional metadata", required = false)
    private Map<String, Object> additionalMetadata;

    @McpToolParam(description = "dbt configuration for SQL-based segments", required = false)
    private SegmentDbtInput includeDbt;

    @McpToolParam(description = "Lookalike criteria", required = false)
    private SegmentLookalikeInput lookalikeCriteria;

    @McpToolParam(description = "Einstein GPT criteria", required = false)
    private SegmentEinsteinGptInput einsteinGptSegmentsUICriteria;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSegmentOnApiName() {
        return segmentOnApiName;
    }

    public void setSegmentOnApiName(String segmentOnApiName) {
        this.segmentOnApiName = segmentOnApiName;
    }

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDataSpace() {
        return dataSpace;
    }

    public void setDataSpace(String dataSpace) {
        this.dataSpace = dataSpace;
    }

    public String getPublishSchedule() {
        return publishSchedule;
    }

    public void setPublishSchedule(String publishSchedule) {
        this.publishSchedule = publishSchedule;
    }

    public String getPublishScheduleStartDateTime() {
        return publishScheduleStartDateTime;
    }

    public void setPublishScheduleStartDateTime(String publishScheduleStartDateTime) {
        this.publishScheduleStartDateTime = publishScheduleStartDateTime;
    }

    public String getPublishScheduleEndDate() {
        return publishScheduleEndDate;
    }

    public void setPublishScheduleEndDate(String publishScheduleEndDate) {
        this.publishScheduleEndDate = publishScheduleEndDate;
    }

    public String getPublishScheduleEndDateTime() {
        return publishScheduleEndDateTime;
    }

    public void setPublishScheduleEndDateTime(String publishScheduleEndDateTime) {
        this.publishScheduleEndDateTime = publishScheduleEndDateTime;
    }

    public Map<String, Object> getPublishScheduleInfo() {
        return publishScheduleInfo;
    }

    public void setPublishScheduleInfo(Map<String, Object> publishScheduleInfo) {
        this.publishScheduleInfo = publishScheduleInfo;
    }

    public String getSegmentCreationFlow() {
        return segmentCreationFlow;
    }

    public void setSegmentCreationFlow(String segmentCreationFlow) {
        this.segmentCreationFlow = segmentCreationFlow;
    }

    public String getIncludeCriteria() {
        return includeCriteria;
    }

    public void setIncludeCriteria(String includeCriteria) {
        this.includeCriteria = includeCriteria;
    }

    public String getExcludeCriteria() {
        return excludeCriteria;
    }

    public void setExcludeCriteria(String excludeCriteria) {
        this.excludeCriteria = excludeCriteria;
    }

    public String getGroupSortLimitFilterCriteria() {
        return groupSortLimitFilterCriteria;
    }

    public void setGroupSortLimitFilterCriteria(String groupSortLimitFilterCriteria) {
        this.groupSortLimitFilterCriteria = groupSortLimitFilterCriteria;
    }

    public String getLookbackPeriod() {
        return lookbackPeriod;
    }

    public void setLookbackPeriod(String lookbackPeriod) {
        this.lookbackPeriod = lookbackPeriod;
    }

    public String getSegmentOnDataGraph() {
        return segmentOnDataGraph;
    }

    public void setSegmentOnDataGraph(String segmentOnDataGraph) {
        this.segmentOnDataGraph = segmentOnDataGraph;
    }

    public String getComponentTemplateName() {
        return componentTemplateName;
    }

    public void setComponentTemplateName(String componentTemplateName) {
        this.componentTemplateName = componentTemplateName;
    }

    public String getDataKitDevName() {
        return dataKitDevName;
    }

    public void setDataKitDevName(String dataKitDevName) {
        this.dataKitDevName = dataKitDevName;
    }

    public Map<String, Object> getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(Map<String, Object> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }

    public SegmentDbtInput getIncludeDbt() {
        return includeDbt;
    }

    public void setIncludeDbt(SegmentDbtInput includeDbt) {
        this.includeDbt = includeDbt;
    }

    public SegmentLookalikeInput getLookalikeCriteria() {
        return lookalikeCriteria;
    }

    public void setLookalikeCriteria(SegmentLookalikeInput lookalikeCriteria) {
        this.lookalikeCriteria = lookalikeCriteria;
    }

    public SegmentEinsteinGptInput getEinsteinGptSegmentsUICriteria() {
        return einsteinGptSegmentsUICriteria;
    }

    public void setEinsteinGptSegmentsUICriteria(SegmentEinsteinGptInput einsteinGptSegmentsUICriteria) {
        this.einsteinGptSegmentsUICriteria = einsteinGptSegmentsUICriteria;
    }
}
