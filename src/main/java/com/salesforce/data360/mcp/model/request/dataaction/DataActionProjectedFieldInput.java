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

/** Mirrors ConnectApi.DataActionProjectedFieldInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionProjectedFieldInput {

    @McpToolParam(description = "Field alias name.", required = false)
    private String fieldAliasName;

    @McpToolParam(description = "Field API name.", required = false)
    private String fieldApiName;

    @McpToolParam(description = "Object API name.", required = false)
    private String objectApiName;

    @McpToolParam(description = "Object data graph name.", required = false)
    private String objectDataGraphName;

    public String getFieldAliasName() { return fieldAliasName; }
    public void setFieldAliasName(String fieldAliasName) { this.fieldAliasName = fieldAliasName; }
    public String getFieldApiName() { return fieldApiName; }
    public void setFieldApiName(String fieldApiName) { this.fieldApiName = fieldApiName; }
    public String getObjectApiName() { return objectApiName; }
    public void setObjectApiName(String objectApiName) { this.objectApiName = objectApiName; }
    public String getObjectDataGraphName() { return objectDataGraphName; }
    public void setObjectDataGraphName(String objectDataGraphName) { this.objectDataGraphName = objectDataGraphName; }
}
