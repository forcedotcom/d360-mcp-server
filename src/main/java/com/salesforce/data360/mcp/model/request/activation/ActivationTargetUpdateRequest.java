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
package com.salesforce.data360.mcp.model.request.activation;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for updating a Connect API activation target
 * (PATCH /ssot/activation-targets/{id}). All fields optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationTargetUpdateRequest {

    @McpToolParam(description = "Name of the activation target.", required = false)
    private String name;

    @McpToolParam(description = "Data space developer name for activation target.", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Target type for activation target. One of AmazonS3, AzureBlob, DataCloud, ExternalPlatform, GoogleCloudStorage, SalesforceMarketingCloud, Sftp", required = false)
    private String platformType;

    @McpToolParam(description = "Connector details for Activation Target", required = false)
    private ConnectorInput connector;

    @McpToolParam(description = "Description of the activation target.", required = false)
    private String description;

    @McpToolParam(description = "Indicates whether communication capping is enabled (true) or not (false).", required = false)
    private Boolean isCappingEnabled;

    @McpToolParam(description = "Egress properties for the activation target, which are applicable only for file-based activation targets.", required = false)
    private EgressPropertiesInput egressProperties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public ConnectorInput getConnector() {
        return connector;
    }

    public void setConnector(ConnectorInput connector) {
        this.connector = connector;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsCappingEnabled() {
        return isCappingEnabled;
    }

    public void setIsCappingEnabled(Boolean isCappingEnabled) {
        this.isCappingEnabled = isCappingEnabled;
    }

    public EgressPropertiesInput getEgressProperties() {
        return egressProperties;
    }

    public void setEgressProperties(EgressPropertiesInput egressProperties) {
        this.egressProperties = egressProperties;
    }
}
