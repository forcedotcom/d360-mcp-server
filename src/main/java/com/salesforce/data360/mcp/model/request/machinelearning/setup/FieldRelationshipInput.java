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
package com.salesforce.data360.mcp.model.request.machinelearning.setup;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Field relationship metadata. Used in multi-source setups to describe how a
 * related field links to the primary source.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldRelationshipInput {

    @McpToolParam(description = "Source dataset (DMO/CIO/SDM) name owning the related field.", required = false)
    private String source;

    @McpToolParam(description = "Field API name on the source dataset.", required = false)
    private String name;

    @Valid
    @McpToolParam(description = "Join clauses linking the primary dataset to this related field.", required = false)
    private List<JoinCriteriaInput> joins;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JoinCriteriaInput> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinCriteriaInput> joins) {
        this.joins = joins;
    }
}
