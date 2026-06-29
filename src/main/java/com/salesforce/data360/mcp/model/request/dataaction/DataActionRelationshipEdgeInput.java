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

/** Mirrors ConnectApi.DataActionRelationshipEdgeInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionRelationshipEdgeInput {

    @McpToolParam(description = "Source field API name.", required = false)
    private String sourceFieldApiName;

    @McpToolParam(description = "Source object API name.", required = false)
    private String sourceObjectApiName;

    @McpToolParam(description = "Target field API name.", required = false)
    private String targetFieldApiName;

    @McpToolParam(description = "Target object API name.", required = false)
    private String targetObjectApiName;

    @McpToolParam(description = "Target object data graph name.", required = false)
    private String targetObjectDataGraphName;

    public String getSourceFieldApiName() { return sourceFieldApiName; }
    public void setSourceFieldApiName(String sourceFieldApiName) { this.sourceFieldApiName = sourceFieldApiName; }
    public String getSourceObjectApiName() { return sourceObjectApiName; }
    public void setSourceObjectApiName(String sourceObjectApiName) { this.sourceObjectApiName = sourceObjectApiName; }
    public String getTargetFieldApiName() { return targetFieldApiName; }
    public void setTargetFieldApiName(String targetFieldApiName) { this.targetFieldApiName = targetFieldApiName; }
    public String getTargetObjectApiName() { return targetObjectApiName; }
    public void setTargetObjectApiName(String targetObjectApiName) { this.targetObjectApiName = targetObjectApiName; }
    public String getTargetObjectDataGraphName() { return targetObjectDataGraphName; }
    public void setTargetObjectDataGraphName(String targetObjectDataGraphName) { this.targetObjectDataGraphName = targetObjectDataGraphName; }
}
