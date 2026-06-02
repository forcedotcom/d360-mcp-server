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
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Snowflake-specific data stream creation tool.
 * Builds the complex payload with advancedAttributes (database/schema/object),
 * sourceFields, mappings, and dataLakeObjectInfo required for Snowflake connectors.
 */
@Component
public class SnowflakeDataStreamTools extends AbstractConnectorDataStreamTools {

    public SnowflakeDataStreamTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/data-streams", verb = "POST")
    @McpTool(
        name = "d360_datastream_create_snowflake",
        description = "Create a Snowflake-backed data stream using an existing Snowflake connection. "
            + "Automatically builds sourceFields, mappings, advancedAttributes (database/schema/object), "
            + "and dataLakeObjectInfo from the provided fields. Use label for Snowflake source column name "
            + "and name for the target DLO field name."
    )
    public String createSnowflakeStream(
        @McpToolParam(description = "Unique developer name for the data stream") String streamName,
        @McpToolParam(description = "Display label for the data stream") String label,
        @McpToolParam(description = "Existing Snowflake connection developer name") String connectionName,
        @McpToolParam(description = "Snowflake database name, e.g. ANALYTICS") String database,
        @McpToolParam(description = "Snowflake schema name, e.g. PUBLIC") String schema,
        @McpToolParam(description = "Snowflake source object/table name, e.g. CUSTOMER_ORDERS") String objectName,
        @McpToolParam(description = "Dataspace name for the target DLO") String dataSpaceName,
        @McpToolParam(description = "Target DLO developer name including __dll suffix") String dloName,
        @McpToolParam(description = "Target DLO label. Defaults to streamName if omitted", required = false) String dloLabel,
        @McpToolParam(description = "DLO category: Profile, Engagement, or Other. Defaults to Profile", required = false) String category,
        @McpToolParam(description = "Event date field name. Required for Engagement category", required = false) String eventDateTimeFieldName,
        @McpToolParam(description = "Refresh mode: TOTAL_REPLACE or UPSERT. Defaults to TOTAL_REPLACE", required = false) String refreshMode,
        @McpToolParam(description = "Data access mode. Defaults to Direct_Access", required = false) String dataAccessMode,
        @McpToolParam(description = "Field definitions: list of objects with name, label, dataType, isPrimaryKey. " +
            "label = Snowflake source column name, name = target DLO field name") List<Map<String, Object>> fields
    ) {
        Map<String, Object> body;
        try {
            body = buildRequestBody(streamName, label, connectionName, database, schema, objectName,
                dataSpaceName, dloName, dloLabel, category, eventDateTimeFieldName, refreshMode,
                dataAccessMode, fields);
        } catch (IllegalArgumentException e) {
            return JsonUtil.toJson(Map.of("error", e.getMessage()));
        }
        return createDataStream(body, null);
    }

    private static Map<String, Object> buildRequestBody(String streamName, String label,
                                                        String connectionName, String database,
                                                        String schema, String objectName,
                                                        String dataSpaceName, String dloName,
                                                        String dloLabel, String category,
                                                        String eventDateTimeFieldName, String refreshMode,
                                                        String dataAccessMode,
                                                        List<Map<String, Object>> fields) {
        require(streamName, "streamName");
        require(label, "label");
        require(connectionName, "connectionName");
        require(database, "database");
        require(schema, "schema");
        require(objectName, "objectName");
        require(dataSpaceName, "dataSpaceName");
        require(dloName, "dloName");
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("At least one field is required");
        }

        List<Map<String, Object>> normalizedFields = normalizeFields(fields);
        String normalizedCategory = normalizeCategory(category);
        if ("Engagement".equals(normalizedCategory) && isBlank(eventDateTimeFieldName)) {
            throw new IllegalArgumentException("eventDateTimeFieldName is required for Engagement category");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", streamName);
        body.put("label", label);
        body.put("datastreamType", "EXTERNAL");
        body.put("connectorInfo", Map.of(
            "connectorType", "DataConnector",
            "connectorDetails", Map.of("name", connectionName)
        ));
        body.put("dataLakeObjectInfo", buildDataLakeObjectInfo(
            dloName, isBlank(dloLabel) ? streamName : dloLabel,
            dataSpaceName, normalizedCategory, eventDateTimeFieldName, normalizedFields
        ));
        body.put("mappings", buildMappings(normalizedFields));
        body.put("refreshConfig", Map.of(
            "refreshMode", isBlank(refreshMode) ? "TOTAL_REPLACE" : refreshMode.trim()
        ));
        body.put("sourceFields", buildSourceFields(normalizedFields));
        body.put("advancedAttributes", Map.of(
            "schema", schema,
            "database", database,
            "object", objectName
        ));
        body.put("dataAccessMode", isBlank(dataAccessMode) ? "Direct_Access" : dataAccessMode.trim());
        return body;
    }

    private static Map<String, Object> buildDataLakeObjectInfo(String dloName, String dloLabel,
                                                                String dataSpaceName, String category,
                                                                String eventDateTimeFieldName,
                                                                List<Map<String, Object>> fields) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("dataspaceInfo", List.of(Map.of("name", dataSpaceName)));
        info.put("category", category);
        info.put("label", dloLabel);
        info.put("name", dloName);
        if (!isBlank(eventDateTimeFieldName)) {
            info.put("eventDateTimeFieldName", eventDateTimeFieldName);
        }
        info.put("dataLakeFieldInputRepresentations", fields);
        return info;
    }

    private static List<Map<String, Object>> buildMappings(List<Map<String, Object>> fields) {
        List<Map<String, Object>> mappings = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            mappings.add(Map.of(
                "sourceFieldLabel", field.get("label"),
                "targetFieldName", field.get("name")
            ));
        }
        return mappings;
    }

    private static List<Map<String, Object>> buildSourceFields(List<Map<String, Object>> fields) {
        List<Map<String, Object>> sourceFields = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            sourceFields.add(Map.of(
                "name", field.get("label"),
                "dataType", field.get("dataType")
            ));
        }
        return sourceFields;
    }

    private static List<Map<String, Object>> normalizeFields(List<Map<String, Object>> fields) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        boolean hasPrimaryKey = false;
        for (Map<String, Object> field : fields) {
            String name = str(field.get("name"));
            String fieldLabel = str(field.get("label"));
            String dataType = str(field.get("dataType"));
            boolean isPrimaryKey = bool(field.get("isPrimaryKey"));
            require(name, "field.name");
            require(fieldLabel, "field.label");
            require(dataType, "field.dataType");
            if (isPrimaryKey) hasPrimaryKey = true;
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("name", name);
            n.put("label", fieldLabel);
            n.put("dataType", normalizeDataType(dataType));
            n.put("isPrimaryKey", isPrimaryKey);
            normalized.add(n);
        }
        if (!hasPrimaryKey) throw new IllegalArgumentException("At least one field must be isPrimaryKey=true");
        return normalized;
    }

    private static String normalizeCategory(String category) {
        if (isBlank(category)) return "Profile";
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "PROFILE" -> "Profile";
            case "ENGAGEMENT" -> "Engagement";
            case "OTHER" -> "Other";
            default -> throw new IllegalArgumentException("Use Profile, Engagement, or Other");
        };
    }

    private static String normalizeDataType(String dt) {
        return switch (dt.trim().toUpperCase(Locale.ROOT)) {
            case "BOOLEAN" -> "Boolean"; case "DATE" -> "Date"; case "DATEONLY" -> "DateOnly";
            case "DATETIME" -> "DateTime"; case "EMAIL" -> "Email"; case "NUMBER" -> "Number";
            case "PERCENT" -> "Percent"; case "PHONE" -> "Phone"; case "TEXT" -> "Text";
            case "URL" -> "Url";
            default -> throw new IllegalArgumentException("Unsupported dataType: " + dt);
        };
    }

    private static void require(String v, String name) { if (isBlank(v)) throw new IllegalArgumentException(name + " is required"); }
    private static String str(Object v) { return v == null ? null : String.valueOf(v); }
    private static boolean bool(Object v) { return v instanceof Boolean b ? b : v != null && Boolean.parseBoolean(String.valueOf(v)); }
    private static boolean isBlank(String v) { return v == null || v.isBlank(); }
}
