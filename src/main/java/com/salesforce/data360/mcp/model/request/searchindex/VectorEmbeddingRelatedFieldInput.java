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
 * Mirrors {@code VectorEmbeddingRelatedFieldsInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VectorEmbeddingRelatedFieldInput {

    @McpToolParam(description = "Alias of the field", required = false)
    private String aliasName;

    @McpToolParam(description = "Filter expression", required = false)
    private String filterExpression;

    @McpToolParam(description = "Related DMO developer name", required = false)
    private String relatedDmoDeveloperName;

    @McpToolParam(description = "Related DMO field developer name", required = false)
    private String relatedDmoFieldDeveloperName;

    @McpToolParam(description = "Related DMO field name", required = false)
    private String relatedDmoFieldName;

    @McpToolParam(description = "Related DMO name", required = false)
    private String relatedDmoName;

    @McpToolParam(description = "Relationship cardinality (OneToOne, ManyToOne, OneToMany)", required = false)
    private String relationshipCardinality;

    @McpToolParam(description = "Field relationship paths", required = false)
    private List<SourceTargetRelationshipInput> relationships;

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }

    public String getFilterExpression() { return filterExpression; }
    public void setFilterExpression(String filterExpression) { this.filterExpression = filterExpression; }

    public String getRelatedDmoDeveloperName() { return relatedDmoDeveloperName; }
    public void setRelatedDmoDeveloperName(String relatedDmoDeveloperName) { this.relatedDmoDeveloperName = relatedDmoDeveloperName; }

    public String getRelatedDmoFieldDeveloperName() { return relatedDmoFieldDeveloperName; }
    public void setRelatedDmoFieldDeveloperName(String relatedDmoFieldDeveloperName) { this.relatedDmoFieldDeveloperName = relatedDmoFieldDeveloperName; }

    public String getRelatedDmoFieldName() { return relatedDmoFieldName; }
    public void setRelatedDmoFieldName(String relatedDmoFieldName) { this.relatedDmoFieldName = relatedDmoFieldName; }

    public String getRelatedDmoName() { return relatedDmoName; }
    public void setRelatedDmoName(String relatedDmoName) { this.relatedDmoName = relatedDmoName; }

    public String getRelationshipCardinality() { return relationshipCardinality; }
    public void setRelationshipCardinality(String relationshipCardinality) { this.relationshipCardinality = relationshipCardinality; }

    public List<SourceTargetRelationshipInput> getRelationships() { return relationships; }
    public void setRelationships(List<SourceTargetRelationshipInput> relationships) { this.relationships = relationships; }
}
