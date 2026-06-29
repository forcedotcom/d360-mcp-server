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
 * Mirrors {@code FieldLevelConfigurationInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldLevelConfigInput {

    @McpToolParam(description = "Chunking strategy configuration with id and userValues", required = false)
    private ConfigInput config;

    @McpToolParam(description = "Source DMO developer name (e.g., ssot__Case__dlm)", required = false)
    private String sourceDmoDeveloperName;

    @McpToolParam(description = "Source DMO field developer name (e.g., ssot__Subject__c)", required = false)
    private String sourceDmoFieldDeveloperName;

    @McpToolParam(description = "Source DMO field name", required = false)
    private String sourceDmoFieldName;

    @McpToolParam(description = "Source DMO name", required = false)
    private String sourceDmoName;

    @McpToolParam(description = "Chunk Config version", required = false)
    private String version;

    public ConfigInput getConfig() { return config; }
    public void setConfig(ConfigInput config) { this.config = config; }

    public String getSourceDmoDeveloperName() { return sourceDmoDeveloperName; }
    public void setSourceDmoDeveloperName(String sourceDmoDeveloperName) { this.sourceDmoDeveloperName = sourceDmoDeveloperName; }

    public String getSourceDmoFieldDeveloperName() { return sourceDmoFieldDeveloperName; }
    public void setSourceDmoFieldDeveloperName(String sourceDmoFieldDeveloperName) { this.sourceDmoFieldDeveloperName = sourceDmoFieldDeveloperName; }

    public String getSourceDmoFieldName() { return sourceDmoFieldName; }
    public void setSourceDmoFieldName(String sourceDmoFieldName) { this.sourceDmoFieldName = sourceDmoFieldName; }

    public String getSourceDmoName() { return sourceDmoName; }
    public void setSourceDmoName(String sourceDmoName) { this.sourceDmoName = sourceDmoName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
