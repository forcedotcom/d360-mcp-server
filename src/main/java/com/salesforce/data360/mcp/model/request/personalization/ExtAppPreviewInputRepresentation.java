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

/**
 * External App Preview Input Representation for generating mobile preview links.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtAppPreviewInputRepresentation {

    @McpToolParam(description = "Streaming App Data Connector ID. Required to resolve the preview base URL")
    private String dataConnectorId;

    @Valid
    @McpToolParam(description = "Personalization Experience Config to preview")
    private PersonalizationExperienceConfigInputRepresentation experienceConfig;

    @Valid
    @McpToolParam(description = "Personalization Transformer to preview")
    private TransformerInputRepresentation transformer;

    @Valid
    @McpToolParam(description = "Decision request body used to generate the preview")
    private DecisionRequestBodyInputRepresentation decisionRequestBody;

    public String getDataConnectorId() {
        return dataConnectorId;
    }

    public void setDataConnectorId(String dataConnectorId) {
        this.dataConnectorId = dataConnectorId;
    }

    public PersonalizationExperienceConfigInputRepresentation getExperienceConfig() {
        return experienceConfig;
    }

    public void setExperienceConfig(PersonalizationExperienceConfigInputRepresentation experienceConfig) {
        this.experienceConfig = experienceConfig;
    }

    public TransformerInputRepresentation getTransformer() {
        return transformer;
    }

    public void setTransformer(TransformerInputRepresentation transformer) {
        this.transformer = transformer;
    }

    public DecisionRequestBodyInputRepresentation getDecisionRequestBody() {
        return decisionRequestBody;
    }

    public void setDecisionRequestBody(DecisionRequestBodyInputRepresentation decisionRequestBody) {
        this.decisionRequestBody = decisionRequestBody;
    }
}
