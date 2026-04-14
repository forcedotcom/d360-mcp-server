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
 * Request to create an activation target.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationTargetCreateRequest {

    @McpToolParam(description = "Connector configuration details")
    private DataConnectorInput connector;

    @McpToolParam(description = "Name of the data space")
    private String dataSpaceName;

    @McpToolParam(description = "Description for the activation target")
    private String description;

    @McpToolParam(description = "Whether communication capping is enabled")
    private Boolean isCappingEnabled;

    @McpToolParam(description = "Name of the activation target")
    private String name;

    @McpToolParam(description = "Platform type: AmazonS3, AzureBlob, DataCloud, ExternalPlatform, GoogleCloudStorage, SalesforceMarketingCloud, Sftp")
    private String platformType;

    @McpToolParam(description = "Egress properties (only for file-based targets)", required = false)
    private EgressPropertiesInput egressProperties;

    public DataConnectorInput getConnector() {
        return connector;
    }

    public void setConnector(DataConnectorInput connector) {
        this.connector = connector;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public EgressPropertiesInput getEgressProperties() {
        return egressProperties;
    }

    public void setEgressProperties(EgressPropertiesInput egressProperties) {
        this.egressProperties = egressProperties;
    }
}
