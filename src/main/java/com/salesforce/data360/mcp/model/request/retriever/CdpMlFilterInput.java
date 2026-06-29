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
 * Mirror of {@code CdpMlFilterInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdpMlFilterInput {

    @McpToolParam(description = "Filter conjunctive operator.", required = false)
    private String conjunctiveOperator;

    @McpToolParam(description = "Criteria", required = false)
    private List<CdpMlFilterCriterionInput> criteria;

    @McpToolParam(
        description = "Custom boolean expression in the format of \"{!$0} AND {!$1} AND ({!$2} OR {!$3}) AND (NOT {!$4})\"",
        required = false)
    private String customBooleanExpression;

    public String getConjunctiveOperator() {
        return conjunctiveOperator;
    }

    public void setConjunctiveOperator(String conjunctiveOperator) {
        this.conjunctiveOperator = conjunctiveOperator;
    }

    public List<CdpMlFilterCriterionInput> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<CdpMlFilterCriterionInput> criteria) {
        this.criteria = criteria;
    }

    public String getCustomBooleanExpression() {
        return customBooleanExpression;
    }

    public void setCustomBooleanExpression(String customBooleanExpression) {
        this.customBooleanExpression = customBooleanExpression;
    }
}
