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
package com.salesforce.data360.mcp.model.request.identityresolution;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating an Identity Resolution ruleset.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityResolutionCreateRequest {

    @McpToolParam(description = "Label of the ruleset")
    private String label;

    @McpToolParam(description = "Configuration type: Account or Individual")
    private String configurationType;

    @McpToolParam(description = "Description of the ruleset", required = false)
    private String description;

    @McpToolParam(description = "Ruleset ID to base this on", required = false)
    private String rulesetId;

    @McpToolParam(description = "Whether to run automatically", required = false)
    private Boolean doesRunAutomatically;

    @McpToolParam(description = "Array of match rule objects: [{label, criteria: [{entityName, fieldName, matchMethodType, shouldMatchOnBlank}]}]", required = false)
    private List<Map<String, Object>> matchRules;

    @McpToolParam(description = "Array of reconciliation rule objects: [{entityName, ruleType, shouldIgnoreEmptyValue, fields: [{fieldName, ruleType, shouldIgnoreEmptyValue, sources: [{name}]}]}]")
    private List<Map<String, Object>> reconciliationRules;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRulesetId() {
        return rulesetId;
    }

    public void setRulesetId(String rulesetId) {
        this.rulesetId = rulesetId;
    }

    public Boolean getDoesRunAutomatically() {
        return doesRunAutomatically;
    }

    public void setDoesRunAutomatically(Boolean doesRunAutomatically) {
        this.doesRunAutomatically = doesRunAutomatically;
    }

    public List<Map<String, Object>> getMatchRules() {
        return matchRules;
    }

    public void setMatchRules(List<Map<String, Object>> matchRules) {
        this.matchRules = matchRules;
    }

    public List<Map<String, Object>> getReconciliationRules() {
        return reconciliationRules;
    }

    public void setReconciliationRules(List<Map<String, Object>> reconciliationRules) {
        this.reconciliationRules = reconciliationRules;
    }
}
