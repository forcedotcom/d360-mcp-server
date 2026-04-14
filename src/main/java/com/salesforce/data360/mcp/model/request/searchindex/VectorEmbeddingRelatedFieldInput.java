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
 * A related DMO field to include in vector embedding.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorEmbeddingRelatedFieldInput {

    @McpToolParam(description = "Developer name of the related DMO")
    private String relatedDmoDeveloperName;

    @McpToolParam(description = "Developer name of the related DMO field")
    private String relatedDmoFieldDeveloperName;

    @McpToolParam(description = "Name of the related DMO", required = false)
    private String relatedDmoName;

    @McpToolParam(description = "Name of the related DMO field", required = false)
    private String relatedDmoFieldName;

    @McpToolParam(description = "Relationships between source and target DMOs", required = false)
    private List<SourceTargetRelationshipInput> relationships;

    @McpToolParam(description = "Relationship cardinality: OneToOne, NToOne, or OneToN", required = false)
    private String relationshipCardinality;

    @McpToolParam(description = "Alias name for this related field", required = false)
    private String aliasName;

    public String getRelatedDmoDeveloperName() { return relatedDmoDeveloperName; }
    public void setRelatedDmoDeveloperName(String relatedDmoDeveloperName) { this.relatedDmoDeveloperName = relatedDmoDeveloperName; }

    public String getRelatedDmoFieldDeveloperName() { return relatedDmoFieldDeveloperName; }
    public void setRelatedDmoFieldDeveloperName(String relatedDmoFieldDeveloperName) { this.relatedDmoFieldDeveloperName = relatedDmoFieldDeveloperName; }

    public String getRelatedDmoName() { return relatedDmoName; }
    public void setRelatedDmoName(String relatedDmoName) { this.relatedDmoName = relatedDmoName; }

    public String getRelatedDmoFieldName() { return relatedDmoFieldName; }
    public void setRelatedDmoFieldName(String relatedDmoFieldName) { this.relatedDmoFieldName = relatedDmoFieldName; }

    public List<SourceTargetRelationshipInput> getRelationships() { return relationships; }
    public void setRelationships(List<SourceTargetRelationshipInput> relationships) { this.relationships = relationships; }

    public String getRelationshipCardinality() { return relationshipCardinality; }
    public void setRelationshipCardinality(String relationshipCardinality) { this.relationshipCardinality = relationshipCardinality; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }
}
