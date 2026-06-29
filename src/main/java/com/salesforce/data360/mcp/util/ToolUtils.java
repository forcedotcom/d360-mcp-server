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
package com.salesforce.data360.mcp.util;

import com.salesforce.data360.mcp.model.common.ApiException;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ToolUtils {

    private ToolUtils() {}

    /**
     * Build a path with URL-encoded query parameters from a map.
     * Null or empty map returns basePath unchanged.
     */
    public static String buildPath(String basePath, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return basePath;
        }
        List<String> queryParams = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) continue;
            String encoded = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            queryParams.add(entry.getKey() + "=" + encoded);
        }
        if (queryParams.isEmpty()) {
            return basePath;
        }
        return basePath + "?" + String.join("&", queryParams);
    }

    /**
     * Build a path with a single URL-encoded dataspace query parameter.
     * Null or blank dataspace returns basePath unchanged.
     */
    public static String buildPath(String basePath, String dataspace) {
        if (dataspace == null || dataspace.isBlank()) {
            return basePath;
        }
        String encoded = URLEncoder.encode(dataspace, StandardCharsets.UTF_8);
        return basePath + "?dataspace=" + encoded;
    }

    /**
     * URL-encode a path segment (handles slashes, spaces, special characters).
     * Use for any user-supplied value inserted into a URL path.
     */
    public static String encodePath(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    /**
     * Build a structured JSON error response string. For {@link ApiException}
     * HTTP errors (non-zero status code) the payload includes {@code statusCode}
     * and {@code path}; otherwise only {@code error} is emitted.
     */
    public static String errorResponse(Throwable e) {
        if (e instanceof ApiException api && api.getStatusCode() != 0) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("error", api.getMessage());
            payload.put("statusCode", api.getStatusCode());
            payload.put("path", api.getRequestPath());
            return JsonUtil.toJson(payload);
        }
        return JsonUtil.toJson(Map.of("error", e.getMessage()));
    }

    public static <T> T parseJson(String json, Class<T> clazz, String inputName) {
        try {
            return JsonUtil.fromJson(json, clazz);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid JSON " + inputName + ": " + rootCauseMessage(e), e);
        }
    }

    private static String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : throwable.getMessage();
    }
}
