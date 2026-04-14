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
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.Map;

/**
 * Connector patch information for data stream update.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectorPatchInput {

    @McpToolParam(description = "Connector type", required = false)
    private String connectorType;

    @McpToolParam(description = "Connector patch details", required = false)
    private Map<String, Object> connectorPatchDetailsConfig;

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public Map<String, Object> getConnectorPatchDetailsConfig() {
        return connectorPatchDetailsConfig;
    }

    public void setConnectorPatchDetailsConfig(Map<String, Object> connectorPatchDetailsConfig) {
        this.connectorPatchDetailsConfig = connectorPatchDetailsConfig;
    }
}
