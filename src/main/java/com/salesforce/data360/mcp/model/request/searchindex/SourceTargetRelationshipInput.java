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
 * Mirrors {@code SourceTargetRelationshipInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceTargetRelationshipInput {

    @McpToolParam(description = "Source DMO developer name", required = false)
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Source DMO field developer name", required = false)
    private String sourceDmoFieldDeveloperName;

    @McpToolParam(description = "Source DMO label", required = false)
    private String sourceDmoLabel;

    @McpToolParam(description = "Source DMO field label", required = false)
    private String sourceFieldLabel;

    @McpToolParam(description = "Target DMO developer name", required = false)
    private String targetDmoDeveloperName;

    @McpToolParam(description = "Target DMO field developer name", required = false)
    private String targetDmoFieldDeveloperName;

    @McpToolParam(description = "Target DMO label", required = false)
    private String targetDmoLabel;

    @McpToolParam(description = "Target DMO field label", required = false)
    private String targetFieldLabel;

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getSourceDmoFieldDeveloperName() { return sourceDmoFieldDeveloperName; }
    public void setSourceDmoFieldDeveloperName(String sourceDmoFieldDeveloperName) { this.sourceDmoFieldDeveloperName = sourceDmoFieldDeveloperName; }

    public String getSourceDmoLabel() { return sourceDmoLabel; }
    public void setSourceDmoLabel(String sourceDmoLabel) { this.sourceDmoLabel = sourceDmoLabel; }

    public String getSourceFieldLabel() { return sourceFieldLabel; }
    public void setSourceFieldLabel(String sourceFieldLabel) { this.sourceFieldLabel = sourceFieldLabel; }

    public String getTargetDmoDeveloperName() { return targetDmoDeveloperName; }
    public void setTargetDmoDeveloperName(String targetDmoDeveloperName) { this.targetDmoDeveloperName = targetDmoDeveloperName; }

    public String getTargetDmoFieldDeveloperName() { return targetDmoFieldDeveloperName; }
    public void setTargetDmoFieldDeveloperName(String targetDmoFieldDeveloperName) { this.targetDmoFieldDeveloperName = targetDmoFieldDeveloperName; }

    public String getTargetDmoLabel() { return targetDmoLabel; }
    public void setTargetDmoLabel(String targetDmoLabel) { this.targetDmoLabel = targetDmoLabel; }

    public String getTargetFieldLabel() { return targetFieldLabel; }
    public void setTargetFieldLabel(String targetFieldLabel) { this.targetFieldLabel = targetFieldLabel; }
}
