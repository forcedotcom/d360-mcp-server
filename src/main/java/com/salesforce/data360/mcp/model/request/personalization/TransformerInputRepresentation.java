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
import java.util.Map;

/**
 * Represents the input for creating or updating a Transformer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformerInputRepresentation extends BasePersonalizationInputRepresentation {

    @McpToolParam(description = "List of Websites or Mobile Apps this Personalization Transformation belongs to. This can be either id, name or appSourceId of DataCloud's StreamingAppDataConnector")
    private List<String> dataConnectors;

    @McpToolParam(description = "Is this Transformer for a Website or Mobile Application. Values: MobileApp, WebApp")
    private String channel;

    @Valid
    @McpToolParam(description = "Map of substitution variable names to their definitions. Each key is a substitution variable name, each value contains label, description, configType, defaultValue, overridable, and required properties")
    private Map<String, SubstitutionInputDetails> substitutionDefinitions;

    @McpToolParam(description = "If the Transformer's Substitution Definitions were created referencing a Personalization Schema, the name of that Schema. This will allow UI's to provide an optional guardrail when selecting Personalization Points to use when creating Personalization Experiences")
    private String schemaReference;

    @McpToolParam(description = "The Name of the Dataspace the Transformer belongs to")
    private String dataSpace;

    @McpToolParam(description = "When delivered to the Mobile App or Website, will this Transformer be enabled?")
    private Boolean isEnabled;

    @McpToolParam(description = "Instruction to the SDK on how the Transformer will work and what SDK code to use when rendering Transformer. Values: Handlebars, HtmlElementModifier, AgentScript, Component")
    private String transformerType;

    @McpToolParam(description = "Thumbnail image for this Transformer (can be a URL or base64-encoded data)")
    private String thumbnailImage;

    @Valid
    @McpToolParam(description = "Data specific to the type of transformer (html, script, componentName, disableAutoInjectionOfEngagementAttributes)")
    private TransformerInputDetails transformerTypeDetails;

    public List<String> getDataConnectors() {
        return dataConnectors;
    }

    public void setDataConnectors(List<String> dataConnectors) {
        this.dataConnectors = dataConnectors;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Map<String, SubstitutionInputDetails> getSubstitutionDefinitions() {
        return substitutionDefinitions;
    }

    public void setSubstitutionDefinitions(Map<String, SubstitutionInputDetails> substitutionDefinitions) {
        this.substitutionDefinitions = substitutionDefinitions;
    }

    public String getSchemaReference() {
        return schemaReference;
    }

    public void setSchemaReference(String schemaReference) {
        this.schemaReference = schemaReference;
    }

    public String getDataSpace() {
        return dataSpace;
    }

    public void setDataSpace(String dataSpace) {
        this.dataSpace = dataSpace;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getTransformerType() {
        return transformerType;
    }

    public void setTransformerType(String transformerType) {
        this.transformerType = transformerType;
    }

    public String getThumbnailImage() {
        return thumbnailImage;
    }

    public void setThumbnailImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    public TransformerInputDetails getTransformerTypeDetails() {
        return transformerTypeDetails;
    }

    public void setTransformerTypeDetails(TransformerInputDetails transformerTypeDetails) {
        this.transformerTypeDetails = transformerTypeDetails;
    }
}
