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
package com.salesforce.data360.mcp.model.request.activation;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Flat union of ConnectApi.SubjectBaseInput, ConnectApi.SubjectInput, and
 * ConnectApi.LabeledSubjectInputRepresentation. The label fields are optional and
 * only populated when the slot expects a labeled subject (i.e. inside
 * {@link LabeledSubjectsConfigInput}); they are omitted otherwise.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubjectInput {

    @McpToolParam(description = "Field name.", required = false)
    private String fieldName;

    @McpToolParam(description = "Object/DMO name.", required = false)
    private String objectName;

    @McpToolParam(description = "Optional field label. Populated only for labeled-subject slots.", required = false)
    private String fieldLabel;

    @McpToolParam(description = "Optional object label. Populated only for labeled-subject slots.", required = false)
    private String objectLabel;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getObjectLabel() {
        return objectLabel;
    }

    public void setObjectLabel(String objectLabel) {
        this.objectLabel = objectLabel;
    }
}
