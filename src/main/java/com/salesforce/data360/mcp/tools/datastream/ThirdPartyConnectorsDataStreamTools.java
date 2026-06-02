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
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Data stream creation tool for third-party connectors
 * such as Airtable, HubSpot, Marketo, Google Ads, and others.
 * Builds the full payload (sourceFields, mappings, dataLakeObjectInfo, advancedAttributes)
 * from simple field definitions.
 */
@Component
public class ThirdPartyConnectorsDataStreamTools extends AbstractConnectorDataStreamTools {

    public ThirdPartyConnectorsDataStreamTools(Data360Client client) {
        super(client);
    }

    @ApiEndpoint(path = "/ssot/data-streams", verb = "POST")
    @McpTool(
        name = "d360_datastream_create_third_party_connectors",
        description = "Create a data stream for a third-party connector "
            + "(e.g. Airtable, HubSpot, Marketo, Google Ads, or any other connector). "
            + "PREREQUISITE: Call 'd360_connection_list' to find the connection, "
            + "then 'd360_connection_objects_list' and 'd360_connection_object_fields_describe' to discover available objects and their fields. "
            + "Automatically builds sourceFields, mappings, dataLakeObjectInfo, and advancedAttributes from the provided fields. "
            + "The 'datasource' param follows the pattern '{ConnectorType}_{connectionName}' "
            + "(e.g. 'Airtable_airtable_dev_conn'). "
            + "datastreamType is set to CONNECTORSFRAMEWORK and connectorType to DataConnector automatically."
    )
    public String createThirdPartyConnectorsDataStream(
        @McpToolParam(description = "Connection developer name from d360_connection_list") String connectionName,
        @McpToolParam(description = "Datasource identifier, pattern: {ConnectorType}_{connectionName} (e.g. Airtable_my_conn)") String datasource,
        @McpToolParam(description = "Source object name from d360_connection_objects_list (e.g. Tasks, People)") String objectName,
        @McpToolParam(description = "Field definitions: list of objects with name, dataType (Text/Number/Date/DateTime/Boolean/Email/Phone/Url/Percent), "
            + "isPrimaryKey (boolean), and optional format (e.g. 'MM/dd/yyyy HH:mm:ss.SSS' for DateTime, 'MM/dd/yyyy' for Date). "
            + "At least one field must have isPrimaryKey=true.") List<Map<String, Object>> fields,
        @McpToolParam(description = "Override data stream name. Defaults to '{objectName}_{connectionName}'", required = false) String streamName,
        @McpToolParam(description = "Override display label. Defaults to name with underscores replaced by spaces", required = false) String label,
        @McpToolParam(description = "Override DLO developer name (must end with __dll). Defaults to '{objectName}_{connectionName}__dll'", required = false) String dloName,
        @McpToolParam(description = "Override DLO display label. Defaults to same as data stream label", required = false) String dloLabel,
        @McpToolParam(description = "DLO category: Profile, Engagement, or Other. Defaults to Profile", required = false) String category,
        @McpToolParam(description = "Dataspace name. Defaults to 'default'", required = false) String dataSpaceName,
        @McpToolParam(description = "Refresh mode: TOTAL_REPLACE, UPSERT, or INCREMENTAL. Defaults to TOTAL_REPLACE. "
            + "When INCREMENTAL, incrementalColumn is required.", required = false) String refreshMode,
        @McpToolParam(description = "Column name used for incremental refresh. Required when refreshMode is INCREMENTAL", required = false) String incrementalColumn,
        @McpToolParam(description = "Event date field name. Required when category is Engagement", required = false) String eventDateTimeFieldName,
        @McpToolParam(description = "Whether to fetch data immediately after creation. Defaults to true", required = false) Boolean fetchImmediately,
        @McpToolParam(description = "Refresh frequency type: NONE, HOURLY, DAILY, WEEKLY, or MONTHLY. Defaults to NONE", required = false) String frequencyType,
        @McpToolParam(description = "Hours to run refresh (0-23). Required for DAILY, WEEKLY, and MONTHLY (e.g. [13] for 1 PM)", required = false) List<Integer> hours,
        @McpToolParam(description = "Day of week for WEEKLY frequency: SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY", required = false) String refreshDayOfWeek,
        @McpToolParam(description = "Day of month (1-31) for MONTHLY frequency (e.g. [6] for the 6th)", required = false) List<Integer> refreshDayOfMonth
    ) {
        try {
            require(connectionName, "connectionName");
            require(datasource, "datasource");
            require(objectName, "objectName");
            if (fields == null || fields.isEmpty()) {
                throw new IllegalArgumentException("At least one field is required");
            }

            List<Map<String, Object>> normalizedFields = normalizeFields(fields);
            String resolvedCategory = normalizeCategory(category);
            if ("Engagement".equals(resolvedCategory) && isBlank(eventDateTimeFieldName)) {
                throw new IllegalArgumentException("eventDateTimeFieldName is required for Engagement category");
            }

            String resolvedName = isBlank(streamName)
                ? objectName + "_" + connectionName : streamName;
            String resolvedLabel = isBlank(label)
                ? resolvedName.replace('_', ' ') : label;
            String resolvedDloName = isBlank(dloName)
                ? resolvedName + "__dll" : dloName;
            String resolvedDloLabel = isBlank(dloLabel)
                ? resolvedLabel : dloLabel;
            String resolvedDataSpace = isBlank(dataSpaceName) ? "default" : dataSpaceName;
            String resolvedRefreshMode = isBlank(refreshMode)
                ? "TOTAL_REPLACE" : refreshMode.trim().toUpperCase(Locale.ROOT);
            if ("INCREMENTAL".equals(resolvedRefreshMode) && isBlank(incrementalColumn)) {
                throw new IllegalArgumentException("incrementalColumn is required when refreshMode is INCREMENTAL");
            }
            boolean resolvedFetchImmediately = fetchImmediately == null || fetchImmediately;

            Map<String, Object> advancedAttributes = new LinkedHashMap<>();
            advancedAttributes.put("objectName", objectName);
            if (!isBlank(incrementalColumn)) {
                advancedAttributes.put("incrementalColumn", incrementalColumn);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("advancedAttributes", advancedAttributes);
            body.put("datasource", datasource);
            body.put("capabilities", Map.of());
            body.put("connectorInfo", Map.of(
                "connectorDetails", Map.of("name", connectionName),
                "connectorType", "DataConnector"
            ));
            body.put("dataAccessMode", "INGEST");
            body.put("dataLakeObjectInfo", buildDataLakeObjectInfo(
                resolvedDloName, resolvedDloLabel, resolvedDataSpace,
                resolvedCategory, eventDateTimeFieldName, normalizedFields));
            body.put("datastreamType", "CONNECTORSFRAMEWORK");
            body.put("label", resolvedLabel);
            body.put("mappings", buildMappings(normalizedFields));
            body.put("name", resolvedName);
            body.put("refreshConfig", buildRefreshConfig(resolvedRefreshMode, resolvedFetchImmediately,
                frequencyType, hours, refreshDayOfWeek, refreshDayOfMonth));
            body.put("sourceFields", buildSourceFields(fields, normalizedFields));

            return createDataStream(body, resolvedDataSpace);
        } catch (RuntimeException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private static Map<String, Object> buildDataLakeObjectInfo(
            String dloName, String dloLabel, String dataSpaceName,
            String category, String eventDateTimeFieldName,
            List<Map<String, Object>> normalizedFields) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("capabilities", Map.of());
        info.put("category", category);
        info.put("dataLakeFieldInputRepresentations", normalizedFields);
        info.put("dataspaceInfo", List.of(Map.of("name", dataSpaceName)));
        info.put("label", dloLabel);
        info.put("name", dloName);
        if (!isBlank(eventDateTimeFieldName)) {
            info.put("eventDateTimeFieldName", eventDateTimeFieldName);
        }
        return info;
    }

    private static List<Map<String, Object>> buildMappings(List<Map<String, Object>> normalizedFields) {
        List<Map<String, Object>> mappings = new ArrayList<>();
        for (Map<String, Object> field : normalizedFields) {
            mappings.add(Map.of(
                "sourceFieldLabel", field.get("name"),
                "targetFieldName", field.get("name")
            ));
        }
        return mappings;
    }

    private static Map<String, Object> buildRefreshConfig(String refreshMode, boolean fetchImmediately,
            String frequencyType, List<Integer> hours, String refreshDayOfWeek, List<Integer> refreshDayOfMonth) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("frequency", buildFrequency(frequencyType, hours, refreshDayOfWeek, refreshDayOfMonth));
        config.put("refreshMode", refreshMode);
        config.put("fetchImmediately", fetchImmediately);
        return config;
    }

    static Map<String, Object> buildFrequency(String frequencyType, List<Integer> hours,
            String refreshDayOfWeek, List<Integer> refreshDayOfMonth) {
        String resolved = isBlank(frequencyType) ? "NONE" : frequencyType.trim().toUpperCase(Locale.ROOT);
        Map<String, Object> frequency = new LinkedHashMap<>();
        switch (resolved) {
            case "NONE" -> frequency.put("frequencyType", "NONE");
            case "HOURLY" -> frequency.put("frequencyType", "HOURLY");
            case "DAILY" -> {
                validateHours(hours);
                frequency.put("frequencyType", "DAILY");
                frequency.put("hours", hours);
            }
            case "WEEKLY" -> {
                validateHours(hours);
                if (isBlank(refreshDayOfWeek)) {
                    throw new IllegalArgumentException("refreshDayOfWeek is required for WEEKLY frequency");
                }
                frequency.put("frequencyType", "WEEKLY");
                frequency.put("hours", hours);
                frequency.put("refreshDayOfWeek", refreshDayOfWeek.trim().toUpperCase(Locale.ROOT));
            }
            case "MONTHLY" -> {
                validateHours(hours);
                if (refreshDayOfMonth == null || refreshDayOfMonth.isEmpty()) {
                    throw new IllegalArgumentException("refreshDayOfMonth is required for MONTHLY frequency");
                }
                frequency.put("frequencyType", "MONTHLY");
                frequency.put("hours", hours);
                frequency.put("refreshDayOfMonth", refreshDayOfMonth);
            }
            default -> throw new IllegalArgumentException(
                "Invalid frequencyType '" + frequencyType + "'. Use NONE, HOURLY, DAILY, WEEKLY, or MONTHLY");
        }
        return frequency;
    }

    private static void validateHours(List<Integer> hours) {
        if (hours == null || hours.isEmpty()) {
            throw new IllegalArgumentException("hours is required for this frequency type");
        }
    }

    private static List<Map<String, Object>> buildSourceFields(
            List<Map<String, Object>> originalFields,
            List<Map<String, Object>> normalizedFields) {
        List<Map<String, Object>> sourceFields = new ArrayList<>();
        for (int i = 0; i < normalizedFields.size(); i++) {
            Map<String, Object> sf = new LinkedHashMap<>();
            sf.put("dataType", normalizedFields.get(i).get("dataType"));
            String format = str(originalFields.get(i).get("format"));
            if (!isBlank(format)) {
                sf.put("format", format);
            }
            sf.put("name", normalizedFields.get(i).get("name"));
            sourceFields.add(sf);
        }
        return sourceFields;
    }

    static List<Map<String, Object>> normalizeFields(List<Map<String, Object>> fields) {
        List<Map<String, Object>> normalized = new ArrayList<>();
        boolean hasPrimaryKey = false;
        for (Map<String, Object> field : fields) {
            String name = str(field.get("name"));
            String dataType = str(field.get("dataType"));
            boolean isPrimaryKey = bool(field.get("isPrimaryKey"));
            require(name, "field.name");
            require(dataType, "field.dataType");
            if (isPrimaryKey) hasPrimaryKey = true;
            String fieldLabel = isBlank(str(field.get("label"))) ? name : str(field.get("label"));
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("dataType", normalizeDataType(dataType));
            n.put("isPrimaryKey", isPrimaryKey);
            n.put("label", fieldLabel);
            n.put("name", name);
            normalized.add(n);
        }
        if (!hasPrimaryKey) {
            throw new IllegalArgumentException("At least one field must have isPrimaryKey=true");
        }
        return normalized;
    }

    private static String normalizeCategory(String category) {
        if (isBlank(category)) return "Profile";
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "PROFILE" -> "Profile";
            case "ENGAGEMENT" -> "Engagement";
            case "OTHER" -> "Other";
            default -> throw new IllegalArgumentException(
                "Invalid category '" + category + "'. Use Profile, Engagement, or Other");
        };
    }

    static String normalizeDataType(String dt) {
        return switch (dt.trim().toUpperCase(Locale.ROOT)) {
            case "BOOLEAN" -> "Boolean";
            case "DATE" -> "Date";
            case "DATETIME" -> "DateTime";
            case "EMAIL" -> "Email";
            case "NUMBER" -> "Number";
            case "FLOAT" -> "Number";
            case "PERCENT" -> "Percent";
            case "PHONE" -> "Phone";
            case "TEXT" -> "Text";
            case "VARCHAR" -> "Text";
            case "TIMESTAMP" -> "DateTime";
            case "URL" -> "Url";
            default -> throw new IllegalArgumentException("Unsupported dataType: " + dt);
        };
    }

    private static void require(String v, String name) {
        if (isBlank(v)) throw new IllegalArgumentException(name + " is required");
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static boolean bool(Object v) {
        return v instanceof Boolean b ? b : v != null && Boolean.parseBoolean(String.valueOf(v));
    }

    private static boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}