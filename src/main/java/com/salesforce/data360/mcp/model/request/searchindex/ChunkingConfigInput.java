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
 * Chunking configuration for a search index.
 * Use fieldLevelConfigurations for structured DMOs, fileLevelConfiguration for unstructured,
 * or both when attachmentDmoDeveloperName is set.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChunkingConfigInput {

    @McpToolParam(description = "Field-level chunking configurations for structured DMO fields", required = false)
    private List<FieldLevelConfigInput> fieldLevelConfigurations;

    @McpToolParam(description = "File-level chunking configuration for unstructured data", required = false)
    private FileLevelConfigInput fileLevelConfiguration;

    public List<FieldLevelConfigInput> getFieldLevelConfigurations() { return fieldLevelConfigurations; }
    public void setFieldLevelConfigurations(List<FieldLevelConfigInput> fieldLevelConfigurations) { this.fieldLevelConfigurations = fieldLevelConfigurations; }

    public FileLevelConfigInput getFileLevelConfiguration() { return fileLevelConfiguration; }
    public void setFileLevelConfiguration(FileLevelConfigInput fileLevelConfiguration) { this.fileLevelConfiguration = fileLevelConfiguration; }
}
