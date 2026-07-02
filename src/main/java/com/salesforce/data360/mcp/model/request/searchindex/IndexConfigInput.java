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
package com.salesforce.data360.mcp.model.request.searchindex;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/**
 * Index configuration for HYBRID search with keyword search fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexConfigInput {

    @McpToolParam(description = "Context field configurations — retrievable-only fields", required = false)
    private List<ContextFieldConfigurationInput> contextFieldConfiguration;

    @McpToolParam(description = "Record field configurations — searchable and retrievable fields for keyword search", required = false)
    private List<RecordFieldConfigurationInput> recordFieldConfiguration;

    @McpToolParam(description = "Whether to vectorize the record chunks for searchable fields", required = false)
    private Boolean shouldVectorizeSearchableFields;

    public List<ContextFieldConfigurationInput> getContextFieldConfiguration() { return contextFieldConfiguration; }
    public void setContextFieldConfiguration(List<ContextFieldConfigurationInput> contextFieldConfiguration) { this.contextFieldConfiguration = contextFieldConfiguration; }

    public List<RecordFieldConfigurationInput> getRecordFieldConfiguration() { return recordFieldConfiguration; }
    public void setRecordFieldConfiguration(List<RecordFieldConfigurationInput> recordFieldConfiguration) { this.recordFieldConfiguration = recordFieldConfiguration; }

    public Boolean getShouldVectorizeSearchableFields() { return shouldVectorizeSearchableFields; }
    public void setShouldVectorizeSearchableFields(Boolean shouldVectorizeSearchableFields) { this.shouldVectorizeSearchableFields = shouldVectorizeSearchableFields; }
}
