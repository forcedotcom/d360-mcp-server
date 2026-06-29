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
package com.salesforce.data360.mcp.model.request.calculatedinsight;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for updating a Calculated Insight.
 * All fields are optional for partial updates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculatedInsightUpdateRequest {

    @McpToolParam(description = "API name for the calculated insight (must end with __cio)", required = false)
    private String apiName;

    @McpToolParam(description = "Display name (label) for the calculated insight", required = false)
    private String displayName;

    @McpToolParam(description = "Calculated insight definition type. One of CALCULATED_METRIC, EXTERNAL_METRIC, STREAMING_METRIC, GRAPH_METRIC, HISTORY_METRIC", required = false)
    private String definitionType;

    @McpToolParam(description = "Calculated insight ansi sql statement/expression", required = false)
    private String expression;

    @McpToolParam(description = "Publish schedule interval: ExternallyManaged, NotScheduled, One, Six, Streaming, SystemManaged, Twelve, TwentyFour", required = false)
    private String publishScheduleInterval;

    @McpToolParam(description = "Calculated insight publish schedule start date time - Expected format: yyyy-MM-ddTHH:mm", required = false)
    private String publishScheduleStartDateTime;

    @McpToolParam(description = "Calculated insight publish schedule end date - Expected format: yyyy-MM-dd", required = false)
    private String publishScheduleEndDate;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Flag to identify Save as draft flow. If true, the calculated insight will get saved as draft.", required = false)
    private Boolean draft;

    @McpToolParam(description = "Flag to identify the ci creation thru package. If true, the ci is created from a installed package", required = false)
    private Boolean createdFromPackage;

    @McpToolParam(description = "Packaged calculated insight API name", required = false)
    private String packagedCalculatedInsightApiName;

    @McpToolParam(description = "History publish schedule interval. One of Daily, ExternallyManaged, Streaming, SystemManaged.", required = false)
    private String historyPublishScheduleInterval;

    @McpToolParam(description = "History publish time", required = false)
    private String historyPublishTime;

    @McpToolParam(description = "Source developer name", required = false)
    private String sourceDeveloperName;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getPublishScheduleInterval() {
        return publishScheduleInterval;
    }

    public void setPublishScheduleInterval(String publishScheduleInterval) {
        this.publishScheduleInterval = publishScheduleInterval;
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

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDraft() {
        return draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public Boolean getCreatedFromPackage() {
        return createdFromPackage;
    }

    public void setCreatedFromPackage(Boolean createdFromPackage) {
        this.createdFromPackage = createdFromPackage;
    }

    public String getPackagedCalculatedInsightApiName() {
        return packagedCalculatedInsightApiName;
    }

    public void setPackagedCalculatedInsightApiName(String packagedCalculatedInsightApiName) {
        this.packagedCalculatedInsightApiName = packagedCalculatedInsightApiName;
    }

    public String getHistoryPublishScheduleInterval() {
        return historyPublishScheduleInterval;
    }

    public void setHistoryPublishScheduleInterval(String historyPublishScheduleInterval) {
        this.historyPublishScheduleInterval = historyPublishScheduleInterval;
    }

    public String getHistoryPublishTime() {
        return historyPublishTime;
    }

    public void setHistoryPublishTime(String historyPublishTime) {
        this.historyPublishTime = historyPublishTime;
    }

    public String getSourceDeveloperName() {
        return sourceDeveloperName;
    }

    public void setSourceDeveloperName(String sourceDeveloperName) {
        this.sourceDeveloperName = sourceDeveloperName;
    }
}
