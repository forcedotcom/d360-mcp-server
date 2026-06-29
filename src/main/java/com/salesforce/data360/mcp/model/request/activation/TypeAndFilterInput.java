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
 * Mirrors ConnectApi.TypeAndFilterInput. Wrapper for logical comparison filters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TypeAndFilterInput {

    @McpToolParam(description = "Entity Filter", required = false)
    private ComparisonInput filter;

    @McpToolParam(description = "Entity Name", required = false)
    private String type;

    public ComparisonInput getFilter() {
        return filter;
    }

    public void setFilter(ComparisonInput filter) {
        this.filter = filter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
