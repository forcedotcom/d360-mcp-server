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
package com.salesforce.data360.mcp.service;

import com.salesforce.data360.mcp.model.request.mapping.FieldMappingInput;
import com.salesforce.data360.mcp.model.request.mapping.MappingCreateRequest;
import com.salesforce.data360.mcp.util.JsonUtil;
import com.salesforce.data360.mcp.util.ToolUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Business logic for Smart Tools - field matching algorithms (Jaccard similarity,
 * event date scoring) and all mapping/data stream intelligence.
 *
 * <p>Pure Java logic for field matching and event date scoring — no API calls.
 */
@Service
public class SmartService {

    private static final Pattern NAME_CLEANUP = Pattern.compile("(?i)^ssot__|(?:__c|__dlm|__dll)$");
    private static final Pattern SEPARATORS = Pattern.compile("[_\\-\\s]+");

    // Well-known system/internal fields to skip during mapping
    static final Set<String> SYSTEM_FIELDS = Set.of(
        "datasource", "datasourceobject", "internalorganization",
        "cdp_sys_sourceversion", "kq_id", "sfdcorganizationid", "isdeleted"
    );

    // Event date candidates with preference scores
    static final Map<String, Integer> EVENT_DATE_CANDIDATES = Map.ofEntries(
        Map.entry("createddate", 100),
        Map.entry("eventdate", 95),
        Map.entry("eventdatetime", 95),
        Map.entry("activitydate", 90),
        Map.entry("activitydatetime", 90),
        Map.entry("occurreddate", 90),
        Map.entry("occurredatetime", 90),
        Map.entry("startdate", 85),
        Map.entry("startdatetime", 85),
        Map.entry("starteddate", 85),
        Map.entry("interactiondate", 85),
        Map.entry("engagementdate", 85),
        Map.entry("timestamp", 80),
        Map.entry("eventtime", 80),
        Map.entry("eventcreateddate", 80),
        Map.entry("sentdate", 75),
        Map.entry("opendate", 75),
        Map.entry("clickdate", 75),
        Map.entry("bounceddate", 70),
        Map.entry("subscriptiondate", 65),
        Map.entry("closedate", 60),
        Map.entry("enddate", 55)
    );

    static final Set<String> MUTABLE_DATE_FIELDS = Set.of(
        "lastmodifieddate", "systemmodstamp", "lastvieweddate",
        "lastreferenceddate", "lastactivitydate", "lastcurequestdate", "lastcuupdatedate"
    );

    /**
     * Suggest field mappings between DLO and DMO using Jaccard similarity.
     * Returns analysis and mapping payload ready for d360_dmo_mapping_create.
     */
    public String smartMappingSuggest(
        List<Map<String, Object>> sourceFields,
        List<Map<String, Object>> targetFields,
        String sourceDloName,
        String targetDmoName,
        Double threshold,
        List<Map<String, Object>> fieldOverrides
    ) {
        try {
            double minThreshold = threshold != null ? threshold : 0.5;

            List<Map<String, Object>> matches = matchFields(sourceFields, targetFields, minThreshold);

            // Apply field overrides
            if (fieldOverrides != null && !fieldOverrides.isEmpty()) {
                Map<String, String> overrideMap = new HashMap<>();
                for (Map<String, Object> override : fieldOverrides) {
                    overrideMap.put((String) override.get("source"), (String) override.get("target"));
                }

                // Remove auto-matched entries that conflict with overrides
                matches = matches.stream()
                    .filter(m -> !overrideMap.containsKey(m.get("sourceFieldName")) &&
                                 !overrideMap.containsValue(m.get("targetFieldName")))
                    .collect(Collectors.toList());

                // Add overrides as perfect matches
                for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
                    Map<String, Object> override = new HashMap<>();
                    override.put("sourceFieldName", entry.getKey());
                    override.put("targetFieldName", entry.getValue());
                    override.put("score", 1.0);
                    override.put("sourceLabel", "(explicit override)");
                    override.put("targetLabel", "(explicit override)");
                    matches.add(override);
                }
            }

            // Build mapping payload using typed request model
            List<FieldMappingInput> fieldMappings = matches.stream()
                .map(m -> {
                    FieldMappingInput input = new FieldMappingInput();
                    input.setSourceFieldDeveloperName((String) m.get("sourceFieldName"));
                    input.setTargetFieldDeveloperName((String) m.get("targetFieldName"));
                    return input;
                })
                .collect(Collectors.toList());

            MappingCreateRequest mappingRequest = new MappingCreateRequest();
            mappingRequest.setSourceEntityDeveloperName(sourceDloName);
            mappingRequest.setTargetEntityDeveloperName(targetDmoName);
            mappingRequest.setFieldMapping(fieldMappings);
            Map<String, Object> mappingPayload = JsonUtil.toMap(mappingRequest);

            Map<String, Object> analysis = buildAnalysis(matches, sourceFields, targetFields,
                sourceDloName, targetDmoName, minThreshold);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("analysis", analysis);
            result.put("mappingPayload", mappingPayload);
            result.put("usage", "Review the 'analysis' section above. If the matches look correct, pass 'mappingPayload' directly to d360_dmo_mapping_create. Use 'fieldOverrides' parameter to manually override specific mappings if needed.");

            return JsonUtil.toJson(result);
        } catch (RuntimeException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Preview DLO-to-DMO field matches with confidence scores without creating anything.
     */
    public String previewFieldMatches(
        List<Map<String, Object>> sourceFields,
        List<Map<String, Object>> targetFields,
        String sourceDloName,
        String targetDmoName,
        Double threshold
    ) {
        try {
            double minThreshold = threshold != null ? threshold : 0.5;

            List<Map<String, Object>> matches = matchFields(sourceFields, targetFields, minThreshold);

            Map<String, Object> analysis = buildAnalysis(matches, sourceFields, targetFields,
                sourceDloName, targetDmoName, minThreshold);

            return JsonUtil.toJson(analysis);
        } catch (RuntimeException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    private Map<String, Object> buildAnalysis(
        List<Map<String, Object>> matches,
        List<Map<String, Object>> sourceFields,
        List<Map<String, Object>> targetFields,
        String sourceDloName,
        String targetDmoName,
        double threshold
    ) {
        Set<String> matchedSourceFields = matches.stream()
            .map(m -> (String) m.get("sourceFieldName"))
            .collect(Collectors.toSet());

        Set<String> matchedTargetFields = matches.stream()
            .map(m -> (String) m.get("targetFieldName"))
            .collect(Collectors.toSet());

        List<Map<String, Object>> unmatchedSource = sourceFields.stream()
            .filter(f -> !matchedSourceFields.contains(f.get("name")))
            .filter(f -> !SYSTEM_FIELDS.contains(normalize((String) f.get("name"))))
            .map(f -> Map.of(
                "name", f.getOrDefault("name", ""),
                "label", f.getOrDefault("label", ""),
                "dataType", f.getOrDefault("dataType", f.getOrDefault("datatype", ""))
            ))
            .collect(Collectors.toList());

        List<Map<String, Object>> unmatchedTarget = targetFields.stream()
            .filter(f -> !matchedTargetFields.contains(f.get("name")))
            .filter(f -> !SYSTEM_FIELDS.contains(normalize((String) f.get("name"))))
            .map(f -> Map.of(
                "name", f.getOrDefault("name", ""),
                "label", f.getOrDefault("label", ""),
                "dataType", f.getOrDefault("dataType", "")
            ))
            .collect(Collectors.toList());

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("sourceDlo", sourceDloName);
        analysis.put("targetDmo", targetDmoName);
        analysis.put("sourceFieldCount", sourceFields.size());
        analysis.put("targetFieldCount", targetFields.size());
        analysis.put("matchedFieldCount", matches.size());
        analysis.put("threshold", threshold);
        analysis.put("matches", matches.stream()
            .map(m -> Map.of(
                "source", m.get("sourceFieldName"),
                "target", m.get("targetFieldName"),
                "confidence", Math.round((Double) m.get("score") * 100) + "%",
                "sourceLabel", m.getOrDefault("sourceLabel", ""),
                "targetLabel", m.getOrDefault("targetLabel", "")
            ))
            .collect(Collectors.toList()));
        analysis.put("unmatchedSourceFields", unmatchedSource);
        analysis.put("unmatchedTargetFields", unmatchedTarget);

        return analysis;
    }

    /**
     * Smart data stream creation with auto-selected event date column.
     */
    public String smartDatastreamCreate(String bodyJson, Boolean autoSelectEventDate) {
        try {
            Map<String, Object> body = JsonUtil.fromJson(bodyJson, Map.class);
            boolean autoSelect = autoSelectEventDate != null ? autoSelectEventDate : true;

            @SuppressWarnings("unchecked")
            Map<String, Object> dloInfo = (Map<String, Object>) body.get("dataLakeObjectInfo");
            String category = dloInfo != null ? (String) dloInfo.get("category") : "";

            if (!autoSelect || !category.equalsIgnoreCase("engagement")) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("mode", "passthrough");
                result.put("message", "Category is '" + category + "' — no event date auto-selection needed. Passing through to standard data stream creation.");
                result.put("body", body);
                return JsonUtil.toJson(result);
            }

            // Get fields from body
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = dloInfo != null ?
                (List<Map<String, Object>>) dloInfo.get("dataLakeFieldInfoRepresentation") : null;

            if (fields == null || fields.isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sourceFields = (List<Map<String, Object>>) body.get("sourceFields");
                fields = sourceFields;
            }

            if (fields == null || fields.isEmpty()) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("mode", "passthrough");
                result.put("message", "No field information available in the body to auto-select event date. Passing through as-is.");
                result.put("body", body);
                return JsonUtil.toJson(result);
            }

            Map<String, Object> selection = selectEventDateColumn(fields, category);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("mode", "smart");
            result.put("category", category);
            result.put("eventDateAnalysis", selection);

            @SuppressWarnings("unchecked")
            Map<String, Object> recommended = (Map<String, Object>) selection.get("recommended");
            if (recommended != null) {
                // Inject event date column into body
                Map<String, Object> enhancedDloInfo = new HashMap<>(dloInfo);
                enhancedDloInfo.put("eventDateColumn", recommended.get("fieldName"));

                Map<String, Object> enhancedBody = new HashMap<>(body);
                enhancedBody.put("dataLakeObjectInfo", enhancedDloInfo);

                result.put("enhancedBody", enhancedBody);
                result.put("message", "Auto-selected '" + recommended.get("fieldName") +
                    "' as event date column (" + recommended.get("reason") +
                    "). Review 'enhancedBody' and pass it to d360_datastream_create if correct.");
            } else {
                result.put("body", body);
                result.put("message", selection.getOrDefault("warning", "Could not determine a suitable event date column."));
            }

            return JsonUtil.toJson(result);
        } catch (RuntimeException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    /**
     * Recommend the best event date column for a given set of fields.
     */
    public String eventDateRecommend(String fieldsJson, String category) {
        try {
            List<Map<String, Object>> fields = JsonUtil.fromJson(fieldsJson, List.class);
            String cat = category != null ? category : "Engagement";
            Map<String, Object> result = selectEventDateColumn(fields, cat);
            return JsonUtil.toJson(result);
        } catch (RuntimeException e) {
            return ToolUtils.errorResponse(e);
        }
    }

    // ============================================================
    // Field Similarity Helpers
    // ============================================================

    private static String objectToLowerCase(Object obj) {
        return obj != null ? obj.toString().toLowerCase() : "";
    }

    public static String normalize(String s) {
        if (s == null) return "";
        String r = NAME_CLEANUP.matcher(s).replaceAll("");
        r = SEPARATORS.matcher(r).replaceAll("");
        return r.toLowerCase();
    }

    /**
     * Tokenize field name: split on underscores, spaces, and camelCase.
     */
    public static List<String> tokenize(String s) {
        if (s == null) return List.of();

        String cleaned = NAME_CLEANUP.matcher(s).replaceAll("");

        List<String> tokens = new ArrayList<>();
        String[] parts = cleaned.split("[_\\s]+");

        for (String part : parts) {
            // Split camelCase: "FirstName" -> ["First", "Name"]
            String[] camelParts = part.replaceAll("([a-z])([A-Z])", "$1_$2").split("_");
            for (String cp : camelParts) {
                if (!cp.isEmpty()) {
                    tokens.add(cp.toLowerCase());
                }
            }
        }

        return tokens;
    }

    /**
     * Compute Jaccard similarity between two sets.
     */
    public static double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<String> union = new HashSet<>(a);
        union.addAll(b);

        return (double) intersection.size() / union.size();
    }

    /**
     * Compute field similarity score (0-1).
     * Uses multiple signals:
     * - Exact normalized name match (1.0)
     * - Token overlap (Jaccard similarity on name tokens)
     * - Label similarity (if labels are available)
     * - Data type compatibility bonus
     */
    public static double fieldSimilarity(Map<String, Object> sourceField, Map<String, Object> targetField) {
        String srcName = (String) sourceField.get("name");
        String tgtName = (String) targetField.get("name");

        if (srcName == null || tgtName == null) return 0.0;

        String srcNorm = normalize(srcName);
        String tgtNorm = normalize(tgtName);

        // Exact match after normalization
        if (srcNorm.equals(tgtNorm)) return 1.0;

        double score = 0.0;

        // Token-based Jaccard similarity on names
        Set<String> srcTokens = new HashSet<>(tokenize(srcName));
        Set<String> tgtTokens = new HashSet<>(tokenize(tgtName));
        if (!srcTokens.isEmpty() && !tgtTokens.isEmpty()) {
            double tokenSim = jaccardSimilarity(srcTokens, tgtTokens);
            score = Math.max(score, tokenSim);
        }

        // Label similarity
        String srcLabel = (String) sourceField.get("label");
        String tgtLabel = (String) targetField.get("label");
        if (srcLabel != null && tgtLabel != null) {
            String srcLabelNorm = normalize(srcLabel);
            String tgtLabelNorm = normalize(tgtLabel);
            if (srcLabelNorm.equals(tgtLabelNorm)) {
                score = Math.max(score, 0.95);
            } else {
                Set<String> srcLabelTokens = new HashSet<>(tokenize(srcLabel));
                Set<String> tgtLabelTokens = new HashSet<>(tokenize(tgtLabel));
                if (!srcLabelTokens.isEmpty() && !tgtLabelTokens.isEmpty()) {
                    double labelSim = jaccardSimilarity(srcLabelTokens, tgtLabelTokens) * 0.9;
                    score = Math.max(score, labelSim);
                }
            }
        }

        // Substring containment bonus
        if (srcNorm.length() >= 3 && tgtNorm.length() >= 3) {
            if (srcNorm.contains(tgtNorm) || tgtNorm.contains(srcNorm)) {
                score = Math.max(score, 0.75);
            }
        }

        // Data type compatibility bonus
        String srcType = objectToLowerCase(sourceField.getOrDefault("dataType", sourceField.get("datatype")));
        String tgtType = objectToLowerCase(targetField.getOrDefault("dataType", targetField.get("datatype")));
        if (!srcType.isEmpty() && !tgtType.isEmpty() && srcType.equals(tgtType)) {
            if (score == 0.0) {
                // No name/label match, but data types match — give a small base score
                score = 0.1;
            } else {
                // Boost existing score by 10%
                score = Math.min(1.0, score * 1.1);
            }
        }

        return score;
    }

    /**
     * Match source fields to target fields using semantic similarity.
     * Returns only matches above threshold, with no target field matched twice.
     */
    public static List<Map<String, Object>> matchFields(
        List<Map<String, Object>> sourceFields,
        List<Map<String, Object>> targetFields,
        double threshold
    ) {
        List<Map<String, Object>> candidates = new ArrayList<>();

        for (Map<String, Object> src : sourceFields) {
            String srcName = (String) src.get("name");
            if (srcName == null || SYSTEM_FIELDS.contains(normalize(srcName))) continue;

            for (Map<String, Object> tgt : targetFields) {
                String tgtName = (String) tgt.get("name");
                if (tgtName == null || SYSTEM_FIELDS.contains(normalize(tgtName))) continue;

                double score = fieldSimilarity(src, tgt);
                if (score >= threshold) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("sourceFieldName", srcName);
                    match.put("targetFieldName", tgtName);
                    match.put("score", score);
                    match.put("sourceLabel", src.get("label"));
                    match.put("targetLabel", tgt.get("label"));
                    candidates.add(match);
                }
            }
        }

        // Greedy matching: pick best score first, each target used at most once
        candidates.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        Set<String> usedSource = new HashSet<>();
        Set<String> usedTarget = new HashSet<>();
        List<Map<String, Object>> matches = new ArrayList<>();

        for (Map<String, Object> c : candidates) {
            String src = (String) c.get("sourceFieldName");
            String tgt = (String) c.get("targetFieldName");
            if (usedSource.contains(src) || usedTarget.contains(tgt)) continue;
            usedSource.add(src);
            usedTarget.add(tgt);
            matches.add(c);
        }

        matches.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return matches;
    }

    // ============================================================
    // Event Date Selection
    // ============================================================

    /**
     * Select the best event date column from a list of fields.
     */
    public static Map<String, Object> selectEventDateColumn(List<Map<String, Object>> fields, String category) {
        List<Map<String, Object>> dateFields = fields.stream()
            .filter(f -> {
                String dt = objectToLowerCase(f.getOrDefault("dataType", f.get("datatype")));
                return dt.equals("date") || dt.equals("datetime") || dt.equals("timestamp");
            })
            .collect(Collectors.toList());

        if (dateFields.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("recommended", null);
            result.put("candidates", List.of());
            result.put("warning", "No date/datetime fields found in this data stream. Event date column cannot be set.");
            return result;
        }

        List<Map<String, Object>> scored = dateFields.stream()
            .map(f -> scoreEventDateField(f, category))
            .sorted((a, b) -> Integer.compare((Integer) b.get("score"), (Integer) a.get("score")))
            .collect(Collectors.toList());

        Map<String, Object> recommended = scored.stream()
            .filter(c -> (Integer) c.get("score") > 0)
            .findFirst()
            .orElse(null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recommended", recommended);
        result.put("candidates", scored);

        if (recommended == null) {
            result.put("warning", "No suitable event date column found. All date fields appear mutable.");
        }

        return result;
    }

    private static Map<String, Object> scoreEventDateField(Map<String, Object> field, String category) {
        String fieldName = (String) field.get("name");
        String norm = normalize(fieldName);
        String dataType = Objects.toString(field.getOrDefault("dataType", field.get("datatype")), "");

        // Check if mutable
        if (MUTABLE_DATE_FIELDS.contains(norm)) {
            return Map.of(
                "fieldName", fieldName,
                "label", field.getOrDefault("label", ""),
                "dataType", dataType,
                "score", -1,
                "reason", "Mutable field — NOT suitable as event date (changes on updates)"
            );
        }

        // Check against known good candidates
        Integer knownScore = EVENT_DATE_CANDIDATES.get(norm);
        if (knownScore != null) {
            return Map.of(
                "fieldName", fieldName,
                "label", field.getOrDefault("label", ""),
                "dataType", dataType,
                "score", knownScore,
                "reason", "Well-known immutable date field"
            );
        }

        // Heuristic: fields with "create" in name
        List<String> tokens = tokenize(fieldName);
        if (tokens.stream().anyMatch(t -> t.equals("created") || t.equals("create") || t.equals("creation"))) {
            return Map.of(
                "fieldName", fieldName,
                "label", field.getOrDefault("label", ""),
                "dataType", dataType,
                "score", 70,
                "reason", "Contains 'create' — likely immutable"
            );
        }

        // For engagement category, prefer event/activity fields
        if (category.equalsIgnoreCase("engagement")) {
            if (tokens.stream().anyMatch(t -> List.of("event", "activity", "interaction", "occurred", "sent", "engagement").contains(t))) {
                return Map.of(
                    "fieldName", fieldName,
                    "label", field.getOrDefault("label", ""),
                    "dataType", dataType,
                    "score", 80,
                    "reason", "Event/activity-related date field"
                );
            }
        }

        // Generic date field
        return Map.of(
            "fieldName", fieldName,
            "label", field.getOrDefault("label", ""),
            "dataType", dataType,
            "score", 30,
            "reason", "Date field (no strong signal for immutability)"
        );
    }
}
