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

import com.salesforce.data360.mcp.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SmartService - field matching algorithms, Jaccard similarity,
 * event date recommendation, and API-calling methods.
 */
@ExtendWith(MockitoExtension.class)
public class SmartServiceTest {

    private SmartService smartService;

    @BeforeEach
    void setUp() {
        smartService = new SmartService();
    }

    // ================================================================
    // Jaccard Similarity
    // ================================================================

    @Test
    void testJaccardSimilarity_exactMatch() {
        Set<String> a = Set.of("first", "name");
        Set<String> b = Set.of("first", "name");
        double similarity = SmartService.jaccardSimilarity(a, b);
        assertEquals(1.0, similarity, 0.001);
    }

    @Test
    void testJaccardSimilarity_noOverlap() {
        Set<String> a = Set.of("email", "address");
        Set<String> b = Set.of("phone", "number");
        double similarity = SmartService.jaccardSimilarity(a, b);
        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    void testJaccardSimilarity_partialOverlap() {
        Set<String> a = Set.of("first", "name", "field");
        Set<String> b = Set.of("first", "field", "label");
        double similarity = SmartService.jaccardSimilarity(a, b);
        assertEquals(0.5, similarity, 0.001);
    }

    @Test
    void testJaccardSimilarity_emptySet() {
        Set<String> a = Set.of();
        Set<String> b = Set.of("first", "name");
        double similarity = SmartService.jaccardSimilarity(a, b);
        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    void testJaccardSimilarity_bothEmpty() {
        double similarity = SmartService.jaccardSimilarity(Set.of(), Set.of());
        assertEquals(1.0, similarity, 0.001);
    }

    // ================================================================
    // Normalize
    // ================================================================

    @Test
    void testNormalize_removesCustomFieldSuffix() {
        assertEquals("email", SmartService.normalize("Email__c"));
        assertEquals("firstname", SmartService.normalize("FirstName__c"));
    }

    @Test
    void testNormalize_removesDlmDllSuffixes() {
        assertEquals("individual", SmartService.normalize("Individual__dlm"));
        assertEquals("contact", SmartService.normalize("Contact__dll"));
    }

    @Test
    void testNormalize_removesSsotPrefix() {
        assertEquals("id", SmartService.normalize("ssot__Id__c"));
        assertEquals("name", SmartService.normalize("SSOT__Name__c"));
    }

    @Test
    void testNormalize_removesUnderscoresAndSpaces() {
        assertEquals("firstname", SmartService.normalize("First_Name"));
        assertEquals("emailaddress", SmartService.normalize("Email Address"));
        assertEquals("personemailaddress", SmartService.normalize("Person_Email_Address"));
    }

    @Test
    void testNormalize_caseFolding() {
        assertEquals("firstname", SmartService.normalize("FirstName"));
        assertEquals("firstname", SmartService.normalize("FIRSTNAME"));
        assertEquals("firstname", SmartService.normalize("firstName"));
    }

    @Test
    void testNormalize_null() {
        assertEquals("", SmartService.normalize(null));
    }

    // ================================================================
    // Tokenize
    // ================================================================

    @Test
    void testTokenize_underscores() {
        List<String> tokens = SmartService.tokenize("First_Name");
        assertEquals(List.of("first", "name"), tokens);
    }

    @Test
    void testTokenize_camelCase() {
        List<String> tokens = SmartService.tokenize("FirstName");
        assertEquals(List.of("first", "name"), tokens);
    }

    @Test
    void testTokenize_mixedCaseAndUnderscores() {
        List<String> tokens = SmartService.tokenize("PersonFirstName__c");
        assertEquals(List.of("person", "first", "name"), tokens);
    }

    @Test
    void testTokenize_ssotPrefix() {
        List<String> tokens = SmartService.tokenize("ssot__EmailAddress__c");
        assertEquals(List.of("email", "address"), tokens);
    }

    @Test
    void testTokenize_null() {
        assertEquals(List.of(), SmartService.tokenize(null));
    }

    // ================================================================
    // Field Similarity
    // ================================================================

    @Test
    void testFieldSimilarity_exactNormalizedMatch() {
        Map<String, Object> src = Map.of("name", "Email__c", "dataType", "Text");
        Map<String, Object> tgt = Map.of("name", "email", "dataType", "Text");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void testFieldSimilarity_tokenOverlap() {
        Map<String, Object> src = Map.of("name", "PersonEmail");
        Map<String, Object> tgt = Map.of("name", "EmailAddress");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertTrue(score >= 0.3 && score <= 0.4);
    }

    @Test
    void testFieldSimilarity_labelMatch() {
        Map<String, Object> src = Map.of("name", "Field1__c", "label", "Email Address");
        Map<String, Object> tgt = Map.of("name", "Field2__c", "label", "Email Address");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertTrue(score >= 0.9);
    }

    @Test
    void testFieldSimilarity_substringContainment() {
        Map<String, Object> src = Map.of("name", "Email");
        Map<String, Object> tgt = Map.of("name", "PersonEmail");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertTrue(score >= 0.75);
    }

    @Test
    void testFieldSimilarity_dataTypeCompatibilityBonus() {
        Map<String, Object> src = Map.of("name", "Amount", "dataType", "Number");
        Map<String, Object> tgt = Map.of("name", "Total", "dataType", "Number");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertTrue(score > 0.0);
    }

    @Test
    void testFieldSimilarity_nullNames() {
        Map<String, Object> src = Map.of("label", "Email");
        Map<String, Object> tgt = Map.of("name", "email");
        double score = SmartService.fieldSimilarity(src, tgt);
        assertEquals(0.0, score, 0.001);
    }

    // ================================================================
    // Match Fields
    // ================================================================

    @Test
    void testMatchFields_systemFieldsSkipped() {
        List<Map<String, Object>> sourceFields = List.of(
            Map.of("name", "Email", "dataType", "Text"),
            Map.of("name", "DataSource", "dataType", "Text"),
            Map.of("name", "FirstName", "dataType", "Text")
        );
        List<Map<String, Object>> targetFields = List.of(
            Map.of("name", "email", "dataType", "Text"),
            Map.of("name", "first_name", "dataType", "Text")
        );

        List<Map<String, Object>> matches = SmartService.matchFields(sourceFields, targetFields, 0.5);

        assertEquals(2, matches.size());
        assertFalse(matches.stream().anyMatch(m ->
            m.get("sourceFieldName").equals("DataSource")));
    }

    @Test
    void testMatchFields_greedyMatching() {
        List<Map<String, Object>> sourceFields = List.of(
            Map.of("name", "Email", "dataType", "Text"),
            Map.of("name", "PersonEmail", "dataType", "Text")
        );
        List<Map<String, Object>> targetFields = List.of(
            Map.of("name", "email", "dataType", "Text")
        );

        List<Map<String, Object>> matches = SmartService.matchFields(sourceFields, targetFields, 0.5);

        assertEquals(1, matches.size());
        assertEquals("Email", matches.get(0).get("sourceFieldName"));
    }

    @Test
    void testMatchFields_thresholdFiltering() {
        List<Map<String, Object>> sourceFields = List.of(
            Map.of("name", "FirstName", "dataType", "Text"),
            Map.of("name", "XYZ", "dataType", "Text")
        );
        List<Map<String, Object>> targetFields = List.of(
            Map.of("name", "first_name", "dataType", "Text"),
            Map.of("name", "ABC", "dataType", "Text")
        );

        List<Map<String, Object>> matches = SmartService.matchFields(sourceFields, targetFields, 0.9);

        assertEquals(1, matches.size());
        assertEquals("FirstName", matches.get(0).get("sourceFieldName"));
    }

    @Test
    void testMatchFields_emptySourceFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(
            List.of(), List.of(Map.of("name", "email")), 0.5);
        assertTrue(matches.isEmpty());
    }

    @Test
    void testMatchFields_emptyTargetFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(
            List.of(Map.of("name", "email")), List.of(), 0.5);
        assertTrue(matches.isEmpty());
    }

    // ================================================================
    // Event Date Selection
    // ================================================================

    @Test
    void testSelectEventDateColumn_createdDatePreferred() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "CreatedDate", "dataType", "DateTime"),
            Map.of("name", "LastModifiedDate", "dataType", "DateTime"),
            Map.of("name", "EventDate", "dataType", "Date")
        );

        Map<String, Object> result = SmartService.selectEventDateColumn(fields, "Engagement");

        assertNotNull(result.get("recommended"));
        @SuppressWarnings("unchecked")
        Map<String, Object> recommended = (Map<String, Object>) result.get("recommended");
        assertEquals("CreatedDate", recommended.get("fieldName"));
        assertTrue((Integer) recommended.get("score") >= 95);
    }

    @Test
    void testSelectEventDateColumn_mutableFieldsRejected() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "LastModifiedDate", "dataType", "DateTime"),
            Map.of("name", "SystemModstamp", "dataType", "DateTime")
        );

        Map<String, Object> result = SmartService.selectEventDateColumn(fields, "Engagement");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
        assertTrue(candidates.stream().allMatch(c -> (Integer) c.get("score") < 0));
        assertNull(result.get("recommended"));
        assertNotNull(result.get("warning"));
    }

    @Test
    void testSelectEventDateColumn_noDateFields() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "Name", "dataType", "Text"),
            Map.of("name", "Amount", "dataType", "Number")
        );

        Map<String, Object> result = SmartService.selectEventDateColumn(fields, "Engagement");

        assertNull(result.get("recommended"));
        assertNotNull(result.get("warning"));
        assertTrue(((String) result.get("warning")).contains("No date/datetime fields"));
    }

    @Test
    void testSelectEventDateColumn_eventDateForEngagement() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "EventDateTime", "dataType", "DateTime"),
            Map.of("name", "SomeOtherDate", "dataType", "Date")
        );

        Map<String, Object> result = SmartService.selectEventDateColumn(fields, "Engagement");

        assertNotNull(result.get("recommended"));
        @SuppressWarnings("unchecked")
        Map<String, Object> recommended = (Map<String, Object>) result.get("recommended");
        assertEquals("EventDateTime", recommended.get("fieldName"));
    }

    @Test
    void testSelectEventDateColumn_heuristicForCreateFields() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "CustomCreateDate", "dataType", "DateTime"),
            Map.of("name", "UpdateDate", "dataType", "DateTime")
        );

        Map<String, Object> result = SmartService.selectEventDateColumn(fields, "Profile");

        assertNotNull(result.get("recommended"));
        @SuppressWarnings("unchecked")
        Map<String, Object> recommended = (Map<String, Object>) result.get("recommended");
        assertEquals("CustomCreateDate", recommended.get("fieldName"));
        assertTrue(((String) recommended.get("reason")).contains("create"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEventDateRecommend_success() {
        List<Map<String, Object>> fields = List.of(
            Map.of("name", "CreatedDate", "dataType", "DateTime"),
            Map.of("name", "Name", "dataType", "Text")
        );
        String fieldsJson = JsonUtil.toJson(fields);

        String resultJson = smartService.eventDateRecommend(fieldsJson, "Engagement");

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"));
        assertNotNull(result.get("recommended"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSmartDatastreamCreate_engagementAutoEventDate() {
        Map<String, Object> dloInfo = new LinkedHashMap<>();
        dloInfo.put("category", "Engagement");
        dloInfo.put("dataLakeFieldInfoRepresentation", List.of(
            Map.of("name", "EventTime", "dataType", "DateTime"),
            Map.of("name", "Name", "dataType", "Text")
        ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dataLakeObjectInfo", dloInfo);

        String bodyJson = JsonUtil.toJson(body);
        String resultJson = smartService.smartDatastreamCreate(bodyJson, true);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"));
        assertEquals("smart", result.get("mode"));
        assertNotNull(result.get("eventDateAnalysis"));
        assertNotNull(result.get("enhancedBody"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSmartDatastreamCreate_profileCategory_passthrough() {
        Map<String, Object> dloInfo = new LinkedHashMap<>();
        dloInfo.put("category", "Profile");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dataLakeObjectInfo", dloInfo);

        String bodyJson = JsonUtil.toJson(body);
        String resultJson = smartService.smartDatastreamCreate(bodyJson, true);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertEquals("passthrough", result.get("mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSmartDatastreamCreate_autoSelectDisabled_passthrough() {
        Map<String, Object> dloInfo = new LinkedHashMap<>();
        dloInfo.put("category", "Engagement");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dataLakeObjectInfo", dloInfo);

        String bodyJson = JsonUtil.toJson(body);
        String resultJson = smartService.smartDatastreamCreate(bodyJson, false);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertEquals("passthrough", result.get("mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testPreviewFieldMatches_success() {
        List<Map<String, Object>> sourceFields = List.of(
            Map.of("name", "Email", "label", "Email", "dataType", "Text"),
            Map.of("name", "FirstName", "label", "First Name", "dataType", "Text")
        );
        List<Map<String, Object>> targetFields = List.of(
            Map.of("name", "email", "label", "Email", "dataType", "Text"),
            Map.of("name", "first_name", "label", "First Name", "dataType", "Text")
        );

        String resultJson = smartService.previewFieldMatches(sourceFields, targetFields, "MyDLO", "MyDMO", null);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"));
        assertEquals("MyDLO", result.get("sourceDlo"));
        assertEquals("MyDMO", result.get("targetDmo"));
        assertEquals(2, result.get("matchedFieldCount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSmartMappingSuggest_success() {
        List<Map<String, Object>> sourceFields = List.of(
            Map.of("name", "Email", "label", "Email", "dataType", "Text")
        );
        List<Map<String, Object>> targetFields = List.of(
            Map.of("name", "email", "label", "Email", "dataType", "Text")
        );

        String resultJson = smartService.smartMappingSuggest(sourceFields, targetFields, "MyDLO", "MyDMO", null, null);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"));
        assertNotNull(result.get("analysis"));
        assertNotNull(result.get("mappingPayload"));
    }
}
