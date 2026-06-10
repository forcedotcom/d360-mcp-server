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
import com.salesforce.data360.mcp.model.request.mapping.FieldMappingInput;
import com.salesforce.data360.mcp.model.request.mapping.MappingCreateRequest;
import com.salesforce.data360.mcp.runtime.ApiEndpoint;
import com.salesforce.data360.mcp.service.MappingLookupService;
import com.salesforce.data360.mcp.service.MappingLookupService.DmoMapping;
import com.salesforce.data360.mcp.service.MappingLookupService.FieldMapping;
import com.salesforce.data360.mcp.service.MappingLookupService.SObjectDmoMappings;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Standard Mapping Tools - Preview and create standard (pre-defined) DLO-to-DMO field mappings.
 * Uses 500+ mapping definitions bundled on the classpath and Data360Client to create mappings via the API.
 */
@Component
public class StandardMappingTools {

    private static final Logger log = LoggerFactory.getLogger(StandardMappingTools.class);

    private final MappingLookupService mappingLookupService;
    private final Data360Client client;

    public StandardMappingTools(MappingLookupService mappingLookupService, Data360Client client) {
        this.mappingLookupService = mappingLookupService;
        this.client = client;
    }

    /**
     * Preview standard DLO-to-DMO field mappings for a given source object.
     * Checks 500+ standard mapping definitions bundled with the server.
     */
    @McpTool(
        name = "d360_standard_mapping_preview",
        description = "Preview standard DLO-to-DMO field mappings for a source object. "
            + "Checks 500+ standard mapping definitions — if a known mapping exists for the source object, "
            + "the field mappings for all target DMOs are returned for review. "
            + "Call this BEFORE d360_standard_mapping_create so the user can verify the recommended mappings. "
    )
    public String standardMappingPreview(
        @McpToolParam(description = "Source DLO or SObject name (e.g., 'Account', 'Contact', or 'Account_00D000000000000__dll')") String sourceObjectName,
        @McpToolParam(description = "Target DMO name to filter results (e.g., 'AccountDmo'). If omitted, returns mappings for ALL target DMOs.", required = false) String targetDmoName
    ) {
        try {
            if (sourceObjectName != null) {
                sourceObjectName = sourceObjectName.trim();
            }

            SObjectDmoMappings mapping = mappingLookupService.lookup(sourceObjectName);
            if (mapping == null) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("found", false);
                result.put("sourceObjectName", sourceObjectName);
                result.put("availableStandardMappings", mappingLookupService.size());
                result.put("message", "No standard mapping found for '" + sourceObjectName
                    + "'. Create custom field mappings manually using d360_dlo_get and d360_dmo_get to retrieve field definitions.");
                return JsonUtil.toJson(result);
            }

            return buildPreviewResponse(mapping, targetDmoName);
        } catch (Exception e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Create all standard DLO-to-DMO mappings for a source object in one call.
     * Looks up the standard mapping definitions and calls the Data 360 API for each target DMO.
     */
    @ApiEndpoint(path = "/ssot/data-model-object-mappings", verb = "POST")
    @McpTool(
        name = "d360_standard_mapping_create",
        description = "Create all standard DLO-to-DMO mappings for a source object in one call. "
            + "Looks up the standard mapping definitions (500+) for the source object and creates "
            + "ALL target DMO mappings via the API. Call d360_standard_mapping_preview first so the user "
            + "can review the mappings before creating them. Use excludeDmos to skip specific DMOs. "
            + "Per-DMO failures are reported individually in the 'results' array without aborting the batch; "
            + "for any DMO reporting an error, fall back to d360_dmo_mapping_create using the preview output as a guide."
    )
    public String standardMappingCreate(
        @McpToolParam(description = "Source DLO developer name (e.g., 'Account_00D000000000000__dll' or 'Account_Home__dll'). Must be the actual DLO name as it appears in Data 360.") String sourceObjectName,
        @McpToolParam(description = "Comma-separated list of DMO names to skip (e.g., 'FinancialCustomerDmo,ContactPointPhoneDmo'). If omitted, creates mappings for ALL target DMOs.", required = false) String excludeDmos,
        @McpToolParam(description = "Optional dataspace name", required = false) String dataspace
    ) {
        try {
            if (sourceObjectName != null) {
                sourceObjectName = sourceObjectName.trim();
            }

            SObjectDmoMappings mapping = mappingLookupService.lookup(sourceObjectName);
            if (mapping == null) {
                return JsonUtil.toJson(Map.of(
                    "error", "No standard mapping found for '" + sourceObjectName + "'",
                    "availableStandardMappings", mappingLookupService.size()
                ));
            }

            Set<String> exclusions = parseExclusions(excludeDmos);
            List<DmoMapping> dmoMappings = mapping.dmoMappings();

            List<DmoMapping> toCreate = new ArrayList<>();
            List<String> skipped = new ArrayList<>();
            for (DmoMapping dm : dmoMappings) {
                if (exclusions.contains(dm.dmoName().toLowerCase())) {
                    skipped.add(dm.dmoName());
                } else {
                    toCreate.add(dm);
                }
            }

            Set<String> dloFieldNames;
            try {
                dloFieldNames = fetchDloFieldNames(sourceObjectName, dataspace);
            } catch (ApiException e) {
                return ToolUtils.errorResponse(e);
            }

            if (dloFieldNames.isEmpty()) {
                return JsonUtil.toJson(Map.of(
                    "error", "DLO '" + sourceObjectName + "' returned no fields. Verify the DLO name"
                        + (dataspace != null ? " and dataspace '" + dataspace + "'" : "") + "."
                ));
            }

            Map<String, Object> params = new HashMap<>();
            if (dataspace != null) params.put("dataspace", dataspace);
            String path = ToolUtils.buildPath("/ssot/data-model-object-mappings", params);

            List<Map<String, Object>> results = new ArrayList<>();
            int succeeded = 0;
            int failed = 0;
            int skippedNoFields = 0;

            for (DmoMapping dm : toCreate) {
                List<FieldMapping> matched = new ArrayList<>();
                List<String> skippedFields = new ArrayList<>();
                for (FieldMapping fm : dm.fieldMappings()) {
                    if (dloFieldNames.contains(fm.sourceField().toLowerCase())) {
                        matched.add(fm);
                    } else {
                        skippedFields.add(fm.sourceField());
                    }
                }

                if (matched.isEmpty()) {
                    results.add(resultEntry(dm.dmoName(), "skipped_no_matching_fields", skippedFields));
                    skippedNoFields++;
                    log.info("Skipping mapping for {} → {}: none of the {} template source fields exist on the DLO",
                        sourceObjectName, dm.dmoName(), skippedFields.size());
                    continue;
                }

                MappingCreateRequest request = buildCreateRequest(sourceObjectName, dm.dmoName(), matched);
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> apiResult = client.post(path, JsonUtil.toMap(request), Map.class);
                    Map<String, Object> entry = resultEntry(dm.dmoName(), "success", skippedFields);
                    entry.put("response", apiResult);
                    results.add(entry);
                    succeeded++;
                } catch (Exception e) {
                    Map<String, Object> entry = resultEntry(dm.dmoName(), "error", skippedFields);
                    entry.put("error", e.getMessage());
                    results.add(entry);
                    failed++;
                    log.warn("Failed to create mapping for {} → {}: {}", sourceObjectName, dm.dmoName(), e.getMessage());
                }
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sourceObjectName", sourceObjectName);
            response.put("totalDmos", dmoMappings.size());
            response.put("attempted", toCreate.size());
            response.put("succeeded", succeeded);
            response.put("failed", failed);
            response.put("skippedNoMatchingFields", skippedNoFields);
            response.put("dloFieldCount", dloFieldNames.size());
            if (!skipped.isEmpty()) {
                response.put("skipped", skipped);
            }
            response.put("results", results);
            return JsonUtil.toJson(response);
        } catch (Exception e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private String buildPreviewResponse(SObjectDmoMappings mapping, String targetDmoName) {
        List<DmoMapping> dmoMappings = mapping.dmoMappings();
        if (targetDmoName != null && !targetDmoName.isEmpty()) {
            dmoMappings = dmoMappings.stream()
                .filter(dm -> dm.dmoName().equalsIgnoreCase(targetDmoName))
                .toList();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", true);
        result.put("sourceObjectName", mapping.sourceObjectName());
        result.put("targetDmoCount", dmoMappings.size());

        String dmoSummary = dmoMappings.stream()
            .map(dm -> dm.dmoName() + ": " + dm.fieldMappings().size() + " fields")
            .collect(Collectors.joining(", "));
        result.put("summary", "Standard mappings available: " + mapping.sourceObjectName()
            + " → " + dmoMappings.size() + " DMOs (" + dmoSummary + ")");

        List<Map<String, Object>> targets = new ArrayList<>();
        for (DmoMapping dm : dmoMappings) {
            Map<String, Object> target = new LinkedHashMap<>();
            target.put("targetDmoName", dm.dmoName());
            target.put("fieldMappingCount", dm.fieldMappings().size());
            target.put("fieldMappings", dm.fieldMappings().stream()
                .map(fm -> {
                    Map<String, Object> fmMap = new LinkedHashMap<>();
                    fmMap.put("sourceField", fm.sourceField());
                    fmMap.put("targetField", fm.targetField());
                    if (fm.isFilterApplied()) {
                        fmMap.put("isFilterApplied", true);
                        if (fm.filterOperationType() != null) {
                            fmMap.put("filterOperationType", fm.filterOperationType());
                        }
                    }
                    return fmMap;
                })
                .toList());
            targets.add(target);
        }
        result.put("dmoMappings", targets);
        result.put("usage", "Review the mappings above. To create ALL of them in one step, "
            + "call d360_standard_mapping_create with the actual DLO developer name (e.g., 'Account_00D...__dll'). "
            + "You can optionally pass excludeDmos to skip specific DMOs.");

        return JsonUtil.toJson(result);
    }

    private static Map<String, Object> resultEntry(String dmoName, String status, List<String> skippedFields) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("targetDmo", dmoName);
        entry.put("status", status);
        if (!skippedFields.isEmpty()) {
            entry.put("skippedFields", skippedFields);
        }
        return entry;
    }

    private MappingCreateRequest buildCreateRequest(String sourceObjectName, String dmoName, List<FieldMapping> fields) {
        List<FieldMappingInput> fieldMappings = fields.stream()
            .map(fm -> {
                FieldMappingInput input = new FieldMappingInput();
                input.setSourceFieldDeveloperName(fm.sourceField());
                input.setTargetFieldDeveloperName(fm.targetField());
                return input;
            })
            .toList();

        MappingCreateRequest request = new MappingCreateRequest();
        request.setSourceEntityDeveloperName(sourceObjectName);
        request.setTargetEntityDeveloperName(dmoName);
        request.setFieldMapping(fieldMappings);
        return request;
    }

    private Set<String> fetchDloFieldNames(String dloName, String dataspace) {
        String path = ToolUtils.buildPath(
            "/ssot/data-lake-objects/" + ToolUtils.encodePath(dloName), dataspace);
        @SuppressWarnings("unchecked")
        Map<String, Object> dlo = client.get(path, Map.class);
        return extractFieldNames(dlo);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> extractFieldNames(Map<String, Object> dlo) {
        if (dlo == null) return Set.of();

        // Server wraps single-GET responses in {"dataLakeObjects": [ { ... } ]}.
        Map<String, Object> inner = dlo;
        if (dlo.get("dataLakeObjects") instanceof List<?> list
            && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            inner = (Map<String, Object>) first;
        }

        Object rawFields = inner.get("dataLakeFieldInfoRepresentation");
        if (!(rawFields instanceof List<?> fields)) return Set.of();
        Set<String> names = new HashSet<>();
        for (Object item : fields) {
            if (item instanceof Map<?, ?> m) {
                Object name = m.get("name");
                if (name != null) {
                    names.add(name.toString().toLowerCase());
                }
            }
        }
        return names;
    }

    private static Set<String> parseExclusions(String excludeDmos) {
        if (excludeDmos == null || excludeDmos.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(excludeDmos.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }
}
