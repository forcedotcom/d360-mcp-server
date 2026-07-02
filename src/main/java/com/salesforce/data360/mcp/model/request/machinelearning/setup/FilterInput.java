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

/** Optional row-level filter applied on the dataset. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterInput {

    @Valid
    @McpToolParam(description = "Filter conditions (each is a field/operator/values triple).", required = false)
    private List<FilterCriterionInput> criteria;

    @McpToolParam(description = "How criteria combine. One of: And, Or, Custom. Required when there are 2+ criteria. With Custom, pair with customBooleanExpression.", required = false)
    private String conjunctiveOperator;

    @McpToolParam(description = "Boolean expression over criterion indices (e.g. \"(1 AND 2) OR 3\"). Honored only when conjunctiveOperator=Custom.", required = false)
    private String customBooleanExpression;

    public List<FilterCriterionInput> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<FilterCriterionInput> criteria) {
        this.criteria = criteria;
    }

    public String getConjunctiveOperator() {
        return conjunctiveOperator;
    }

    public void setConjunctiveOperator(String conjunctiveOperator) {
        this.conjunctiveOperator = conjunctiveOperator;
    }

    public String getCustomBooleanExpression() {
        return customBooleanExpression;
    }

    public void setCustomBooleanExpression(String customBooleanExpression) {
        this.customBooleanExpression = customBooleanExpression;
    }
}
