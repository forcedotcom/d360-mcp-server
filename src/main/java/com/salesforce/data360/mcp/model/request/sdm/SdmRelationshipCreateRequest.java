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
package com.salesforce.data360.mcp.model.request.sdm;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** Request body for creating a relationship between data objects in a semantic model. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SdmRelationshipCreateRequest {

    @McpToolParam(description = "Display label for the relationship")
    private String label;

    @McpToolParam(description = "API name of the left (source) data object")
    private String leftSemanticDefinitionApiName;

    @McpToolParam(description = "API name of the right (target) data object")
    private String rightSemanticDefinitionApiName;

    @McpToolParam(description = "Cardinality: 'OneToOne', 'OneToMany', or 'ManyToOne'")
    private String cardinality;

    @McpToolParam(description = "Join criteria as JSON object", required = false)
    private String criteria;

    @McpToolParam(description = "Join type, e.g. 'Inner', 'LeftOuter'", required = false)
    private String joinType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLeftSemanticDefinitionApiName() {
        return leftSemanticDefinitionApiName;
    }

    public void setLeftSemanticDefinitionApiName(String leftSemanticDefinitionApiName) {
        this.leftSemanticDefinitionApiName = leftSemanticDefinitionApiName;
    }

    public String getRightSemanticDefinitionApiName() {
        return rightSemanticDefinitionApiName;
    }

    public void setRightSemanticDefinitionApiName(String rightSemanticDefinitionApiName) {
        this.rightSemanticDefinitionApiName = rightSemanticDefinitionApiName;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }
}
