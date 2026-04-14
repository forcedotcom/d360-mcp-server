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
package com.salesforce.data360.mcp.model.request.identityresolution;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * Request body for running an Identity Resolution ruleset.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityResolutionRunRequest {

    @McpToolParam(description = "Calling application name", required = false)
    private String callingApp;

    @McpToolParam(description = "Additional calling application info", required = false)
    private String callingAppInfo;

    public String getCallingApp() {
        return callingApp;
    }

    public void setCallingApp(String callingApp) {
        this.callingApp = callingApp;
    }

    public String getCallingAppInfo() {
        return callingAppInfo;
    }

    public void setCallingAppInfo(String callingAppInfo) {
        this.callingAppInfo = callingAppInfo;
    }
}
