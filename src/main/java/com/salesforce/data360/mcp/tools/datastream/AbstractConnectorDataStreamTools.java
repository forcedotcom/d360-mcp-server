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
package com.salesforce.data360.mcp.tools.datastream;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.datastream.DataStreamCreateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for data stream tools. Holds the shared {@link Data360Client} and
 * provides the common create-data-stream API call used by both the generic
 * tool ({@link DataStreamTools}) and connector-specific tools
 * (e.g. {@link SalesforceCrmDataStreamTools}).
 */
public abstract class AbstractConnectorDataStreamTools {

    protected final Data360Client client;

    protected AbstractConnectorDataStreamTools(Data360Client client) {
        this.client = client;
    }

    /**
     * Post a {@link DataStreamCreateRequest} to the Data 360 data-streams endpoint.
     *
     * @param request   the fully-built create request
     * @param dataspace optional dataspace query-param override
     * @return JSON response string (success or structured error)
     */
    protected String createDataStream(DataStreamCreateRequest request, String dataspace) {
        return createDataStream(JsonUtil.toMap(request), dataspace);
    }

    /**
     * Post a pre-built request body to the Data 360 data-streams endpoint.
     * Used by connectors whose payload shape diverges from {@link DataStreamCreateRequest}.
     *
     * @param body      the fully-built request body
     * @param dataspace optional dataspace query-param override; pass {@code null} to omit
     * @return JSON response string (success or structured error)
     */
    protected String createDataStream(Map<String, Object> body, String dataspace) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);

            String path = ToolUtils.buildPath("/ssot/data-streams", params);
            Map result = client.post(path, body, Map.class);
            return JsonUtil.toJson(result);
        } catch (ApiException e) {
            return ToolUtils.errorResponse(e);
        }
    }
}
