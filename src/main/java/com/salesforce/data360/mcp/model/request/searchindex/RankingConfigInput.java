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
 * Ranking field configuration for hybrid search.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RankingConfigInput {

    @McpToolParam(description = "Developer name of the DMO", required = false)
    private String dmoDeveloperName;

    @McpToolParam(description = "Developer name of the DMO field", required = false)
    private String dmoFieldDeveloperName;

    @McpToolParam(description = "Sort order", required = false)
    private String sortOrder;

    public String getDmoDeveloperName() { return dmoDeveloperName; }
    public void setDmoDeveloperName(String dmoDeveloperName) { this.dmoDeveloperName = dmoDeveloperName; }

    public String getDmoFieldDeveloperName() { return dmoFieldDeveloperName; }
    public void setDmoFieldDeveloperName(String dmoFieldDeveloperName) { this.dmoFieldDeveloperName = dmoFieldDeveloperName; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
