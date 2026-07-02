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
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Shared shape for the four ML setup-helper query endpoints. Each concrete
 * subclass adds only its endpoint-specific fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class SetupQueryBaseRequest {

    @Valid
    @NotNull
    @McpToolParam(description = "Data source to introspect. Set type=DataModelObject/CalculatedInsightObject/SdmObject with source+dataSpace for single-source, or type=MultiSourceObjects with sources[] for joined sources.")
    private InputSourceInput input;

    @Valid
    @McpToolParam(description = "Optional row-level filter applied before processing.", required = false)
    private FilterInput filter;

    @Valid
    @McpToolParam(description = "Optional join applied to multi-source inputs.", required = false)
    private JoinInput join;

    public InputSourceInput getInput() {
        return input;
    }

    public void setInput(InputSourceInput input) {
        this.input = input;
    }

    public FilterInput getFilter() {
        return filter;
    }

    public void setFilter(FilterInput filter) {
        this.filter = filter;
    }

    public JoinInput getJoin() {
        return join;
    }

    public void setJoin(JoinInput join) {
        this.join = join;
    }
}
