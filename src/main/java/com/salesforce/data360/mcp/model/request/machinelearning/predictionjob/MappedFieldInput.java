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
package com.salesforce.data360.mcp.model.request.machinelearning.predictionjob;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Reference to a DMO field, with optional relationship path for fields which come from an adjacent DMO.
 * Inherits id/name from {@link AssetReferenceInput}, including the
 * class-level "id or name required" constraint enforced via {@code @AssetRefIdOrName}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappedFieldInput extends AssetReferenceInput {

    @Valid
    @NotNull
    @McpToolParam(description = "Reference to the DMO that owns this field.")
    private AssetReferenceInput dataObject;

    @Valid
    @McpToolParam(description = "Join path when this field is reached via a related DMO. Empty list when the field is on the input DMO itself.", required = false)
    private List<FieldRelationshipInput> relationshipPath;

    public AssetReferenceInput getDataObject() {
        return dataObject;
    }

    public void setDataObject(AssetReferenceInput dataObject) {
        this.dataObject = dataObject;
    }

    public List<FieldRelationshipInput> getRelationshipPath() {
        return relationshipPath;
    }

    public void setRelationshipPath(List<FieldRelationshipInput> relationshipPath) {
        this.relationshipPath = relationshipPath;
    }
}
