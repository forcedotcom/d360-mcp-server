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
package com.salesforce.data360.mcp.model.request.dlo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Filter expression for data space.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterExpressionDataSpaceInput {

    @McpToolParam(description = "Filter operator: And_Operator, Or_Operator", required = false)
    private String filterOperator;

    @McpToolParam(description = "Filter type: Condition", required = false)
    private String filterType;

    @McpToolParam(description = "Array of filter conditions", required = false)
    private List<FilterDataSpaceInput> filters;

    public String getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(String filterOperator) {
        this.filterOperator = filterOperator;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public List<FilterDataSpaceInput> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDataSpaceInput> filters) {
        this.filters = filters;
    }
}
