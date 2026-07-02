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
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for patching a prediction job definition. Same shape as
 * {@link PredictionJobDefCreateRequest} but every field is optional —
 * partial-update semantics: send only the fields you want to change.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionJobDefPatchRequest {

    @McpToolParam(description = "Job type. Usually omitted on patch (type is set at create).", required = false)
    private String type;

    @McpToolParam(description = "api name.", required = false)
    private String apiName;

    @McpToolParam(description = "Display label.", required = false)
    private String label;

    @McpToolParam(description = "Free-form description.", required = false)
    private String description;

    @Valid
    @McpToolParam(description = "Configured-model reference.", required = false)
    private AssetReferenceInput model;

    @Valid
    @McpToolParam(description = "Output DMO configuration.", required = false)
    private PredictionsConfigInput outputConfig;

    @McpToolParam(description = "Scoring mode: Batch, Streaming.", required = false)
    private String scoringMode;

    @McpToolParam(description = "Activation status: Active or Inactive. Prefer d360_prediction_job_def_activate / _deactivate for this.", required = false)
    private String activationStatus;

    @Valid
    @McpToolParam(description = "Input configuration.", required = false)
    private PredictionJobDefInputConfig inputConfig;

    @Valid
    @McpToolParam(description = "Regression-only settings.", required = false)
    private PredictionJobDefSettingsInput regressionSettings;

    @Valid
    @McpToolParam(description = "Binary classification settings.", required = false)
    private PredictionJobDefSettingsInput binaryClassificationSettings;

    @Valid
    @McpToolParam(description = "Multiclass classification settings.", required = false)
    private PredictionJobDefSettingsInput multiclassClassificationSettings;

    @Valid
    @McpToolParam(description = "Clustering settings.", required = false)
    private PredictionJobDefSettingsInput clusteringSettings;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AssetReferenceInput getModel() {
        return model;
    }

    public void setModel(AssetReferenceInput model) {
        this.model = model;
    }

    public PredictionsConfigInput getOutputConfig() {
        return outputConfig;
    }

    public void setOutputConfig(PredictionsConfigInput outputConfig) {
        this.outputConfig = outputConfig;
    }

    public String getScoringMode() {
        return scoringMode;
    }

    public void setScoringMode(String scoringMode) {
        this.scoringMode = scoringMode;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }

    public PredictionJobDefInputConfig getInputConfig() {
        return inputConfig;
    }

    public void setInputConfig(PredictionJobDefInputConfig inputConfig) {
        this.inputConfig = inputConfig;
    }

    public PredictionJobDefSettingsInput getRegressionSettings() {
        return regressionSettings;
    }

    public void setRegressionSettings(PredictionJobDefSettingsInput regressionSettings) {
        this.regressionSettings = regressionSettings;
    }

    public PredictionJobDefSettingsInput getBinaryClassificationSettings() {
        return binaryClassificationSettings;
    }

    public void setBinaryClassificationSettings(PredictionJobDefSettingsInput binaryClassificationSettings) {
        this.binaryClassificationSettings = binaryClassificationSettings;
    }

    public PredictionJobDefSettingsInput getMulticlassClassificationSettings() {
        return multiclassClassificationSettings;
    }

    public void setMulticlassClassificationSettings(PredictionJobDefSettingsInput multiclassClassificationSettings) {
        this.multiclassClassificationSettings = multiclassClassificationSettings;
    }

    public PredictionJobDefSettingsInput getClusteringSettings() {
        return clusteringSettings;
    }

    public void setClusteringSettings(PredictionJobDefSettingsInput clusteringSettings) {
        this.clusteringSettings = clusteringSettings;
    }
}
