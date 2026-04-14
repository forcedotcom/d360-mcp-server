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
 * Configuration for data action targets.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataActionTargetConfig {

    @McpToolParam(description = "Target endpoint URL", required = false)
    private String targetEndpoint;

    @McpToolParam(description = "API contract", required = false)
    private String apiContract;

    @McpToolParam(description = "Org ID (for CRM targets)", required = false)
    private String orgId;

    @McpToolParam(description = "Org label (for CRM targets)", required = false)
    private String orgLabel;

    @McpToolParam(description = "Content key (for MC targets)", required = false)
    private String contentKey;

    @McpToolParam(description = "Content template (for MC targets)", required = false)
    private String contentTemplate;

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    public String getApiContract() {
        return apiContract;
    }

    public void setApiContract(String apiContract) {
        this.apiContract = apiContract;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgLabel() {
        return orgLabel;
    }

    public void setOrgLabel(String orgLabel) {
        this.orgLabel = orgLabel;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }
}
