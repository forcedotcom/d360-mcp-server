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
package com.salesforce.data360.mcp.model.request.machinelearning.modelartifact;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Partial-update body for a model artifact update request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelArtifactPatchRequest {

    @McpToolParam(description = "Display label.", required = false)
    private String label;

    @McpToolParam(description = "Free-form description.", required = false)
    private String description;

    @McpToolParam(description = "Artifact status. Can be updated to Enabled or Disabled.", required = false)
    private String status;

    @Valid
    @McpToolParam(description = "Output-field feature overrides — used to rename raw model outputs (e.g. clustering bucket labels). " +
            "Each entry targets one output field by id.", required = false)
    private List<ModelOutputFieldOverrideInput> outputFields;

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

    public List<ModelOutputFieldOverrideInput> getOutputFields() {
        return outputFields;
    }

    public void setOutputFields(List<ModelOutputFieldOverrideInput> outputFields) {
        this.outputFields = outputFields;
    }
}
