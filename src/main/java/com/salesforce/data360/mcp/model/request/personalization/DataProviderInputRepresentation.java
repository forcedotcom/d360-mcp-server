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
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Personalization Experience Config Data Provider Input Representation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataProviderInputRepresentation {

    @McpToolParam(description = "The type of data provider which also describes how the data is provided. Values: PersonalizationPoint")
    private String type;

    @McpToolParam(description = "What type of unique identifier is provided to link to this Data Provider? Values: ApiName")
    private String referenceType;

    @McpToolParam(description = "The unique identifier of the Data Provider. If ReferenceType is ApiName then this would be Data Provider's API Name")
    private String value;

    @McpToolParam(description = "What type of unique identifier is provided to link to the Data Space? Values: ApiName")
    private String dataSpaceReferenceType;

    @McpToolParam(description = "The unique identifier of the Data Space. Optional, to override default. If DataSpaceReferenceType is ApiName then this would be Data Space's API Name")
    private String dataSpaceValue;

    @McpToolParam(description = "If Personalization requests to this Data Provider should be Grouped, what is the type of the Grouping Value. Values: DataGraph")
    private String groupByType;

    @McpToolParam(description = "What type of unique identifier is provided for the Grouping Value? Values: ApiName")
    private String groupByReferenceType;

    @McpToolParam(description = "The unique identifier of the Grouping By Value. Optional, to override default. If GroupByReferenceType is ApiName then this would be Group By's API Name")
    private String groupByValue;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDataSpaceReferenceType() {
        return dataSpaceReferenceType;
    }

    public void setDataSpaceReferenceType(String dataSpaceReferenceType) {
        this.dataSpaceReferenceType = dataSpaceReferenceType;
    }

    public String getDataSpaceValue() {
        return dataSpaceValue;
    }

    public void setDataSpaceValue(String dataSpaceValue) {
        this.dataSpaceValue = dataSpaceValue;
    }

    public String getGroupByType() {
        return groupByType;
    }

    public void setGroupByType(String groupByType) {
        this.groupByType = groupByType;
    }

    public String getGroupByReferenceType() {
        return groupByReferenceType;
    }

    public void setGroupByReferenceType(String groupByReferenceType) {
        this.groupByReferenceType = groupByReferenceType;
    }

    public String getGroupByValue() {
        return groupByValue;
    }

    public void setGroupByValue(String groupByValue) {
        this.groupByValue = groupByValue;
    }
}
