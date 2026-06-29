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
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Override for a single model parameter.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelParameterOverrideInput {

    @NotBlank
    @McpToolParam(description = "Parameter name as declared on the referenced model artifact's parameter definitions.")
    private String parameterName;

    @McpToolParam(description = "Override value for a discrete (string) parameter. Provide either discreteValue or continuousValue, not both.", required = false)
    private String discreteValue;

    @McpToolParam(description = "Override value for a continuous (numeric) parameter. Provide either discreteValue or continuousValue, not both.", required = false)
    private Double continuousValue;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getDiscreteValue() {
        return discreteValue;
    }

    public void setDiscreteValue(String discreteValue) {
        this.discreteValue = discreteValue;
    }

    public Double getContinuousValue() {
        return continuousValue;
    }

    public void setContinuousValue(Double continuousValue) {
        this.continuousValue = continuousValue;
    }
}
