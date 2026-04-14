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
package com.salesforce.data360.mcp.model.request.searchindex;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Describes a join relationship between a source and target DMO field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceTargetRelationshipInput {

    @McpToolParam(description = "Developer name of the source DMO")
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Developer name of the source DMO field")
    private String sourceDmoFieldDeveloperName;

    @McpToolParam(description = "Developer name of the target DMO")
    private String targetDmoDeveloperName;

    @McpToolParam(description = "Developer name of the target DMO field")
    private String targetDmoFieldDeveloperName;

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getSourceDmoFieldDeveloperName() { return sourceDmoFieldDeveloperName; }
    public void setSourceDmoFieldDeveloperName(String sourceDmoFieldDeveloperName) { this.sourceDmoFieldDeveloperName = sourceDmoFieldDeveloperName; }

    public String getTargetDmoDeveloperName() { return targetDmoDeveloperName; }
    public void setTargetDmoDeveloperName(String targetDmoDeveloperName) { this.targetDmoDeveloperName = targetDmoDeveloperName; }

    public String getTargetDmoFieldDeveloperName() { return targetDmoFieldDeveloperName; }
    public void setTargetDmoFieldDeveloperName(String targetDmoFieldDeveloperName) { this.targetDmoFieldDeveloperName = targetDmoFieldDeveloperName; }
}
