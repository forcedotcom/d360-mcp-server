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
package com.salesforce.data360.mcp.model.request.datakit;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.Map;

/**
 * Component for data kit.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataKitComponentInput {

    @McpToolParam(description = "Component type: ActivationTarget, CalculatedInsight, DataLakeObject, DataStreamBundle, DataTransform, IdentityResolution, MarketSegment, SemanticModel, etc.", required = false)
    private String type;

    @McpToolParam(description = "Component info (base or bundle)", required = false)
    private Map<String, Object> info;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }
}
