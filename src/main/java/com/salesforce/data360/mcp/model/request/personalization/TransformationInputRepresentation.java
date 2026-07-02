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
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.Map;

/**
 * Personalization Experience Config Transformation Input Representation.
 * Defines how to transform data from the Data Provider.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformationInputRepresentation {

    @McpToolParam(description = "The name of the transformer to use for the transformation")
    private String transformerName;

    @McpToolParam(description = "Map of substitution variable names to their values for this transformation. Keys are substitution variable names defined in the transformer, values are the actual values to use")
    private Map<String, Object> substitutions;

    public String getTransformerName() {
        return transformerName;
    }

    public void setTransformerName(String transformerName) {
        this.transformerName = transformerName;
    }

    public Map<String, Object> getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(Map<String, Object> substitutions) {
        this.substitutions = substitutions;
    }
}
