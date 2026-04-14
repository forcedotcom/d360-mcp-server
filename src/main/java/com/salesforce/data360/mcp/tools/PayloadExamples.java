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

import com.salesforce.data360.mcp.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payload Examples for Complex Data 360 Tools
 *
 * Provides concrete, working examples of request bodies for the most complex tools.
 * Use these when building automated workflows or when LLMs need reference implementations.
 *
 * Examples are loaded from /metadata/payload-examples.json at class initialization time.
 * To add or edit examples, modify that JSON resource file directly.
 */
@Component
public class PayloadExamples {

    /**
     * All payload examples keyed by tool name.
     */
    public static final Map<String, Map<String, Object>> PAYLOAD_EXAMPLES;

    static {
        try (InputStream is = PayloadExamples.class.getResourceAsStream("/metadata/payload-examples.json")) {
            if (is != null) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> loaded = JsonUtil.fromJson(
                    new String(is.readAllBytes(), StandardCharsets.UTF_8), Map.class);
                PAYLOAD_EXAMPLES = Collections.unmodifiableMap(loaded);
            } else {
                PAYLOAD_EXAMPLES = Map.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load payload-examples.json", e);
        }
    }

    // ========================================================================
    // Public API
    // ========================================================================

    /**
     * Returns the payload example for the given tool name, or null if not found.
     */
    public static Map<String, Object> getPayloadExample(String toolName) {
        return PAYLOAD_EXAMPLES.get(toolName);
    }

    /**
     * Returns a sorted list of all tool names that have payload examples.
     */
    public static List<String> listToolsWithExamples() {
        return PAYLOAD_EXAMPLES.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
    }

}
