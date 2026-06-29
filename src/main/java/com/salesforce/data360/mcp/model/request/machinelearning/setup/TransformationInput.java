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
 * Polymorphic transformation. Per-type extras are exposed as explicit optional fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformationInput {

    @NotBlank
    @McpToolParam(description = "Transformation type. One of: ExtractDayOfWeek, ExtractMonthOfYear, FreeTextClustering, " +
            "NumericalImputation, SentimentAnalysis, TopicClassification.")
    private String type;

    @NotBlank
    @McpToolParam(description = "Field API name to read from.")
    private String sourceFieldName;

    @NotBlank
    @McpToolParam(description = "Field API name to write to (often equals sourceFieldName for in-place transformations).")
    private String targetFieldName;

    @McpToolParam(description = "Multi-source inputs only — target dataset name.", required = false)
    private String source;

    @McpToolParam(description = "Multi-source inputs only — source dataset name.", required = false)
    private String sourceFieldSource;

    @McpToolParam(description = "NumericalImputation only — imputation method. One of: Mean, Median, Mode. Defaults to Mean.", required = false)
    private String imputeMethod;

    @McpToolParam(description = "TopicClassification only — list of candidate topics for the classifier.", required = false)
    private List<String> candidates;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceFieldSource() {
        return sourceFieldSource;
    }

    public void setSourceFieldSource(String sourceFieldSource) {
        this.sourceFieldSource = sourceFieldSource;
    }

    public String getImputeMethod() {
        return imputeMethod;
    }

    public void setImputeMethod(String imputeMethod) {
        this.imputeMethod = imputeMethod;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }
}
