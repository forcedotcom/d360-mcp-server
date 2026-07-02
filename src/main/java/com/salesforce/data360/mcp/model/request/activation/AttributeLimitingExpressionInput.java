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
 * Mirrors ConnectApi.AttributeLimitingExpressionInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeLimitingExpressionInput {

    @McpToolParam(description = "Attribute Name", required = false)
    private String attributeName;

    @McpToolParam(description = "Entity Name", required = false)
    private String entityName;

    @McpToolParam(description = "Sort order. One of FilterSortOrderAsc, FilterSortOrderDesc.", required = false)
    private String order;

    @McpToolParam(description = "Query Path Config", required = false)
    private List<QueryPathInputConfig> queryPathConfig;

    @McpToolParam(description = "Attribute Type", required = false)
    private String type;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<QueryPathInputConfig> getQueryPathConfig() {
        return queryPathConfig;
    }

    public void setQueryPathConfig(List<QueryPathInputConfig> queryPathConfig) {
        this.queryPathConfig = queryPathConfig;
    }
}
