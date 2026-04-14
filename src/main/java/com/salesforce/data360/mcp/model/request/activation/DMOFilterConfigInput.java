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
import java.util.Map;

/**
 * DMO filter configuration for activation.
 * Contains deeply nested filter objects.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DMOFilterConfigInput {

    @McpToolParam(description = "Array of DMO filter objects. Each filter has: entityName (string), entityFilter (array), entityFilterType (string), inheritedFilter (array), inheritedFilterType (string), queryPathConfigForActivateOnToContainer (array), queryPathConfigFromContainerToEntity (array)", required = false)
    private List<Map<String, Object>> filters;

    public List<Map<String, Object>> getFilters() {
        return filters;
    }

    public void setFilters(List<Map<String, Object>> filters) {
        this.filters = filters;
    }
}
