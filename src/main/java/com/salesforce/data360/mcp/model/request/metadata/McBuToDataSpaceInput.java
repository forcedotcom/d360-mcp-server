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
package com.salesforce.data360.mcp.model.request.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Mirrors {@code McBuToDataSpaceInputRepresentation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McBuToDataSpaceInput {

    @McpToolParam(description = "Name of the dataspace", required = false)
    private String dataspaceName;

    @McpToolParam(description = "MID of the Marketing Cloud account", required = false)
    private String mid;

    public String getDataspaceName() {
        return dataspaceName;
    }

    public void setDataspaceName(String dataspaceName) {
        this.dataspaceName = dataspaceName;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}
