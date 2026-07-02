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
 * Mirrors ConnectApi.ActivationAdditionalAttributesConfigInput.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributesConfigInput {

    @McpToolParam(description = "Activation Platform Attr Id", required = false)
    private String activationPlatformAttrId;

    @McpToolParam(description = "Data Source Type", required = false)
    private String dataSourceType;

    @McpToolParam(description = "Entity Name", required = false)
    private String entityName;

    @McpToolParam(description = "external platform attribute destination name", required = false)
    private String externalPlatformAttributeName;

    @McpToolParam(description = "Filter Expression", required = false)
    private List<AttributeFilterInput> filterExpression;

    @McpToolParam(description = "Is Rolluppable attribute", required = false)
    private Boolean isRolluppable;

    @McpToolParam(description = "Attribute Label", required = false)
    private String label;

    @McpToolParam(description = "Attribute Name", required = false)
    private String name;

    @McpToolParam(description = "preferred name", required = false)
    private String preferredName;

    @McpToolParam(description = "Query Path (Join path to reach the attribute from segmentedOn Entity)", required = false)
    private List<QueryPathInputConfig> queryPathConfig;

    @McpToolParam(description = "ref Attribute Developer Name", required = false)
    private String referenceAttributeName;

    @McpToolParam(description = "Data export attribute source. One of Direct, Related.", required = false)
    private String source;

    @McpToolParam(description = "Data export attribute type. One of ComputedDimension, ComputedMeasure, Model, ModelRelated, NonAggregatableComputedMeasure.", required = false)
    private String type;

    public String getActivationPlatformAttrId() {
        return activationPlatformAttrId;
    }

    public void setActivationPlatformAttrId(String activationPlatformAttrId) {
        this.activationPlatformAttrId = activationPlatformAttrId;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getExternalPlatformAttributeName() {
        return externalPlatformAttributeName;
    }

    public void setExternalPlatformAttributeName(String externalPlatformAttributeName) {
        this.externalPlatformAttributeName = externalPlatformAttributeName;
    }

    public List<AttributeFilterInput> getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(List<AttributeFilterInput> filterExpression) {
        this.filterExpression = filterExpression;
    }

    public Boolean getIsRolluppable() {
        return isRolluppable;
    }

    public void setIsRolluppable(Boolean isRolluppable) {
        this.isRolluppable = isRolluppable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public List<QueryPathInputConfig> getQueryPathConfig() {
        return queryPathConfig;
    }

    public void setQueryPathConfig(List<QueryPathInputConfig> queryPathConfig) {
        this.queryPathConfig = queryPathConfig;
    }

    public String getReferenceAttributeName() {
        return referenceAttributeName;
    }

    public void setReferenceAttributeName(String referenceAttributeName) {
        this.referenceAttributeName = referenceAttributeName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
