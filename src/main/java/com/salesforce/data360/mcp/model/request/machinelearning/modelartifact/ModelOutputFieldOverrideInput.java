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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Output-field-scoped feature-value overrides.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelOutputFieldOverrideInput {

    @NotBlank
    @McpToolParam(description = "Output field id from the artifact's outputFields[].")
    private String id;

    @Valid
    @McpToolParam(description = "Feature override values for this output field.", required = false)
    private List<ModelFeatureOverrideValueInput> featureOverrideValues;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ModelFeatureOverrideValueInput> getFeatureOverrideValues() {
        return featureOverrideValues;
    }

    public void setFeatureOverrideValues(List<ModelFeatureOverrideValueInput> featureOverrideValues) {
        this.featureOverrideValues = featureOverrideValues;
    }
}
