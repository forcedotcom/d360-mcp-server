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
package com.salesforce.data360.mcp.model.request.dataaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for updating a data action target.
 * All fields are optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionTargetUpdateRequest {

    @McpToolParam(description = "API name of the data action target", required = false)
    private String apiName;

    @McpToolParam(description = "Label", required = false)
    private String label;

    @McpToolParam(description = "Data Action Target Type. One of Core, Internal_WebHook, MarketingCloud, WebHook", required = false)
    private String type;

    @McpToolParam(description = "Data Action Target Sub type. One of Grpc, Rest", required = false)
    private String subType;

    @McpToolParam(description = "External record identifier", required = false)
    private String externalRecordIdentifier;

    @McpToolParam(description = "Target configuration", required = false)
    private DataActionTargetConfig config;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getExternalRecordIdentifier() {
        return externalRecordIdentifier;
    }

    public void setExternalRecordIdentifier(String externalRecordIdentifier) {
        this.externalRecordIdentifier = externalRecordIdentifier;
    }

    public DataActionTargetConfig getConfig() {
        return config;
    }

    public void setConfig(DataActionTargetConfig config) {
        this.config = config;
    }
}
