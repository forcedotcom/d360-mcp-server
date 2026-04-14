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

import java.util.List;

/**
 * A reusable config block with an id and user-configurable values.
 * Used for chunking strategies, embedding models, index types, parsing stages, etc.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigInput {

    @McpToolParam(description = "Configuration identifier (e.g., passage_extraction, e5_large_v2, HNSW)")
    private String id;

    @McpToolParam(description = "User-configurable parameter values", required = false)
    private List<UserValueInput> userValues;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<UserValueInput> getUserValues() { return userValues; }
    public void setUserValues(List<UserValueInput> userValues) { this.userValues = userValues; }
}
