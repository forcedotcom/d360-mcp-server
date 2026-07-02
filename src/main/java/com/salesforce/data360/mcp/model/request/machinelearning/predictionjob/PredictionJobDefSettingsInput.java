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
package com.salesforce.data360.mcp.model.request.machinelearning.predictionjob;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Prediction-job settings
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionJobDefSettingsInput {

    @McpToolParam(description = "Regression / Binary / Multiclass / Clustering. Number of feature contributors to include per prediction. Range 0-3, default 0.", required = false)
    private Integer maxTopContributors;

    @McpToolParam(description = "Regression / Binary only. Maximum prescriptions per prediction. Range 0-3, default 0.", required = false)
    private Integer maxPrescriptions;

    @McpToolParam(description = "Regression / Binary only. Score threshold above which prescriptions are emitted. Range 0-100, default 0.", required = false)
    private Integer prescriptionThreshold;

    @McpToolParam(description = "Binary classification only. When true, output columns include both Positive and Negative class probabilities. Default true.", required = false)
    private Boolean showClassProbabilities;

    @McpToolParam(description = "Multiclass classification only. Number of classes to emit per prediction. Minimum 1.", required = false)
    private Integer numberOfClasses;

    @McpToolParam(description = "Clustering only. Required. Output column label for the cluster id.", required = false)
    private String clusterIdFieldLabel;

    @McpToolParam(description = "Clustering only. Required. Output column label for the cluster name.", required = false)
    private String clusterLabelFieldLabel;

    public Integer getMaxTopContributors() {
        return maxTopContributors;
    }

    public void setMaxTopContributors(Integer maxTopContributors) {
        this.maxTopContributors = maxTopContributors;
    }

    public Integer getMaxPrescriptions() {
        return maxPrescriptions;
    }

    public void setMaxPrescriptions(Integer maxPrescriptions) {
        this.maxPrescriptions = maxPrescriptions;
    }

    public Integer getPrescriptionThreshold() {
        return prescriptionThreshold;
    }

    public void setPrescriptionThreshold(Integer prescriptionThreshold) {
        this.prescriptionThreshold = prescriptionThreshold;
    }

    public Boolean getShowClassProbabilities() {
        return showClassProbabilities;
    }

    public void setShowClassProbabilities(Boolean showClassProbabilities) {
        this.showClassProbabilities = showClassProbabilities;
    }

    public Integer getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(Integer numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public String getClusterIdFieldLabel() {
        return clusterIdFieldLabel;
    }

    public void setClusterIdFieldLabel(String clusterIdFieldLabel) {
        this.clusterIdFieldLabel = clusterIdFieldLabel;
    }

    public String getClusterLabelFieldLabel() {
        return clusterLabelFieldLabel;
    }

    public void setClusterLabelFieldLabel(String clusterLabelFieldLabel) {
        this.clusterLabelFieldLabel = clusterLabelFieldLabel;
    }
}
