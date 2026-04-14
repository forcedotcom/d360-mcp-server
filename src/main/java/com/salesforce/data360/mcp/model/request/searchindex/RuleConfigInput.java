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
 * Rule configuration with priority rules and sorting rules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleConfigInput {

    @McpToolParam(description = "Label for the rule configuration", required = false)
    private String label;

    @McpToolParam(description = "Priority rules with conditions", required = false)
    private List<PriorityRuleInput> rules;

    @McpToolParam(description = "Sorting rules", required = false)
    private List<SortingRuleInput> sortingRules;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<PriorityRuleInput> getRules() { return rules; }
    public void setRules(List<PriorityRuleInput> rules) { this.rules = rules; }

    public List<SortingRuleInput> getSortingRules() { return sortingRules; }
    public void setSortingRules(List<SortingRuleInput> sortingRules) { this.sortingRules = sortingRules; }
}
