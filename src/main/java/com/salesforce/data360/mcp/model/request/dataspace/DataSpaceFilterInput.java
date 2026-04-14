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
package com.salesforce.data360.mcp.model.request.dataspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Filter configuration for data space members.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSpaceFilterInput {

    @McpToolParam(description = "Array of condition collections")
    private List<DataSpaceFilterConditionCollectionInput> conditions;

    @McpToolParam(description = "Conjunctive operator: AndOperator, NoneOperator, OrOperator")
    private String conjunctiveOperator;

    public List<DataSpaceFilterConditionCollectionInput> getConditions() {
        return conditions;
    }

    public void setConditions(List<DataSpaceFilterConditionCollectionInput> conditions) {
        this.conditions = conditions;
    }

    public String getConjunctiveOperator() {
        return conjunctiveOperator;
    }

    public void setConjunctiveOperator(String conjunctiveOperator) {
        this.conjunctiveOperator = conjunctiveOperator;
    }
}
