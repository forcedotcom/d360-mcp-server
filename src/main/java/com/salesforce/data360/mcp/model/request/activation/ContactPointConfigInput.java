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
 * Contact point configuration for activation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactPointConfigInput {

    @McpToolParam(description = "Array of activation contact point objects. Each has: entityName, type (Email|Phone|Push|etc), externalPlatformHashMethod, queryPathConfig, attributesConfig, sourcesConfig, filterExpression", required = false)
    private List<Map<String, Object>> contactPoints;

    @McpToolParam(description = "ID", required = false)
    private String id;

    @McpToolParam(description = "Name", required = false)
    private String name;

    @McpToolParam(description = "Namespace", required = false)
    private String namespace;

    public List<Map<String, Object>> getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(List<Map<String, Object>> contactPoints) {
        this.contactPoints = contactPoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
