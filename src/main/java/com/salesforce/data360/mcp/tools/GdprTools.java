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
package com.salesforce.data360.mcp.tools;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Data 360 GDPR Family Tools - data subject rights.
 * Uses /cdp/gdpr/ path prefix (non-SSOT Connect surface).
 */
@Component
public class GdprTools {

    private final Data360Client client;

    public GdprTools(Data360Client client) {
        this.client = client;
    }

    @McpTool(
        name = "d360_gdpr_read",
        description = "Read GDPR data for an individual."
    )
    public String gdprRead(
        @McpToolParam(description = "The individual ID") String individualId,
        @McpToolParam(description = "Request type (e.g., 'PORTABILITY' or 'ACCESS')") String requestType
    ) {
        try {
            String path = "/cdp/gdpr/read/" + ToolUtils.encodePath(individualId) + "/" + ToolUtils.encodePath(requestType);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_gdpr_bulk_read",
        description = "Bulk read GDPR data for multiple individuals."
    )
    public String gdprBulkRead(
        @McpToolParam(description = "Individual ID and request IDs (format: id1:type,id2:type)") String individualIdRequestIds
    ) {
        try {
            String path = "/cdp/gdpr/bulkRead/" + ToolUtils.encodePath(individualIdRequestIds);
            Map result = client.get(path, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    @McpTool(
        name = "d360_gdpr_request",
        description = "Submit a GDPR request (delete/portability)."
    )
    public String gdprRequest(
        @McpToolParam(description = "GDPR request body as JSON string") String body
    ) {
        try {
            Map<String, Object> bodyMap = ToolUtils.parseJson(body, Map.class, "body");
            Map result = client.put("/cdp/gdpr", bodyMap, Map.class);
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
