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

import com.salesforce.data360.mcp.service.MappingLookupService.SObjectDmoMappings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MappingLookupService - XML parsing, indexing, and lookup.
 * Uses the real mapping files from classpath:mappings/.
 */
class MappingLookupServiceTest {

    private MappingLookupService service;

    @BeforeEach
    void setUp() {
        service = new MappingLookupService();
        service.loadMappings();
    }

    @Test
    void testLoadMappings_loadsMultipleFiles() {
        assertThat(service.size()).isGreaterThan(100);
    }

    @Test
    void testLookup_exactMatch() {
        SObjectDmoMappings result = service.lookup("Account");
        assertThat(result).isNotNull();
        assertThat(result.sourceObjectName()).isEqualTo("Account");
        assertThat(result.dmoMappings()).isNotEmpty();
    }

    @Test
    void testLookup_caseInsensitive() {
        SObjectDmoMappings result = service.lookup("account");
        assertThat(result).isNotNull();
        assertThat(result.sourceObjectName()).isEqualTo("Account");
    }

    @Test
    void testLookup_caseInsensitiveUpperCase() {
        SObjectDmoMappings result = service.lookup("ACCOUNT");
        assertThat(result).isNotNull();
        assertThat(result.sourceObjectName()).isEqualTo("Account");
    }

    @Test
    void testLookup_dloSuffixStripping() {
        // Simulates a DLO name like "Account_00D000000000000__dll"
        SObjectDmoMappings result = service.lookup("Account_00D000000000000__dll");
        assertThat(result).isNotNull();
        assertThat(result.sourceObjectName()).isEqualTo("Account");
    }

    @Test
    void testLookup_dloWithDlmSuffix() {
        SObjectDmoMappings result = service.lookup("Contact_00D000000000000__dlm");
        assertThat(result).isNull();
    }

    @Test
    void testLookup_notFound() {
        SObjectDmoMappings result = service.lookup("NonExistentObject999");
        assertThat(result).isNull();
    }

    @Test
    void testLookup_null() {
        SObjectDmoMappings result = service.lookup(null);
        assertThat(result).isNull();
    }

    @Test
    void testLookup_accountHasMultipleDmoTargets() {
        SObjectDmoMappings result = service.lookup("Account");
        assertThat(result).isNotNull();
        // Account maps to AccountDmo, ContactPointAddressDmo, ContactPointPhoneDmo, etc.
        assertThat(result.dmoMappings().size()).isGreaterThanOrEqualTo(2);
        assertThat(result.dmoMappings())
            .anyMatch(dm -> dm.dmoName().equals("AccountDmo"));
    }

    @Test
    void testLookup_fieldMappingsPopulated() {
        SObjectDmoMappings result = service.lookup("Account");
        assertThat(result).isNotNull();

        var accountDmo = result.dmoMappings().stream()
            .filter(dm -> dm.dmoName().equals("AccountDmo"))
            .findFirst()
            .orElseThrow();

        assertThat(accountDmo.fieldMappings()).isNotEmpty();
        assertThat(accountDmo.fieldMappings())
            .anyMatch(fm -> fm.sourceField().equals("Name__c") && fm.targetField().equals("Name"));
    }

    @Test
    void testLookup_contactMapping() {
        SObjectDmoMappings result = service.lookup("Contact");
        assertThat(result).isNotNull();
        assertThat(result.dmoMappings())
            .anyMatch(dm -> dm.dmoName().equals("IndividualDmo"));
    }

    @Test
    void testStripDloSuffix_basic() {
        assertThat(MappingLookupService.stripDloSuffix("Account_00D000000000000__dll"))
            .isEqualTo("Account");
    }

    @Test
    void testStripDloSuffix_18charOrgId() {
        assertThat(MappingLookupService.stripDloSuffix("Account_00D000000000000UAE__dll"))
            .isEqualTo("Account");
    }

    @Test
    void testStripDloSuffix_dlmSuffix() {
        assertThat(MappingLookupService.stripDloSuffix("Contact_00D000000000000__dlm"))
            .isEqualTo("Contact_00D000000000000__dlm");
    }

    @Test
    void testStripDloSuffix_noSuffix() {
        assertThat(MappingLookupService.stripDloSuffix("Account"))
            .isEqualTo("Account");
    }

    @Test
    void testStripDloSuffix_nonOrgIdMiddle() {
        assertThat(MappingLookupService.stripDloSuffix("Account_Home__dll"))
            .isEqualTo("Account");
    }

    @Test
    void testStripDloSuffix_onlyDll() {
        assertThat(MappingLookupService.stripDloSuffix("MyObject__dll"))
            .isEqualTo("MyObject");
    }
}
