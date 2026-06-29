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
 * Mirrors ConnectApi.DmoFilterLimitInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmoFilterLimitInput {

    @McpToolParam(description = "Attribute Name", required = false)
    private String attributeName;

    @McpToolParam(description = "Max Number of Values", required = false)
    private Integer maxNumberOfValues;

    @McpToolParam(description = "Sort order. One of FilterSortOrderAsc, FilterSortOrderDesc.", required = false)
    private String order;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Integer getMaxNumberOfValues() {
        return maxNumberOfValues;
    }

    public void setMaxNumberOfValues(Integer maxNumberOfValues) {
        this.maxNumberOfValues = maxNumberOfValues;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
