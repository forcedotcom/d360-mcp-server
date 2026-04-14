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
 * Record field configuration for keyword search indexing.
 * Fields configured here are searchable and retrievable.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordFieldConfigurationInput {

    @McpToolParam(description = "Developer name of the DMO containing the field")
    private String dmoDeveloperName;

    @McpToolParam(description = "Developer name of the DMO field to index")
    private String dmoFieldDeveloperName;

    @McpToolParam(description = "Alias name for the indexed field")
    private String aliasName;

    @McpToolParam(description = "Relationships between source and target DMOs", required = false)
    private List<SourceTargetRelationshipInput> relationships;

    @McpToolParam(description = "Tokenization strategies (e.g., STANDARD, TYPEAHEAD)", required = false)
    private List<TokenizationInput> tokenizations;

    public String getDmoDeveloperName() { return dmoDeveloperName; }
    public void setDmoDeveloperName(String dmoDeveloperName) { this.dmoDeveloperName = dmoDeveloperName; }

    public String getDmoFieldDeveloperName() { return dmoFieldDeveloperName; }
    public void setDmoFieldDeveloperName(String dmoFieldDeveloperName) { this.dmoFieldDeveloperName = dmoFieldDeveloperName; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }

    public List<SourceTargetRelationshipInput> getRelationships() { return relationships; }
    public void setRelationships(List<SourceTargetRelationshipInput> relationships) { this.relationships = relationships; }

    public List<TokenizationInput> getTokenizations() { return tokenizations; }
    public void setTokenizations(List<TokenizationInput> tokenizations) { this.tokenizations = tokenizations; }
}
