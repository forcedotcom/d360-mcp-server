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
package com.salesforce.data360.mcp.model.request.dlo;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import com.salesforce.data360.mcp.model.request.datastream.DataLakeFieldInput;

import java.util.List;

/**
 * Request body for updating a Data Lake Object.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DloPatchRequest {

    @McpToolParam(description = "Updated display label", required = false)
    private String label;

    @McpToolParam(description = "Array of field definitions", required = false)
    private List<DataLakeFieldInput> dataLakeFieldInputRepresentations;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<DataLakeFieldInput> getDataLakeFieldInputRepresentations() {
        return dataLakeFieldInputRepresentations;
    }

    public void setDataLakeFieldInputRepresentations(List<DataLakeFieldInput> dataLakeFieldInputRepresentations) {
        this.dataLakeFieldInputRepresentations = dataLakeFieldInputRepresentations;
    }
}
