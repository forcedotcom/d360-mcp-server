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
package com.salesforce.data360.mcp.model.request.machinelearning.configuredmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Partial-update body for a configured model. All fields are optional;
 * only the populated ones are applied.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfiguredModelPatchRequest {

    @Valid
    @McpToolParam(description = "Reference to the underlying ML model artifact. Setting this would update the current active model.", required = false)
    private AssetReferenceInput artifact;

    @McpToolParam(description = "Display label for the configured model.", required = false)
    private String label;

    @McpToolParam(description = "Free-form description.", required = false)
    private String description;

    @McpToolParam(description = "Configured model status. One of: Disabled, Enabled, Deprecated, Rerouted. " +
            "Setting Enabled activates the model; setting Disabled deactivates. ", required = false)
    private String status;

    @McpToolParam(description = "Visibility. One of: Shown, Hidden.", required = false)
    private String visibility;

    @McpToolParam(description = "Model capability. See create tool for full enum list.", required = false)
    private String capability;

    @Valid
    @McpToolParam(description = "Per-parameter overrides.", required = false)
    private List<ModelParameterOverrideInput> parameterOverrides;

    @Valid
    @McpToolParam(description = "Actionable variables. Prescriptions can be generated on these variables.", required = false)
    private List<CustomizableFieldInput> actionableFields;

    public AssetReferenceInput getArtifact() {
        return artifact;
    }

    public void setArtifact(AssetReferenceInput artifact) {
        this.artifact = artifact;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getCapability() {
        return capability;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public List<ModelParameterOverrideInput> getParameterOverrides() {
        return parameterOverrides;
    }

    public void setParameterOverrides(List<ModelParameterOverrideInput> parameterOverrides) {
        this.parameterOverrides = parameterOverrides;
    }

    public List<CustomizableFieldInput> getActionableFields() {
        return actionableFields;
    }

    public void setActionableFields(List<CustomizableFieldInput> actionableFields) {
        this.actionableFields = actionableFields;
    }
}
