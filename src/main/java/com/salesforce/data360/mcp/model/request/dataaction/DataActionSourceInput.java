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

import java.util.List;

/** Mirrors ConnectApi.DataActionSourceInputRepresentation. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionSourceInput {

    @McpToolParam(description = "Source object for the action.", required = false)
    private String actionsSourceObject;

    @McpToolParam(description = "Path to the source object.", required = false)
    private String actionsSourceObjectPath;

    @McpToolParam(description = "Source object type.", required = false)
    private String actionsSourceObjectType;

    @McpToolParam(description = "Source CDC subscription modes. Elements should be among Create, Delete, Update", required = false)
    private List<String> sourceCdcSubscriptions;

    @McpToolParam(description = "Source name.", required = false)
    private String sourceName;

    @McpToolParam(description = "Source type.", required = false)
    private String sourceType;

    public String getActionsSourceObject() { return actionsSourceObject; }
    public void setActionsSourceObject(String actionsSourceObject) { this.actionsSourceObject = actionsSourceObject; }
    public String getActionsSourceObjectPath() { return actionsSourceObjectPath; }
    public void setActionsSourceObjectPath(String actionsSourceObjectPath) { this.actionsSourceObjectPath = actionsSourceObjectPath; }
    public String getActionsSourceObjectType() { return actionsSourceObjectType; }
    public void setActionsSourceObjectType(String actionsSourceObjectType) { this.actionsSourceObjectType = actionsSourceObjectType; }
    public List<String> getSourceCdcSubscriptions() { return sourceCdcSubscriptions; }
    public void setSourceCdcSubscriptions(List<String> sourceCdcSubscriptions) { this.sourceCdcSubscriptions = sourceCdcSubscriptions; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}
