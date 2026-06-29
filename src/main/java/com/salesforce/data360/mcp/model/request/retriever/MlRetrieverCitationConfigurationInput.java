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
package com.salesforce.data360.mcp.model.request.retriever;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mirror of {@code MlRetrieverCitationConfigurationInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MlRetrieverCitationConfigurationInput {

    @McpToolParam(description = "Optionally base URL field to be appended before mappedUrlField if set", required = false)
    private String baseUrl;

    @McpToolParam(description = "Optionally mapped DMO field for label", required = false)
    private MlRetrieverCitationFieldInput mappedLabelField;

    @McpToolParam(description = "Optionally mapped DMO field for URL, this can be used in conjunction with the baseUrl field", required = false)
    private MlRetrieverCitationFieldInput mappedUrlField;

    @McpToolParam(description = "Configuration type", required = false)
    private String type;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public MlRetrieverCitationFieldInput getMappedLabelField() {
        return mappedLabelField;
    }

    public void setMappedLabelField(MlRetrieverCitationFieldInput mappedLabelField) {
        this.mappedLabelField = mappedLabelField;
    }

    public MlRetrieverCitationFieldInput getMappedUrlField() {
        return mappedUrlField;
    }

    public void setMappedUrlField(MlRetrieverCitationFieldInput mappedUrlField) {
        this.mappedUrlField = mappedUrlField;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
