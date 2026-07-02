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
package com.salesforce.data360.mcp.model.request.dataaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** Mirrors ConnectApi.DataActionConditionInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionConditionInput {

    @McpToolParam(description = "Condition source type.", required = false)
    private String conditionSourceType;

    @McpToolParam(description = "Condition type to differentiate between regular and additional conditions.", required = false)
    private String conditionType;

    @McpToolParam(description = "Name of the data graph.", required = false)
    private String dataGraphName;

    @McpToolParam(description = "Object field name used in the action condition.", required = false)
    private String fieldName;

    @McpToolParam(description = "Unique object name in the data graph.", required = false)
    private String objectDataGraphName;

    @McpToolParam(description = "Object name used in the action condition.", required = false)
    private String objectName;

    @McpToolParam(description = "Operator used in the rule.", required = false)
    private String operator;

    @McpToolParam(description = "Sequence number that the user entered the rule from UI.", required = false)
    private String order;

    @McpToolParam(description = "Value specified by the user in the rule.", required = false)
    private String value;

    public String getConditionSourceType() { return conditionSourceType; }
    public void setConditionSourceType(String conditionSourceType) { this.conditionSourceType = conditionSourceType; }
    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    public String getDataGraphName() { return dataGraphName; }
    public void setDataGraphName(String dataGraphName) { this.dataGraphName = dataGraphName; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getObjectDataGraphName() { return objectDataGraphName; }
    public void setObjectDataGraphName(String objectDataGraphName) { this.objectDataGraphName = objectDataGraphName; }
    public String getObjectName() { return objectName; }
    public void setObjectName(String objectName) { this.objectName = objectName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
