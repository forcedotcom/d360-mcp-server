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

/** One condition inside a filter. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterCriterionInput {

    @Valid
    @McpToolParam(description = "Values compared against the field (interpreted by operator).", required = false)
    private List<FilterValueInput> values;

    @McpToolParam(description = "Comparison operator. One of: In, NotIn, EqualTo, NotEqualTo, Contains, StartsWith, EndsWith, " +
            "IsNull, IsNotNull, LessThan, LessThanOrEqualTo, GreaterThan, GreaterThanOrEqualTo, Like.", required = false)
    private String operator;

    @Valid
    @McpToolParam(description = "Reference to the field this criterion filters on.", required = false)
    private FieldSourceInput filterField;

    public List<FilterValueInput> getValues() {
        return values;
    }

    public void setValues(List<FilterValueInput> values) {
        this.values = values;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public FieldSourceInput getFilterField() {
        return filterField;
    }

    public void setFilterField(FieldSourceInput filterField) {
        this.filterField = filterField;
    }
}
