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
package com.salesforce.data360.mcp.model.request.machinelearning.setup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Input source for model-setup. Can be DMO, CIO or Multi-source
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputSourceInput {

    @NotBlank
    @McpToolParam(description = "Source kind. One of: DataModelObject, CalculatedInsightObject, SdmObject, MultiSourceObjects. "
        + "For the first three, set 'source' (required) and 'dataSpace' and leave 'sources' empty. "
        + "For MultiSourceObjects, set 'sources' (a list of single-source entries) and leave 'source'/'dataSpace'/'primary' empty. ")
    private String type;

    @Valid
    @McpToolParam(description = "Single-source only. Reference to the DMO/CIO/SDM entity (by name or id).", required = false)
    private AssetReferenceInput source;

    @Valid
    @McpToolParam(description = "Single-source only. Dataspace the source lives in.", required = false)
    private AssetReferenceInput dataSpace;

    @McpToolParam(description = "Marks this entry as the primary source (relevant for MultiSourceObjects).", required = false)
    private Boolean primary;

    @Valid
    @McpToolParam(description = "MultiSourceObjects only. List of single-source entries (each with its own type/source/dataSpace; " +
            "mark exactly one as primary=true).", required = false)
    private List<InputSourceInput> sources;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AssetReferenceInput getSource() {
        return source;
    }

    public void setSource(AssetReferenceInput source) {
        this.source = source;
    }

    public AssetReferenceInput getDataSpace() {
        return dataSpace;
    }

    public void setDataSpace(AssetReferenceInput dataSpace) {
        this.dataSpace = dataSpace;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public List<InputSourceInput> getSources() {
        return sources;
    }

    public void setSources(List<InputSourceInput> sources) {
        this.sources = sources;
    }
}
