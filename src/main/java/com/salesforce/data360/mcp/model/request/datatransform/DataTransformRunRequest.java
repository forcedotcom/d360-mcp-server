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
package com.salesforce.data360.mcp.model.request.datatransform;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for running a data transform.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformRunRequest {

    @McpToolParam(description = "Definition name to run", required = false)
    private String definitionName;

    @McpToolParam(description = "Whether to force a full run", required = false)
    private Boolean shouldForceFullRun;

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public Boolean getShouldForceFullRun() {
        return shouldForceFullRun;
    }

    public void setShouldForceFullRun(Boolean shouldForceFullRun) {
        this.shouldForceFullRun = shouldForceFullRun;
    }
}
