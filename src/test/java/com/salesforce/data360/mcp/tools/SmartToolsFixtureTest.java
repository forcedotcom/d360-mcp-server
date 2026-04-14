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

import com.salesforce.data360.mcp.service.SmartService;
import com.salesforce.data360.mcp.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fixture-based tests for SmartTools/SmartService.
 * Loads real-world DLO/DMO field definitions from JSON fixtures and validates
 * the smart mapping and event date analysis algorithms against them.
 */
@ExtendWith(MockitoExtension.class)
public class SmartToolsFixtureTest {

    private SmartService smartService;

    private List<Map<String, Object>> accountDloFields;
    private List<Map<String, Object>> accountDmoFields;
    private List<Map<String, Object>> engagementDloFields;
    private List<Map<String, Object>> individualDmoFields;
    private List<Map<String, Object>> telcoCsvDloFields;

    @BeforeEach
    void setUp() throws Exception {
        smartService = new SmartService();
        accountDloFields = loadFixture("fixtures/account-dlo-fields.json");
        accountDmoFields = loadFixture("fixtures/account-dmo-fields.json");
        engagementDloFields = loadFixture("fixtures/engagement-dlo-fields.json");
        individualDmoFields = loadFixture("fixtures/individual-dmo-fields.json");
        telcoCsvDloFields = loadFixture("fixtures/telco-csv-dlo-fields.json");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadFixture(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Fixture not found: " + resourcePath);
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return JsonUtil.fromJson(json, List.class);
        }
    }

    // ================================================================
    // Smart Mapping: Account DLO -> Account DMO
    // ================================================================

    @Test
    void testAccountDloToDmo_matchesExpectedFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(accountDloFields, accountDmoFields, 0.5);

        assertFalse(matches.isEmpty(), "Should find matches between Account DLO and DMO");

        // Extract matched source field names for easy assertions
        Set<String> matchedSources = matches.stream()
                .map(m -> (String) m.get("sourceFieldName"))
                .collect(Collectors.toSet());

        // These DLO fields should definitely match to DMO counterparts
        assertTrue(matchedSources.contains("Name"), "Name should be matched");
        assertTrue(matchedSources.contains("AccountSource"), "AccountSource should be matched");
        assertTrue(matchedSources.contains("Industry"), "Industry should be matched");
        assertTrue(matchedSources.contains("Description"), "Description should be matched");
    }

    @Test
    void testAccountDloToDmo_systemFieldsExcluded() {
        List<Map<String, Object>> matches = SmartService.matchFields(accountDloFields, accountDmoFields, 0.5);

        Set<String> matchedSources = matches.stream()
                .map(m -> (String) m.get("sourceFieldName"))
                .collect(Collectors.toSet());

        // System fields should not appear in matches
        assertFalse(matchedSources.contains("DataSource"), "DataSource (system) should not be matched");
        assertFalse(matchedSources.contains("DataSourceObject"), "DataSourceObject (system) should not be matched");
        assertFalse(matchedSources.contains("IsDeleted"), "IsDeleted (system) should not be matched");
        assertFalse(matchedSources.contains("cdp_sys_SourceVersion"), "cdp_sys_SourceVersion (system) should not be matched");
    }

    @Test
    void testAccountDloToDmo_noTargetFieldMatchedTwice() {
        List<Map<String, Object>> matches = SmartService.matchFields(accountDloFields, accountDmoFields, 0.5);

        Set<String> usedTargets = new HashSet<>();
        for (Map<String, Object> match : matches) {
            String target = (String) match.get("targetFieldName");
            assertFalse(usedTargets.contains(target),
                    "Target field '" + target + "' should not be matched more than once");
            usedTargets.add(target);
        }
    }

    @Test
    void testAccountDloToDmo_allScoresAboveThreshold() {
        double threshold = 0.5;
        List<Map<String, Object>> matches = SmartService.matchFields(accountDloFields, accountDmoFields, threshold);

        for (Map<String, Object> match : matches) {
            double score = (Double) match.get("score");
            assertTrue(score >= threshold,
                    "Match " + match.get("sourceFieldName") + " -> " + match.get("targetFieldName") +
                    " has score " + score + " below threshold " + threshold);
        }
    }

    @Test
    void testAccountDloToDmo_highThresholdReducesMatches() {
        List<Map<String, Object>> looseMatches = SmartService.matchFields(accountDloFields, accountDmoFields, 0.3);
        List<Map<String, Object>> strictMatches = SmartService.matchFields(accountDloFields, accountDmoFields, 0.9);

        assertTrue(strictMatches.size() <= looseMatches.size(),
                "Stricter threshold should produce fewer or equal matches");
    }

    // ================================================================
    // Smart Mapping: Telco CSV DLO -> Individual DMO
    // ================================================================

    @Test
    void testTelcoDloToIndividualDmo_matchesExpectedFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(telcoCsvDloFields, individualDmoFields, 0.5);

        assertFalse(matches.isEmpty(), "Should find matches between Telco DLO and Individual DMO");

        Set<String> matchedSources = matches.stream()
                .map(m -> (String) m.get("sourceFieldName"))
                .collect(Collectors.toSet());

        // AGE -> ssot__Age__c, GENDER -> ssot__GenderId__c
        assertTrue(matchedSources.contains("AGE"), "AGE should be matched");
    }

    @Test
    void testTelcoDloToIndividualDmo_systemFieldsExcluded() {
        List<Map<String, Object>> matches = SmartService.matchFields(telcoCsvDloFields, individualDmoFields, 0.5);

        Set<String> matchedSources = matches.stream()
                .map(m -> (String) m.get("sourceFieldName"))
                .collect(Collectors.toSet());

        assertFalse(matchedSources.contains("InternalOrganization"), "InternalOrganization (system) should not be matched");
        assertFalse(matchedSources.contains("DataSource"), "DataSource (system) should not be matched");
        assertFalse(matchedSources.contains("DataSourceObject"), "DataSourceObject (system) should not be matched");
    }

    // Tests for smartMappingSuggest removed — tool replaced by d360_standard_mapping_preview
    // in StandardMappingTools. Similarity matching static helpers are still tested above.

    // ================================================================
    // Event Date Analysis: Engagement DLO
    // ================================================================

    @Test
    @SuppressWarnings("unchecked")
    void testEventDateSelection_engagementFixture() {
        Map<String, Object> result = SmartService.selectEventDateColumn(engagementDloFields, "Engagement");

        assertNotNull(result.get("recommended"), "Should recommend an event date column for engagement data");

        Map<String, Object> recommended = (Map<String, Object>) result.get("recommended");
        String fieldName = (String) recommended.get("fieldName");
        assertNotNull(fieldName, "Recommended field should have a name");

        // The engagement fixture has EventTime as a DateTime field - it should be selected
        assertEquals("EventTime", fieldName,
                "EventTime should be the recommended event date for engagement data");

        int score = (Integer) recommended.get("score");
        assertTrue(score > 0, "Recommended field should have a positive score");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEventDateSelection_engagementFixture_candidatesList() {
        Map<String, Object> result = SmartService.selectEventDateColumn(engagementDloFields, "Engagement");

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
        assertNotNull(candidates, "Should have candidates list");
        assertFalse(candidates.isEmpty(), "Should have at least one date field candidate");

        // All candidates should have required fields
        for (Map<String, Object> c : candidates) {
            assertNotNull(c.get("fieldName"), "Candidate should have fieldName");
            assertNotNull(c.get("score"), "Candidate should have score");
            assertNotNull(c.get("reason"), "Candidate should have reason");
        }

        // Candidates should be sorted by score descending
        for (int i = 0; i < candidates.size() - 1; i++) {
            int current = (Integer) candidates.get(i).get("score");
            int next = (Integer) candidates.get(i + 1).get("score");
            assertTrue(current >= next,
                    "Candidates should be sorted by score descending, but " + current + " < " + next);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEventDateRecommend_engagementFixture() {
        String fieldsJson = JsonUtil.toJson(engagementDloFields);

        String resultJson = smartService.eventDateRecommend(fieldsJson, "Engagement");

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"), "Should not return error");
        assertNotNull(result.get("recommended"), "Should have a recommended field");
        assertNotNull(result.get("candidates"), "Should have candidates");
    }

    // ================================================================
    // Smart Data Stream: Engagement with auto event date
    // ================================================================

    @Test
    @SuppressWarnings("unchecked")
    void testSmartDatastreamCreate_engagementAutoEventDate() {
        // Build a realistic data stream body with engagement fields
        Map<String, Object> dloInfo = new LinkedHashMap<>();
        dloInfo.put("category", "Engagement");
        dloInfo.put("dataLakeFieldInfoRepresentation", engagementDloFields);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dataLakeObjectInfo", dloInfo);

        String bodyJson = JsonUtil.toJson(body);
        String resultJson = smartService.smartDatastreamCreate(bodyJson, true);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertFalse(result.containsKey("error"), "Should not return error");
        assertEquals("smart", result.get("mode"), "Should be in smart mode for engagement");
        assertNotNull(result.get("eventDateAnalysis"), "Should contain event date analysis");
        assertNotNull(result.get("enhancedBody"), "Should contain enhanced body with event date");

        // Verify the enhanced body has eventDateColumn set
        Map<String, Object> enhancedBody = (Map<String, Object>) result.get("enhancedBody");
        Map<String, Object> enhancedDloInfo = (Map<String, Object>) enhancedBody.get("dataLakeObjectInfo");
        assertNotNull(enhancedDloInfo.get("eventDateColumn"), "Enhanced body should have eventDateColumn");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSmartDatastreamCreate_profileCategory_passthrough() {
        // Profile category should pass through without event date selection
        Map<String, Object> dloInfo = new LinkedHashMap<>();
        dloInfo.put("category", "Profile");
        dloInfo.put("dataLakeFieldInfoRepresentation", accountDloFields);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("dataLakeObjectInfo", dloInfo);

        String bodyJson = JsonUtil.toJson(body);
        String resultJson = smartService.smartDatastreamCreate(bodyJson, true);

        Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
        assertEquals("passthrough", result.get("mode"), "Profile category should use passthrough mode");
    }

    // ================================================================
    // Edge Cases
    // ================================================================

    @Test
    void testMatchFields_emptySourceFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(
                List.of(), accountDmoFields, 0.5);
        assertTrue(matches.isEmpty(), "Empty source fields should produce no matches");
    }

    @Test
    void testMatchFields_emptyTargetFields() {
        List<Map<String, Object>> matches = SmartService.matchFields(
                accountDloFields, List.of(), 0.5);
        assertTrue(matches.isEmpty(), "Empty target fields should produce no matches");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEventDateSelection_accountDloFields_noEngagementDates() {
        // Account DLO has date fields but they are mutable (LastModifiedDate, etc.)
        // CreatedDate should still be recommended
        Map<String, Object> result = SmartService.selectEventDateColumn(accountDloFields, "Profile");

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
        assertNotNull(candidates, "Should have candidates even for profile data");
        assertFalse(candidates.isEmpty(), "Account DLO has date fields");
    }
}
