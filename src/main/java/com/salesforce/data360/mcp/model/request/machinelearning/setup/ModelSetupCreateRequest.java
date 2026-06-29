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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for model setup container. Creates a
 * model-setup container — the parent record that groups setup versions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelSetupCreateRequest {

    @NotBlank
    @McpToolParam(description = "Display label for the model setup container.")
    private String label;

    @McpToolParam(description = "Free-form description.", required = false)
    private String description;

    @NotBlank
    @McpToolParam(description = "Model type. One of: Predictive, Generative, Transcribe, SpeechSynthesis, Summarization, Unknown.")
    private String modelType;

    @NotBlank
    @McpToolParam(description = "Model capability. For predictive use one of: Regression, BinaryClassification, MulticlassClassification, Clustering.)")
    private String modelCapability;

    @NotBlank
    @McpToolParam(description = "Setup type. One of: EdcNoCode, OutOfTheBox, ModelConnector. Use EdcNoCode for the agent-authored, no-code training flow.")
    private String setupType;

    @McpToolParam(description = "Connector type — only when setupType=ModelConnector. One of: SageMaker, OpenAI, AzureOpenAI, InternalEmbedding, Generic, Databricks, VertexAI, Anthropic, Bedrock, OpenConnector, Salesforce.", required = false)
    private String connectorType;

    @Valid
    @McpToolParam(description = "Optional outcome hints at the container level (per-version OutcomeInput is authoritative).", required = false)
    private ModelOutcomeDefinitionInput outcomeDefinition;

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

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getModelCapability() {
        return modelCapability;
    }

    public void setModelCapability(String modelCapability) {
        this.modelCapability = modelCapability;
    }

    public String getSetupType() {
        return setupType;
    }

    public void setSetupType(String setupType) {
        this.setupType = setupType;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public ModelOutcomeDefinitionInput getOutcomeDefinition() {
        return outcomeDefinition;
    }

    public void setOutcomeDefinition(ModelOutcomeDefinitionInput outcomeDefinition) {
        this.outcomeDefinition = outcomeDefinition;
    }
}
