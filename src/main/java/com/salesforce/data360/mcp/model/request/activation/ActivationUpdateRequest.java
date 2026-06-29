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
 * Request body for updating a Connect API activation (PUT /ssot/activations/{id}).
 * All fields optional; populate only what you want to change.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivationUpdateRequest {

    @McpToolParam(description = "Name of the activation", required = false)
    private String name;

    @McpToolParam(description = "Activation Refresh Type (FULL_REFRESH/INCREMENTAL)", required = false)
    private String refreshType;

    @McpToolParam(description = "Dataspace name of Activation", required = false)
    private String dataSpaceName;

    @McpToolParam(description = "Description of the activation", required = false)
    private String description;

    @McpToolParam(description = "Activation type: Segment or ApiTriggered. Defaults to Segment when unspecified", required = false)
    private String activationType;

    @McpToolParam(description = "Activation template ID.", required = false)
    private String activationTemplateId;

    @McpToolParam(description = "Platform name.", required = false)
    private String platformName;

    @McpToolParam(description = "Name for the activation target. Either activationTargetName or dataExportDefinitionId must be present.", required = false)
    private String activationTargetName;

    @McpToolParam(description = "Activation target ID for the activation. Either activationTargetName or dataExportDefinitionId must be present.", required = false)
    private String dataExportDefinitionId;

    @McpToolParam(description = "Developer name of the segment. Either marketSegmentId or segmentApiName must be present. Exclude this property for ApiTriggered activation types.", required = false)
    private String segmentApiName;

    @McpToolParam(description = "Segment ID of the segment the activation needs to be created against. Either marketSegmentId or segmentApiName must be present. Exclude this property for ApiTriggered activation types.", required = false)
    private String marketSegmentId;

    @McpToolParam(description = "Name (developer/api name) of the source DMO. Required when activationType is ApiTriggered.", required = false)
    private String sourceDmoName;

    @McpToolParam(description = "Activation Target Subject Config ", required = false)
    private ActivationTargetSubjectConfigInput activationTargetSubjectConfig;

    @McpToolParam(description = "Curated entity", required = false)
    private CuratedEntityInput curatedEntity;

    @McpToolParam(description = "Limit Value", required = false)
    private Integer limitValue;

    @McpToolParam(description = "Customer file source. One of FirstAndThirdParty, FirstParty, ThirdParty.", required = false)
    private String customerFileSource;

    @McpToolParam(description = "Type of identifier to use for activation audience matching, such as PII or ANON.", required = false)
    private String activationMappingSchema;

    @McpToolParam(description = "Exclude records removed since the last refresh.", required = false)
    private Boolean shouldExcludeDeletes;

    @McpToolParam(description = "Exclude records modified since the last refresh.", required = false)
    private Boolean shouldExcludeUpdates;

    @McpToolParam(description = "Additional attributes for the activation.", required = false)
    private List<AttributesConfigInput> attributesConfig;

    @McpToolParam(description = "Contact points configuration", required = false)
    private List<ContactPointConfigInput> contactPointsConfig;

    @McpToolParam(description = "Direct DMO filters configuration", required = false)
    private List<DMOFilterConfigInput> directDmoFiltersConfig;

    @McpToolParam(description = "Related DMO filters configuration.", required = false)
    private List<DMOFilterConfigInput> relatedDmoFiltersConfig;

    @McpToolParam(description = "Attribute limiting expression input configuration", required = false)
    private List<AttributeLimitingExpressionInput> attributeLimitingExpressionConfig;

    @McpToolParam(description = "Identity match boost provider configuration.", required = false)
    private List<IdentityMatchBoostProviderInput> identityProviderConfig;

    @McpToolParam(description = "Activation Data Sources Configuration", required = false)
    private List<ActivationDataSourceConfigInput> dataSourcesConfig;

    @McpToolParam(description = "Configuration of static data, which adds metadata or campaign details in the output. For example, campaignId or campaignName.", required = false)
    private List<StaticDataInput> staticDataConfig;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivationType() {
        return activationType;
    }

    public void setActivationType(String activationType) {
        this.activationType = activationType;
    }

    public String getActivationTemplateId() {
        return activationTemplateId;
    }

    public void setActivationTemplateId(String activationTemplateId) {
        this.activationTemplateId = activationTemplateId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getActivationTargetName() {
        return activationTargetName;
    }

    public void setActivationTargetName(String activationTargetName) {
        this.activationTargetName = activationTargetName;
    }

    public String getDataExportDefinitionId() {
        return dataExportDefinitionId;
    }

    public void setDataExportDefinitionId(String dataExportDefinitionId) {
        this.dataExportDefinitionId = dataExportDefinitionId;
    }

    public String getSegmentApiName() {
        return segmentApiName;
    }

    public void setSegmentApiName(String segmentApiName) {
        this.segmentApiName = segmentApiName;
    }

    public String getMarketSegmentId() {
        return marketSegmentId;
    }

    public void setMarketSegmentId(String marketSegmentId) {
        this.marketSegmentId = marketSegmentId;
    }

    public String getSourceDmoName() {
        return sourceDmoName;
    }

    public void setSourceDmoName(String sourceDmoName) {
        this.sourceDmoName = sourceDmoName;
    }

    public ActivationTargetSubjectConfigInput getActivationTargetSubjectConfig() {
        return activationTargetSubjectConfig;
    }

    public void setActivationTargetSubjectConfig(ActivationTargetSubjectConfigInput activationTargetSubjectConfig) {
        this.activationTargetSubjectConfig = activationTargetSubjectConfig;
    }

    public CuratedEntityInput getCuratedEntity() {
        return curatedEntity;
    }

    public void setCuratedEntity(CuratedEntityInput curatedEntity) {
        this.curatedEntity = curatedEntity;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(Integer limitValue) {
        this.limitValue = limitValue;
    }

    public String getCustomerFileSource() {
        return customerFileSource;
    }

    public void setCustomerFileSource(String customerFileSource) {
        this.customerFileSource = customerFileSource;
    }

    public String getActivationMappingSchema() {
        return activationMappingSchema;
    }

    public void setActivationMappingSchema(String activationMappingSchema) {
        this.activationMappingSchema = activationMappingSchema;
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

    public List<AttributeLimitingExpressionInput> getAttributeLimitingExpressionConfig() {
        return attributeLimitingExpressionConfig;
    }

    public void setAttributeLimitingExpressionConfig(List<AttributeLimitingExpressionInput> attributeLimitingExpressionConfig) {
        this.attributeLimitingExpressionConfig = attributeLimitingExpressionConfig;
    }

    public List<IdentityMatchBoostProviderInput> getIdentityProviderConfig() {
        return identityProviderConfig;
    }

    public void setIdentityProviderConfig(List<IdentityMatchBoostProviderInput> identityProviderConfig) {
        this.identityProviderConfig = identityProviderConfig;
    }

    public List<ActivationDataSourceConfigInput> getDataSourcesConfig() {
        return dataSourcesConfig;
    }

    public void setDataSourcesConfig(List<ActivationDataSourceConfigInput> dataSourcesConfig) {
        this.dataSourcesConfig = dataSourcesConfig;
    }

    public List<StaticDataInput> getStaticDataConfig() {
        return staticDataConfig;
    }

    public void setStaticDataConfig(List<StaticDataInput> staticDataConfig) {
        this.staticDataConfig = staticDataConfig;
    }
}
