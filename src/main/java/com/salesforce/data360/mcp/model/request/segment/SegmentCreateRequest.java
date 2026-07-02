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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.Map;

/**
 * Request body for {@code POST /ssot/segments}.
 *
 * <p>Mirrors {@code CdpSegmentInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SegmentCreateRequest {

    @McpToolParam(description = "Map of additional metadata", required = false)
    private Map<String, String> additionalMetadata;

    @McpToolParam(description = "Component Template Name for the Segment", required = false)
    private String componentTemplateName;

    @McpToolParam(description = "DataKit Name for the Segment", required = false)
    private String dataKitDevName;

    @McpToolParam(description = "Segment Description")
    private String description;

    @NotBlank
    @McpToolParam(description = "Segment Developer Name")
    private String developerName;

    @NotBlank
    @McpToolParam(description = "Segment Display Name")
    private String displayName;

    @McpToolParam(description = "EinsteinGptSegmentsUI segment criteria", required = false)
    private SegmentEinsteinGptInput einsteinGptSegmentsUICriteria;

    @McpToolParam(description = "Exclude criteria", required = false)
    private String excludeCriteria;

    @McpToolParam(description = "Group Sort Limit criteria", required = false)
    private String groupSortLimitFilterCriteria;

    @McpToolParam(description = "Include criteria", required = false)
    private String includeCriteria;

    @McpToolParam(description = "Segment data build tool (DBT).", required = false)
    private SegmentDbtInput includeDbt;

    @McpToolParam(description = "Lookalike segment criteria", required = false)
    private SegmentLookalikeInput lookalikeCriteria;

    @McpToolParam(description = "lookback period for this segment", required = false)
    private String lookbackPeriod;

    @McpToolParam(description = "Publish schedule in hours for segment. One of NoRefresh, One, Two, Four, Six, Twelve, TwentyFour", required = false)
    private String publishSchedule;

    @McpToolParam(description = "Date indicating the end of the publish schedule. Optional if publishSchedule is unspecified.", required = false)
    private String publishScheduleEndDate;

    @McpToolParam(description = "Datetime indicating End of the publish schedule", required = false)
    private String publishScheduleEndDateTime;

    @McpToolParam(description = "Segment additional schedule", required = false)
    private SegmentScheduleInput publishScheduleInfo;

    @McpToolParam(description = "Datetime indicating the start of the publish schedule. Optional if publishSchedule is unspecified.", required = false)
    private String publishScheduleStartDateTime;

    @McpToolParam(description = "Segment Creation Flow. One of Datakit, EinsteinGpt, Visual", required = false)
    private String segmentCreationFlow;

    @NotBlank
    @McpToolParam(description = "API name of the SegmentOn entity.")
    private String segmentOnApiName;

    @McpToolParam(description = "Data graph of the SegmentOn entity for real-time segments.", required = false)
    private String segmentOnDataGraph;

    @NotBlank
    @McpToolParam(description = "Segment Type. One of Dbt, Dynamic, EinsteinGptSegmentsUI, Lookalike, Realtime, Ui, Waterfall")
    private String segmentType;

    public Map<String, String> getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(Map<String, String> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public SegmentEinsteinGptInput getEinsteinGptSegmentsUICriteria() {
        return einsteinGptSegmentsUICriteria;
    }

    public void setEinsteinGptSegmentsUICriteria(SegmentEinsteinGptInput einsteinGptSegmentsUICriteria) {
        this.einsteinGptSegmentsUICriteria = einsteinGptSegmentsUICriteria;
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

    public String getIncludeCriteria() {
        return includeCriteria;
    }

    public void setIncludeCriteria(String includeCriteria) {
        this.includeCriteria = includeCriteria;
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

    public String getLookbackPeriod() {
        return lookbackPeriod;
    }

    public void setLookbackPeriod(String lookbackPeriod) {
        this.lookbackPeriod = lookbackPeriod;
    }

    public String getPublishSchedule() {
        return publishSchedule;
    }

    public void setPublishSchedule(String publishSchedule) {
        this.publishSchedule = publishSchedule;
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

    public SegmentScheduleInput getPublishScheduleInfo() {
        return publishScheduleInfo;
    }

    public void setPublishScheduleInfo(SegmentScheduleInput publishScheduleInfo) {
        this.publishScheduleInfo = publishScheduleInfo;
    }

    public String getPublishScheduleStartDateTime() {
        return publishScheduleStartDateTime;
    }

    public void setPublishScheduleStartDateTime(String publishScheduleStartDateTime) {
        this.publishScheduleStartDateTime = publishScheduleStartDateTime;
    }

    public String getSegmentCreationFlow() {
        return segmentCreationFlow;
    }

    public void setSegmentCreationFlow(String segmentCreationFlow) {
        this.segmentCreationFlow = segmentCreationFlow;
    }

    public String getSegmentOnApiName() {
        return segmentOnApiName;
    }

    public void setSegmentOnApiName(String segmentOnApiName) {
        this.segmentOnApiName = segmentOnApiName;
    }

    public String getSegmentOnDataGraph() {
        return segmentOnDataGraph;
    }

    public void setSegmentOnDataGraph(String segmentOnDataGraph) {
        this.segmentOnDataGraph = segmentOnDataGraph;
    }

    public String getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(String segmentType) {
        this.segmentType = segmentType;
    }
}
