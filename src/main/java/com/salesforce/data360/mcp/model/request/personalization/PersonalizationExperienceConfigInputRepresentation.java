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
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Personalization Experience Config Input Representation.
 * Main configuration for a personalization experience.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonalizationExperienceConfigInputRepresentation extends BasePersonalizationInputRepresentation {

    @McpToolParam(description = "Id of the Personalization Experience Config (read-only, ignored on input)")
    private String id;

    @Valid
    @McpToolParam(description = "What is supplying the data for this Personalization Experience?")
    private DataProviderInputRepresentation dataProvider;

    @Valid
    @McpToolParam(description = "How to find the source where this personalization will occur which for web is which web pages")
    private List<SourceMatcherInputRepresentation> sourceMatchers;

    @Valid
    @McpToolParam(description = "How will the data provided by the Data Provider be transformed?")
    private TransformationConfigInputRepresentation transformationConfig;

    @McpToolParam(description = "The datetime when this Personalization Experience Config was last published (read-only, ignored on input)")
    private String publishedDate;

    @McpToolParam(description = "Id of the user who created this Personalization Experience Config (read-only, ignored on input)")
    private String createdById;

    @McpToolParam(description = "The datetime when this Personalization Experience Config was created (read-only, ignored on input)")
    private String createdDate;

    @McpToolParam(description = "Id of the user who last modified this Personalization Experience Config (read-only, ignored on input)")
    private String lastModifiedById;

    @McpToolParam(description = "The datetime when this Personalization Experience Config was last modified (read-only, ignored on input)")
    private String lastModifiedDate;

    @McpToolParam(description = "Whether or not this particular configuration is enabled")
    private Boolean isEnabled;

    @McpToolParam(description = "When editing this configuration on the Website which page of the website should this configuration be displayed on if not the current page")
    private String displayUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataProviderInputRepresentation getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProviderInputRepresentation dataProvider) {
        this.dataProvider = dataProvider;
    }

    public List<SourceMatcherInputRepresentation> getSourceMatchers() {
        return sourceMatchers;
    }

    public void setSourceMatchers(List<SourceMatcherInputRepresentation> sourceMatchers) {
        this.sourceMatchers = sourceMatchers;
    }

    public TransformationConfigInputRepresentation getTransformationConfig() {
        return transformationConfig;
    }

    public void setTransformationConfig(TransformationConfigInputRepresentation transformationConfig) {
        this.transformationConfig = transformationConfig;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedById() {
        return lastModifiedById;
    }

    public void setLastModifiedById(String lastModifiedById) {
        this.lastModifiedById = lastModifiedById;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }
}
