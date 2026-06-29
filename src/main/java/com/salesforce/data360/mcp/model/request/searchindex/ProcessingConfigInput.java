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
 * Configuration for parsing, pre-processing, or post-processing stages.
 * Shared structure used by parsingConfigurations, preProcessingConfigurations,
 * and postProcessingConfigurations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessingConfigInput {

    @McpToolParam(description = "Processing strategy configuration with id and userValues")
    private ConfigInput config;

    @McpToolParam(description = "File extensions this processing applies to (e.g., pdf, html, docx)")
    private List<String> fileExtensions;

    @McpToolParam(description = "Developer name of the source DMO", required = false)
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Developer name of the source DMO field", required = false)
    private String sourceDmoFieldDeveloperName;

    public ConfigInput getConfig() { return config; }
    public void setConfig(ConfigInput config) { this.config = config; }

    public List<String> getFileExtensions() { return fileExtensions; }
    public void setFileExtensions(List<String> fileExtensions) { this.fileExtensions = fileExtensions; }

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getSourceDmoFieldDeveloperName() { return sourceDmoFieldDeveloperName; }
    public void setSourceDmoFieldDeveloperName(String sourceDmoFieldDeveloperName) { this.sourceDmoFieldDeveloperName = sourceDmoFieldDeveloperName; }
}
