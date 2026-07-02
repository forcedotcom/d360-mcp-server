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
package com.salesforce.data360.mcp.model.request.retriever;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Mirror of {@code MlRetrieverOutputFieldInputRepresentation} (extends
 * {@code MlRetrieverFieldBaseInputRepresentation}).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MlRetrieverOutputFieldInput {

    @McpToolParam(description = "The related DMO field developer name", required = false)
    private String relatedDmoFieldName;

    @McpToolParam(description = "The related DMO developer name", required = false)
    private String relatedDmoName;

    @McpToolParam(description = "The output field data source", required = false)
    private String dataSourceType;

    @McpToolParam(description = "The output field label", required = false)
    private String label;

    @McpToolParam(description = "The relationship path from this related DMO to the source DMO of the semantic search definition", required = false)
    private List<MlDmoFieldSourceTargetRelationshipInput> relationships;

    public String getRelatedDmoFieldName() {
        return relatedDmoFieldName;
    }

    public void setRelatedDmoFieldName(String relatedDmoFieldName) {
        this.relatedDmoFieldName = relatedDmoFieldName;
    }

    public String getRelatedDmoName() {
        return relatedDmoName;
    }

    public void setRelatedDmoName(String relatedDmoName) {
        this.relatedDmoName = relatedDmoName;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<MlDmoFieldSourceTargetRelationshipInput> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<MlDmoFieldSourceTargetRelationshipInput> relationships) {
        this.relationships = relationships;
    }
}
