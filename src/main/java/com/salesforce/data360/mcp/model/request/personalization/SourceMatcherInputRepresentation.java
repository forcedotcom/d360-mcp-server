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
package com.salesforce.data360.mcp.model.request.personalization;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Personalization Experience Config Source Matcher Input Representation.
 * Defines where the personalization will occur (which pages/locations).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceMatcherInputRepresentation {

    @McpToolParam(description = "How to match a web page for the source of a personalization experience config. Values: PageType, PageUrlMatcher, ContentZone")
    private String type;

    @McpToolParam(description = "Contents depends on SourceMatcherType. If SourceMatcherType.PageType then this will be a specific Page Type from the Site Map. If SourceMatcherType.PageUrlMatcher then this will be a Page URL which supports wildcards")
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
