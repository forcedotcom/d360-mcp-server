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
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Partial-update body for {@code PATCH /ssot/machine-learning/model-setups/{idOrName}}.
 * Only set the fields you want to change.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelSetupPatchRequest {

    @McpToolParam(description = "New display label.", required = false)
    private String label;

    @McpToolParam(description = "New description.", required = false)
    private String description;

    @McpToolParam(description = "Updated model capability. See create-request docs for valid values.", required = false)
    private String modelCapability;

    @Valid
    @McpToolParam(description = "Updated outcome hint.", required = false)
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

    public String getModelCapability() {
        return modelCapability;
    }

    public void setModelCapability(String modelCapability) {
        this.modelCapability = modelCapability;
    }

    public ModelOutcomeDefinitionInput getOutcomeDefinition() {
        return outcomeDefinition;
    }

    public void setOutcomeDefinition(ModelOutcomeDefinitionInput outcomeDefinition) {
        this.outcomeDefinition = outcomeDefinition;
    }
}
