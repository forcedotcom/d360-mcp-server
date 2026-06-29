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
package com.salesforce.data360.mcp.model.request.activation;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Mirrors ConnectApi.AttributeFilterInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeFilterInput {

    @McpToolParam(description = "Attribute Id", required = false)
    private String attributeId;

    @McpToolParam(description = "Attribute Name", required = false)
    private String attributeName;

    @McpToolParam(description = "Date unit filter. One of Days, Months, Years.", required = false)
    private String dateUnits;

    @McpToolParam(description = "Operator", required = false)
    private String operator;

    @McpToolParam(description = "Filter operator data type. One of FilterOperatorDataTypeBoolean, FilterOperatorDataTypeDate, FilterOperatorDataTypeDateOnly, FilterOperatorDataTypeExactlyRelativeDate, FilterOperatorDataTypeNumber, FilterOperatorDataTypeRelateToNowDate, FilterOperatorDataTypeText.", required = false)
    private String type;

    @McpToolParam(description = "Filters Config", required = false)
    private List<String> value;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getDateUnits() {
        return dateUnits;
    }

    public void setDateUnits(String dateUnits) {
        this.dateUnits = dateUnits;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
}
