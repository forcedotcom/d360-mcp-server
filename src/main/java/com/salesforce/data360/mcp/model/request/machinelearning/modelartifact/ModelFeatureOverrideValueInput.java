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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Single feature-value override for a model output field. Used to rename
 * raw model outputs (e.g. clustering bucket labels).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelFeatureOverrideValueInput {

    @NotBlank
    @McpToolParam(description = "The raw output value emitted by the model.")
    private String sourceValue;

    @NotBlank
    @McpToolParam(description = "Replacement value to surface.")
    private String overrideValue;

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getOverrideValue() {
        return overrideValue;
    }

    public void setOverrideValue(String overrideValue) {
        this.overrideValue = overrideValue;
    }
}
