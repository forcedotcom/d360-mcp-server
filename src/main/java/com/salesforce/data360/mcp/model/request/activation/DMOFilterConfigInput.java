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
 * Mirrors ConnectApi.DMOFilterInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DMOFilterConfigInput {

    @McpToolParam(description = "Entity Filter", required = false)
    private ComparisonInput entityFilter;

    @McpToolParam(description = "Entity Filter type", required = false)
    private String entityFilterType;

    @McpToolParam(description = "Entity Name", required = false)
    private String entityName;

    @McpToolParam(description = "Limit", required = false)
    private DmoFilterLimitInput filterLimit;

    @McpToolParam(description = "Inherited Filter", required = false)
    private ComparisonInput inheritedFilter;

    @McpToolParam(description = "inherited filter type", required = false)
    private String inheritedFilterType;

    @McpToolParam(description = "Path From Activate On To Container", required = false)
    private List<QueryPathInputConfig> queryPathConfigForActivateOnToContainer;

    @McpToolParam(description = "Path From Container To Entity", required = false)
    private List<QueryPathInputConfig> queryPathConfigFromContainerToEntity;

    public ComparisonInput getEntityFilter() {
        return entityFilter;
    }

    public void setEntityFilter(ComparisonInput entityFilter) {
        this.entityFilter = entityFilter;
    }

    public String getEntityFilterType() {
        return entityFilterType;
    }

    public void setEntityFilterType(String entityFilterType) {
        this.entityFilterType = entityFilterType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public DmoFilterLimitInput getFilterLimit() {
        return filterLimit;
    }

    public void setFilterLimit(DmoFilterLimitInput filterLimit) {
        this.filterLimit = filterLimit;
    }

    public ComparisonInput getInheritedFilter() {
        return inheritedFilter;
    }

    public void setInheritedFilter(ComparisonInput inheritedFilter) {
        this.inheritedFilter = inheritedFilter;
    }

    public String getInheritedFilterType() {
        return inheritedFilterType;
    }

    public void setInheritedFilterType(String inheritedFilterType) {
        this.inheritedFilterType = inheritedFilterType;
    }

    public List<QueryPathInputConfig> getQueryPathConfigForActivateOnToContainer() {
        return queryPathConfigForActivateOnToContainer;
    }

    public void setQueryPathConfigForActivateOnToContainer(List<QueryPathInputConfig> queryPathConfigForActivateOnToContainer) {
        this.queryPathConfigForActivateOnToContainer = queryPathConfigForActivateOnToContainer;
    }

    public List<QueryPathInputConfig> getQueryPathConfigFromContainerToEntity() {
        return queryPathConfigFromContainerToEntity;
    }

    public void setQueryPathConfigFromContainerToEntity(List<QueryPathInputConfig> queryPathConfigFromContainerToEntity) {
        this.queryPathConfigFromContainerToEntity = queryPathConfigFromContainerToEntity;
    }
}
