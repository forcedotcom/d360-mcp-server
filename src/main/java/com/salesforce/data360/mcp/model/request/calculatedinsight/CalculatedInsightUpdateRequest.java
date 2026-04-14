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

    @McpToolParam(description = "Display name for the calculated insight", required = false)
    private String displayName;

    @McpToolParam(description = "Definition type: CALCULATED_METRIC, EXTERNAL_METRIC, or STREAMING_METRIC", required = false)
    private String definitionType;

    @McpToolParam(description = "SQL expression for the calculated insight", required = false)
    private String expression;

    @McpToolParam(description = "Publish schedule interval: ExternallyManaged, NotScheduled, One, Six, Streaming, SystemManaged, Twelve, TwentyFour", required = false)
    private String publishScheduleInterval;

    @McpToolParam(description = "Publish schedule start date/time (ISO 8601)", required = false)
    private String publishScheduleStartDateTime;

    @McpToolParam(description = "Publish schedule end date (ISO 8601)", required = false)
    private String publishScheduleEndDate;

    @McpToolParam(description = "Data space name", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Whether this is a draft", required = false)
    private Boolean draft;

    @McpToolParam(description = "Whether created from a package", required = false)
    private Boolean createdFromPackage;

    @McpToolParam(description = "Packaged calculated insight API name", required = false)
    private String packagedCalculatedInsightApiName;

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
}
