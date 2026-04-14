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
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Request to create an activation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationCreateRequest {

    @NotBlank
    @McpToolParam(description = "Name of the activation")
    private String name;

    @NotBlank
    @McpToolParam(description = "Refresh type (e.g., FULL, INCREMENTAL)")
    private String refreshType;

    @NotBlank
    @McpToolParam(description = "Data space name")
    private String dataSpaceName;

    @McpToolParam(description = "Limit value")
    private Integer limitValue;

    @McpToolParam(description = "Curated entity configuration")
    private CuratedEntityInput curatedEntity;

    @McpToolParam(description = "Activation target subject configuration")
    private ActivationTargetSubjectConfigInput activationTargetSubjectConfig;

    @McpToolParam(description = "Attribute limiting expression configuration")
    private AttributeLimitingExpressionConfigInput attributeLimitingExpressionConfig;

    @McpToolParam(description = "Array of data source configurations")
    private List<ActivationDataSourceConfigInput> dataSourcesConfig;

    @McpToolParam(description = "Name of the activation target", required = false)
    private String activationTargetName;

    @McpToolParam(description = "API name of the segment to activate", required = false)
    private String segmentApiName;

    @McpToolParam(description = "Description", required = false)
    private String description;

    @McpToolParam(description = "Customer file source: FirstAndThirdParty, FirstParty, ThirdParty", required = false)
    private String customerFileSource;

    @McpToolParam(description = "Whether to exclude deletes", required = false)
    private Boolean shouldExcludeDeletes;

    @McpToolParam(description = "Whether to exclude updates", required = false)
    private Boolean shouldExcludeUpdates;

    @McpToolParam(description = "Attributes config", required = false)
    private List<AttributesConfigInput> attributesConfig;

    @McpToolParam(description = "Contact points config", required = false)
    private List<ContactPointConfigInput> contactPointsConfig;

    @McpToolParam(description = "Direct DMO filters config", required = false)
    private List<DMOFilterConfigInput> directDmoFiltersConfig;

    @McpToolParam(description = "Related DMO filters config", required = false)
    private List<DMOFilterConfigInput> relatedDmoFiltersConfig;

    @McpToolParam(description = "Static data config", required = false)
    private List<StaticDataConfigInput> staticDataConfig;

    @McpToolParam(description = "Activation mapping schema", required = false)
    private String activationMappingSchema;

    @McpToolParam(description = "Data export definition ID", required = false)
    private String dataExportDefinitionId;

    @McpToolParam(description = "Market segment ID", required = false)
    private String marketSegmentId;

    @McpToolParam(description = "ID (inherited)", required = false)
    private String id;

    @McpToolParam(description = "Namespace (inherited)", required = false)
    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefreshType() {
        return refreshType;
    }

    public void setRefreshType(String refreshType) {
        this.refreshType = refreshType;
    }

    public String getDataSpaceName() {
        return dataSpaceName;
    }

    public void setDataSpaceName(String dataSpaceName) {
        this.dataSpaceName = dataSpaceName;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    public CuratedEntityInput getCuratedEntity() {
        return curatedEntity;
    }

    public void setCuratedEntity(CuratedEntityInput curatedEntity) {
        this.curatedEntity = curatedEntity;
    }

    public ActivationTargetSubjectConfigInput getActivationTargetSubjectConfig() {
        return activationTargetSubjectConfig;
    }

    public void setActivationTargetSubjectConfig(ActivationTargetSubjectConfigInput activationTargetSubjectConfig) {
        this.activationTargetSubjectConfig = activationTargetSubjectConfig;
    }

    public AttributeLimitingExpressionConfigInput getAttributeLimitingExpressionConfig() {
        return attributeLimitingExpressionConfig;
    }

    public void setAttributeLimitingExpressionConfig(AttributeLimitingExpressionConfigInput attributeLimitingExpressionConfig) {
        this.attributeLimitingExpressionConfig = attributeLimitingExpressionConfig;
    }

    public List<ActivationDataSourceConfigInput> getDataSourcesConfig() {
        return dataSourcesConfig;
    }

    public void setDataSourcesConfig(List<ActivationDataSourceConfigInput> dataSourcesConfig) {
        this.dataSourcesConfig = dataSourcesConfig;
    }

    public String getActivationTargetName() {
        return activationTargetName;
    }

    public void setActivationTargetName(String activationTargetName) {
        this.activationTargetName = activationTargetName;
    }

    public String getSegmentApiName() {
        return segmentApiName;
    }

    public void setSegmentApiName(String segmentApiName) {
        this.segmentApiName = segmentApiName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCustomerFileSource() {
        return customerFileSource;
    }

    public void setCustomerFileSource(String customerFileSource) {
        this.customerFileSource = customerFileSource;
    }

    public Boolean getShouldExcludeDeletes() {
        return shouldExcludeDeletes;
    }

    public void setShouldExcludeDeletes(Boolean shouldExcludeDeletes) {
        this.shouldExcludeDeletes = shouldExcludeDeletes;
    }

    public Boolean getShouldExcludeUpdates() {
        return shouldExcludeUpdates;
    }

    public void setShouldExcludeUpdates(Boolean shouldExcludeUpdates) {
        this.shouldExcludeUpdates = shouldExcludeUpdates;
    }

    public List<AttributesConfigInput> getAttributesConfig() {
        return attributesConfig;
    }

    public void setAttributesConfig(List<AttributesConfigInput> attributesConfig) {
        this.attributesConfig = attributesConfig;
    }

    public List<ContactPointConfigInput> getContactPointsConfig() {
        return contactPointsConfig;
    }

    public void setContactPointsConfig(List<ContactPointConfigInput> contactPointsConfig) {
        this.contactPointsConfig = contactPointsConfig;
    }

    public List<DMOFilterConfigInput> getDirectDmoFiltersConfig() {
        return directDmoFiltersConfig;
    }

    public void setDirectDmoFiltersConfig(List<DMOFilterConfigInput> directDmoFiltersConfig) {
        this.directDmoFiltersConfig = directDmoFiltersConfig;
    }

    public List<DMOFilterConfigInput> getRelatedDmoFiltersConfig() {
        return relatedDmoFiltersConfig;
    }

    public void setRelatedDmoFiltersConfig(List<DMOFilterConfigInput> relatedDmoFiltersConfig) {
        this.relatedDmoFiltersConfig = relatedDmoFiltersConfig;
    }

    public List<StaticDataConfigInput> getStaticDataConfig() {
        return staticDataConfig;
    }

    public void setStaticDataConfig(List<StaticDataConfigInput> staticDataConfig) {
        this.staticDataConfig = staticDataConfig;
    }

    public String getActivationMappingSchema() {
        return activationMappingSchema;
    }

    public void setActivationMappingSchema(String activationMappingSchema) {
        this.activationMappingSchema = activationMappingSchema;
    }

    public String getDataExportDefinitionId() {
        return dataExportDefinitionId;
    }

    public void setDataExportDefinitionId(String dataExportDefinitionId) {
        this.dataExportDefinitionId = dataExportDefinitionId;
    }

    public String getMarketSegmentId() {
        return marketSegmentId;
    }

    public void setMarketSegmentId(String marketSegmentId) {
        this.marketSegmentId = marketSegmentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
