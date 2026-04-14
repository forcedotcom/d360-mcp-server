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

import java.util.List;

/**
 * Field-level chunking configuration for a specific DMO field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldLevelConfigInput {

    @McpToolParam(description = "Developer name of the source DMO (e.g., ssot__Case__dlm)")
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Developer name of the source DMO field (e.g., ssot__Subject__c)")
    private String sourceDmoFieldDeveloperName;

    @McpToolParam(description = "List of decorator names", required = false)
    private List<String> decorators;

    @McpToolParam(description = "Chunking strategy configuration with id and userValues")
    private ConfigInput config;

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getSourceDmoFieldDeveloperName() { return sourceDmoFieldDeveloperName; }
    public void setSourceDmoFieldDeveloperName(String sourceDmoFieldDeveloperName) { this.sourceDmoFieldDeveloperName = sourceDmoFieldDeveloperName; }

    public List<String> getDecorators() { return decorators; }
    public void setDecorators(List<String> decorators) { this.decorators = decorators; }

    public ConfigInput getConfig() { return config; }
    public void setConfig(ConfigInput config) { this.config = config; }
}
