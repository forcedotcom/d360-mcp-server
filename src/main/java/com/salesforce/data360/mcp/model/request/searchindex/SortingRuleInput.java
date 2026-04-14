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
package com.salesforce.data360.mcp.model.request.searchindex;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * A sorting rule within a rule configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SortingRuleInput {

    @McpToolParam(description = "Field to sort by", required = false)
    private String field;

    @McpToolParam(description = "Sort direction (e.g., ASC, DESC)", required = false)
    private String direction;

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
