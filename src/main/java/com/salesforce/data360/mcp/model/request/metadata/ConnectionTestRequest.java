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
package com.salesforce.data360.mcp.model.request.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Request body for {@code POST /ssot/connections/actions/test}.
 *
 * <p>Mirrors {@code ConnectionTestInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionTestRequest {

    @NotBlank
    @McpToolParam(description = "ype of the connector. For example, AwsRdsPostgres, AzureBlob, Databricks, Gcs, IngestApi, SalesforceDotCom, SalesforceMarketingCloud, Sftp, StreamingApp, and so forth.")
    private String connectorType;

    @McpToolParam(description = "Connection credentials", required = false)
    private List<DataConnectionParameterInput> credentials;

    @McpToolParam(description = "Connection method. One of Egress, Ingress.", required = false)
    private String method;

    @McpToolParam(description = "Connection parameters", required = false)
    private List<DataConnectionParameterInput> parameters;

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public List<DataConnectionParameterInput> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<DataConnectionParameterInput> credentials) {
        this.credentials = credentials;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<DataConnectionParameterInput> getParameters() {
        return parameters;
    }

    public void setParameters(List<DataConnectionParameterInput> parameters) {
        this.parameters = parameters;
    }
}
