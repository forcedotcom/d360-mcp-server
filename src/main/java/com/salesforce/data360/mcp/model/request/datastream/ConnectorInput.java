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
package com.salesforce.data360.mcp.model.request.datastream;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.Map;

/**
 * Connector information for data stream.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectorInput {

    @NotBlank
    @McpToolParam(description = "Connector type", required = false)
    private String connectorType;

    @NotNull
    @McpToolParam(description = "Connector details", required = false)
    private Map<String, Object> connectorDetails;

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public Map<String, Object> getConnectorDetails() {
        return connectorDetails;
    }

    public void setConnectorDetails(Map<String, Object> connectorDetails) {
        this.connectorDetails = connectorDetails;
    }
}
