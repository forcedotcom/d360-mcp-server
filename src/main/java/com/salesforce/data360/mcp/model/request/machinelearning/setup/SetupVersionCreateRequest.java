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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for creating a setup-version.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SetupVersionCreateRequest {

    @Valid
    @NotNull
    @McpToolParam(description = "Data source. Set type=DataModelObject/CalculatedInsightObject/SdmObject with source+dataSpace, " +
            "or type=MultiSourceObjects with sources[] for joined sources.")
    private InputSourceInput input;

    @Valid
    @NotEmpty
    @McpToolParam(description = "Outcome definitions. Typically a single entry — Binary, DiscreteVariable, ContinuousVariable, or Clustering.")
    private List<OutcomeInput> outcomes;

    @Valid
    @NotEmpty
    @McpToolParam(description = "Field configurations. At least 3 fields are required by the platform.")
    private List<FieldConfigInput> fields;

    @McpToolParam(description = "Optional algorithm. Hint: Catboost (default for Binary/Continuous), XGBoost (also strong default), " +
            "GLM (linear baseline), LightGBM (fast tree-boost). For Clustering use Kmeans (set nClusters) or Hdbscan (auto-determined). " +
            "If omitted, the tool applies defaulting based on outcome type.", required = false)
    private String algorithmType;

    @McpToolParam(description = "Field selection strategy. One of: Automatic, Manual.", required = false)
    private String fieldSelection;

    @Valid
    @McpToolParam(description = "Optional transformations.", required = false)
    private List<TransformationInput> transformations;

    @Valid
    @McpToolParam(description = "Optional row-level filter applied before training.", required = false)
    private FilterInput filter;

    @Valid
    @McpToolParam(description = "Optional join clauses for multi-source inputs.", required = false)
    private List<JoinCriteriaInput> joins;

    @Valid
    @McpToolParam(description = "Optional field relationships for multi-source inputs.", required = false)
    private List<FieldRelationshipInput> fieldRelationships;

    @McpToolParam(description = "Source setup version number (default 0 for first version; use prior version's number when re-training with edits).", required = false)
    private Integer sourceSetupVersionNumber;

    @McpToolParam(description = "Free-form description.", required = false)
    private String description;

    @McpToolParam(description = "Runtime type. One of: Internal, InternalV2. Defaults to InternalV2", required = false)
    private String runtimeType;

    public InputSourceInput getInput() {
        return input;
    }

    public void setInput(InputSourceInput input) {
        this.input = input;
    }

    public List<OutcomeInput> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<OutcomeInput> outcomes) {
        this.outcomes = outcomes;
    }

    public List<FieldConfigInput> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfigInput> fields) {
        this.fields = fields;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    public String getFieldSelection() {
        return fieldSelection;
    }

    public void setFieldSelection(String fieldSelection) {
        this.fieldSelection = fieldSelection;
    }

    public List<TransformationInput> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<TransformationInput> transformations) {
        this.transformations = transformations;
    }

    public FilterInput getFilter() {
        return filter;
    }

    public void setFilter(FilterInput filter) {
        this.filter = filter;
    }

    public List<JoinCriteriaInput> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinCriteriaInput> joins) {
        this.joins = joins;
    }

    public List<FieldRelationshipInput> getFieldRelationships() {
        return fieldRelationships;
    }

    public void setFieldRelationships(List<FieldRelationshipInput> fieldRelationships) {
        this.fieldRelationships = fieldRelationships;
    }

    public Integer getSourceSetupVersionNumber() {
        return sourceSetupVersionNumber;
    }

    public void setSourceSetupVersionNumber(Integer sourceSetupVersionNumber) {
        this.sourceSetupVersionNumber = sourceSetupVersionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntimeType() {
        return runtimeType;
    }

    public void setRuntimeType(String runtimeType) {
        this.runtimeType = runtimeType;
    }
}
