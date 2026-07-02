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
import jakarta.validation.Valid;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;

/** Request body for {@code POST /ssot/machine-learning/query-data-profile}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataProfileRequest extends SetupQueryBaseRequest {

    @Valid
    @McpToolParam(description = "Optional. Fields to profile, each as a typed reference (works for multi-source / related-object fields). " +
            "Omit to profile all eligible fields on the primary source.", required = false)
    private List<FieldSourceInput> profileFieldNames;

    public List<FieldSourceInput> getProfileFieldNames() {
        return profileFieldNames;
    }

    public void setProfileFieldNames(List<FieldSourceInput> profileFieldNames) {
        this.profileFieldNames = profileFieldNames;
    }
}
