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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.Map;

public final class JsonUtil {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .build();

    private JsonUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param obj the object to serialize
     * @return JSON string representation
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Deserializes JSON string to an object of the specified type.
     *
     * @param json the JSON string
     * @param clazz the target class
     * @param <T> the type parameter
     * @return deserialized object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getName(), e);
        }
    }

    /**
     * Convert a POJO to a Map for passing to Data360Client.
     * Respects @JsonInclude(NON_NULL) on the POJO class so null fields are omitted.
     */
    public static Map<String, Object> toMap(Object pojo) {
        return MAPPER.convertValue(pojo, new TypeReference<Map<String, Object>>() {});
    }
}
