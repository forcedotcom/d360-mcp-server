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

/**
 * Mirrors ConnectApi.ActivationContactPointInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactPointConfigInput {

    @McpToolParam(description = "Contact Point Attributes Config", required = false)
    private List<ContactPointAttributeInput> attributesConfig;

    @McpToolParam(description = "Entity Name", required = false)
    private String entityName;

    @McpToolParam(description = "External Platform Hash Method", required = false)
    private String externalPlatformHashMethod;

    @McpToolParam(description = "Contact Point Filter Expression", required = false)
    private List<DMOFilterConfigInput> filterExpression;

    @McpToolParam(description = "Query Path", required = false)
    private List<QueryPathInputConfig> queryPathConfig;

    @McpToolParam(description = "Sources Config", required = false)
    private List<ContactPointSourceInput> sourcesConfig;

    @McpToolParam(description = "Contact point type. One of Email, Maid, Ott, Phone, Push, SubscriberKeyEmail, SubscriberKeyPhone, WhatsApp.", required = false)
    private String type;

    public List<ContactPointAttributeInput> getAttributesConfig() {
        return attributesConfig;
    }

    public void setAttributesConfig(List<ContactPointAttributeInput> attributesConfig) {
        this.attributesConfig = attributesConfig;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getExternalPlatformHashMethod() {
        return externalPlatformHashMethod;
    }

    public void setExternalPlatformHashMethod(String externalPlatformHashMethod) {
        this.externalPlatformHashMethod = externalPlatformHashMethod;
    }

    public List<DMOFilterConfigInput> getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(List<DMOFilterConfigInput> filterExpression) {
        this.filterExpression = filterExpression;
    }

    public List<QueryPathInputConfig> getQueryPathConfig() {
        return queryPathConfig;
    }

    public void setQueryPathConfig(List<QueryPathInputConfig> queryPathConfig) {
        this.queryPathConfig = queryPathConfig;
    }

    public List<ContactPointSourceInput> getSourcesConfig() {
        return sourcesConfig;
    }

    public void setSourcesConfig(List<ContactPointSourceInput> sourcesConfig) {
        this.sourcesConfig = sourcesConfig;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
