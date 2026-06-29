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
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Container-level outcome hint. Distinct from the per-version
 * {@code OutcomeInput}: this lives on the model-setup container and only
 * conveys the goal direction and field-name hints; the version is the
 * authoritative outcome definition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelOutcomeDefinitionInput {

    @McpToolParam(description = "Outcome goal. One of: Minimize, Maximize.", required = false)
    private String goal;

    @McpToolParam(description = "Names of fields the outcome targets.", required = false)
    private List<String> fieldNames;

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }
}
