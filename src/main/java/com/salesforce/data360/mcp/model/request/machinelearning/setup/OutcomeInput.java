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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Polymorphic outcome definition for a setup version.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutcomeInput {

    @NotBlank
    @McpToolParam(description = "Outcome type. One of: Binary, DiscreteVariable, ContinuousVariable, Clustering. Per-type" +
            " required fields: Binary needs successValue+failureValue; DiscreteVariable needs classLabels; ContinuousVariable " +
            "needs min+max; Clustering needs nClusters (omit for HDBSCAN — it's auto-determined).")
    private String type;

    @McpToolParam(description = "Display label for the outcome.", required = false)
    private String label;

    @McpToolParam(description = "Field API name on the data source that holds the outcome (e.g. Attrition__c).", required = false)
    private String name;

    @McpToolParam(description = "Optimization goal. One of: Minimize, Maximize.", required = false)
    private String goal;

    @McpToolParam(description = "Owning data source name (multi-source only — disambiguates which DMO the outcome field belongs to).", required = false)
    private String source;

    @McpToolParam(description = "Binary outcomes only — value of the success class (e.g. \"Yes\").", required = false)
    private String successValue;

    @McpToolParam(description = "Binary outcomes only — value of the failure class (e.g. \"No\").", required = false)
    private String failureValue;

    @McpToolParam(description = "DiscreteVariable outcomes only — list of class labels.", required = false)
    private List<String> classLabels;

    @McpToolParam(description = "ContinuousVariable outcomes only — minimum value of the target variable.", required = false)
    private Double min;

    @McpToolParam(description = "ContinuousVariable outcomes only — maximum value of the target variable.", required = false)
    private Double max;

    @McpToolParam(description = "Clustering outcomes only — desired number of clusters (omit for HDBSCAN).", required = false)
    private Integer nClusters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSuccessValue() {
        return successValue;
    }

    public void setSuccessValue(String successValue) {
        this.successValue = successValue;
    }

    public String getFailureValue() {
        return failureValue;
    }

    public void setFailureValue(String failureValue) {
        this.failureValue = failureValue;
    }

    public List<String> getClassLabels() {
        return classLabels;
    }

    public void setClassLabels(List<String> classLabels) {
        this.classLabels = classLabels;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Integer getNClusters() {
        return nClusters;
    }

    public void setNClusters(Integer nClusters) {
        this.nClusters = nClusters;
    }
}
