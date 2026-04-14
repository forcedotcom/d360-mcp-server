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
import com.salesforce.data360.mcp.model.request.eventing.EventBatchPublishRequest;
import com.salesforce.data360.mcp.model.request.eventing.EventPublishRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data 360 Eventing Tools - streaming event ingestion.
 * Maps to /a360/event and /a360/events endpoints.
 */
@Component
public class EventingTools {

    private final Data360Client client;

    public EventingTools(Data360Client client) {
        this.client = client;
    }

    /**
     * Publish a single streaming event to Data 360 via the Ingestion API.
     * Use this to send a real-time event (e.g. page view, click, purchase) into Data 360 for immediate ingestion.
     */
    @McpTool(
        name = "d360_event_publish",
        description = "Publish a single streaming event to Data 360 via the Ingestion API."
    )
    public String publishEvent(EventPublishRequest request) {
        try {
            Map<String, Object> body = JsonUtil.toMap(request);
            body.put("payload", ToolUtils.parseJson(request.getPayload(), Map.class, "payload"));

            String path = "/a360/event";
            Map result = client.post(path, body, Map.class);
            if (result == null) {
                return JsonUtil.toJson(Map.of("success", true));
            }
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Publish a batch of streaming events to Data 360.
     * Use this when you have multiple events to ingest at once — more efficient than individual d360_event_publish calls.
     */
    @McpTool(
        name = "d360_event_publish_batch",
        description = "Publish a batch of streaming events to Data 360."
    )
    public String publishBatchEvents(EventBatchPublishRequest request) {
        try {
            List<Map> eventsList = ToolUtils.parseJson(request.getEvents(), List.class, "events");
            List<Map> schemasList = ToolUtils.parseJson(request.getSchemas(), List.class, "schemas");

            Map<String, Object> body = new HashMap<>();
            body.put("events", eventsList);
            body.put("schemas", schemasList);
            body.put("count", eventsList.size());
            body.put("creationDateTime", Instant.now().toString());
            if (request.getSchemaVersion() != null) {
                body.put("schemaVersion", request.getSchemaVersion());
            }

            String path = "/a360/events";
            Map result = client.post(path, body, Map.class);
            if (result == null) {
                return JsonUtil.toJson(Map.of("success", true, "count", eventsList.size()));
            }
            return JsonUtil.toJson(result);
        } catch (IllegalArgumentException | ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }

}
