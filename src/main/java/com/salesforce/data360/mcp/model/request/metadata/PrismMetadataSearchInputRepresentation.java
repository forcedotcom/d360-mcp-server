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
package com.salesforce.data360.mcp.model.request.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/** Input representation for Prism Metadata Search request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrismMetadataSearchInputRepresentation {

    @McpToolParam(description = "Search query")
    private String query;

    @McpToolParam(description = "Pagination settings", required = false)
    private PrismMetadataSearchPaginationInputRepresentation pagination;

    @McpToolParam(description = "Filters to apply to the search", required = false)
    private List<PrismMetadataSearchFilterInputRepresentation> filters;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public PrismMetadataSearchPaginationInputRepresentation getPagination() {
        return pagination;
    }

    public void setPagination(PrismMetadataSearchPaginationInputRepresentation pagination) {
        this.pagination = pagination;
    }

    public List<PrismMetadataSearchFilterInputRepresentation> getFilters() {
        return filters;
    }

    public void setFilters(List<PrismMetadataSearchFilterInputRepresentation> filters) {
        this.filters = filters;
    }
}
