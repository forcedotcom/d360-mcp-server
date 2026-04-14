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
package com.salesforce.data360.mcp.model.request.sdm;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/** Request body for creating a semantic data model. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SdmModelCreateRequest {

    @McpToolParam(description = "API name of the semantic model")
    private String apiName;

    @McpToolParam(description = "Display label of the semantic model")
    private String label;

    @McpToolParam(description = "Description of the semantic model", required = false)
    private String description;

    @McpToolParam(description = "Dataspace name (required in body)")
    private String dataspace;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataspace() {
        return dataspace;
    }

    public void setDataspace(String dataspace) {
        this.dataspace = dataspace;
    }
}
