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
 * Mirrors ConnectApi.ContactPointSourceInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactPointSourceInput {

    @McpToolParam(description = "Data Source Id", required = false)
    private String dataSourceId;

    @McpToolParam(description = "Data Source Object", required = false)
    private String dataSourceObject;

    @McpToolParam(description = "Contact point preference. One of ContactPointPrefAny, ContactPointPrefBusiness, ContactPointPrefPersonal, ContactPointPrefPrimary.", required = false)
    private String dataSourcePreference;

    @McpToolParam(description = "Data Source Priority", required = false)
    private Integer dataSourcePriority;

    @McpToolParam(description = "Id", required = false)
    private String id;

    @McpToolParam(description = "Data Source name", required = false)
    private String name;

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceObject() {
        return dataSourceObject;
    }

    public void setDataSourceObject(String dataSourceObject) {
        this.dataSourceObject = dataSourceObject;
    }

    public String getDataSourcePreference() {
        return dataSourcePreference;
    }

    public void setDataSourcePreference(String dataSourcePreference) {
        this.dataSourcePreference = dataSourcePreference;
    }

    public Integer getDataSourcePriority() {
        return dataSourcePriority;
    }

    public void setDataSourcePriority(Integer dataSourcePriority) {
        this.dataSourcePriority = dataSourcePriority;
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
}
