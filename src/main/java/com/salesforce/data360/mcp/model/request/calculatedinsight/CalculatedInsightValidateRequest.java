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
package com.salesforce.data360.mcp.model.request.calculatedinsight;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** CdpCalculatedInsightValidateInputRepresentation */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculatedInsightValidateRequest {

    @NotBlank
    @McpToolParam(description = "Calculated insight ansi sql statement/expression")
    private String expression;

    @McpToolParam(description = "Calculated insight definition type. One of CALCULATED_METRIC, EXTERNAL_METRIC, STREAMING_METRIC, GRAPH_METRIC, HISTORY_METRIC", required = false)
    private String definitionType;

    @McpToolParam(description = "Dataspace name.", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Flag to identify the ci creation thru package. If true, the ci is created from a installed package", required = false)
    private Boolean createdFromPackage;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public Boolean getCreatedFromPackage() {
        return createdFromPackage;
    }

    public void setCreatedFromPackage(Boolean createdFromPackage) {
        this.createdFromPackage = createdFromPackage;
    }
}
