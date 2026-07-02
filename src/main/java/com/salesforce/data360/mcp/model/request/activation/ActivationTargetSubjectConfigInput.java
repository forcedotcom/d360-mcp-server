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

import java.util.List;

/**
 * Mirrors ConnectApi.ActivationTargetSubjectConfigInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationTargetSubjectConfigInput {

    @McpToolParam(description = "Developer Name of Activation Target Subject Config", required = false)
    private String developerName;

    @McpToolParam(description = "Path of Activation Target Subject Config", required = false)
    private List<QueryPathInputConfig> queryPathConfig;

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public List<QueryPathInputConfig> getQueryPathConfig() {
        return queryPathConfig;
    }

    public void setQueryPathConfig(List<QueryPathInputConfig> queryPathConfig) {
        this.queryPathConfig = queryPathConfig;
    }
}
