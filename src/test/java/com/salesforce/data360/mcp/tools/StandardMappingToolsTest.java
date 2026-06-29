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
import com.salesforce.data360.mcp.service.MappingLookupService;
import com.salesforce.data360.mcp.service.MappingLookupService.DmoMapping;
import com.salesforce.data360.mcp.service.MappingLookupService.FieldMapping;
import com.salesforce.data360.mcp.service.MappingLookupService.SObjectDmoMappings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandardMappingToolsTest {

    @Mock
    private MappingLookupService mappingLookupService;

    @Mock
    private Data360Client client;

    private StandardMappingTools standardMappingTools;

    @BeforeEach
    void setUp() {
        standardMappingTools = new StandardMappingTools(mappingLookupService, client);
    }

    /** Stub {@code client.get} for the DLO fetch with the wrapped shape the server actually returns. */
    private void stubDloFields(String... fieldNames) {
        List<Map<String, Object>> fields = new java.util.ArrayList<>();
        for (String name : fieldNames) {
            fields.add(Map.of("name", name));
        }
        Map<String, Object> response = Map.of(
            "dataLakeObjects", List.of(
                Map.of("dataLakeFieldInfoRepresentation", fields)
            )
        );
        when(client.get(contains("/ssot/data-lake-objects/"), eq(Map.class))).thenReturn(response);
    }

    // ================================================================
    // d360_standard_mapping_preview
    // ================================================================

    @Test
    void testPreview_returnsStandardMappingWhenFound() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null),
                new FieldMapping("Id", "Id", true, "Equal")
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account")).thenReturn(mapping);

        String result = standardMappingTools.standardMappingPreview("Account", null);

        assertThat(result).contains("\"found\":true", "\"sourceObjectName\":\"Account\"",
            "AccountDmo", "ContactPointPhoneDmo", "\"sourceField\":\"Name\"",
            "\"targetDmoCount\":2", "\"summary\":", "d360_standard_mapping_create");
    }

    @Test
    void testPreview_filtersTargetDmo() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null)
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account")).thenReturn(mapping);

        String result = standardMappingTools.standardMappingPreview("Account", "AccountDmo");

        assertThat(result).contains("\"found\":true", "\"targetDmoName\":\"AccountDmo\"", "\"targetDmoCount\":1");
        assertThat(result).doesNotContain("\"targetDmoName\":\"ContactPointPhoneDmo\"");
    }

    @Test
    void testPreview_includesFilterAttributes() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Id", "Id", true, "Equal")
            ))
        ));
        when(mappingLookupService.lookup("Account")).thenReturn(mapping);

        String result = standardMappingTools.standardMappingPreview("Account", null);

        assertThat(result).contains("\"isFilterApplied\":true", "\"filterOperationType\":\"Equal\"");
    }

    @Test
    void testPreview_returnsNotFoundWhenNoMapping() {
        when(mappingLookupService.lookup("CustomObject")).thenReturn(null);
        when(mappingLookupService.size()).thenReturn(550);

        String result = standardMappingTools.standardMappingPreview("CustomObject", null);

        assertThat(result).contains("\"found\":false", "\"availableStandardMappings\":550");
    }

    @Test
    void testPreview_trimsSourceObjectName() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account")).thenReturn(mapping);

        String result = standardMappingTools.standardMappingPreview("  Account  ", null);

        assertThat(result).contains("\"found\":true");
    }

    @Test
    void testPreview_includesSummary() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null),
                new FieldMapping("Id", "Id", false, null)
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account")).thenReturn(mapping);

        String result = standardMappingTools.standardMappingPreview("Account", null);

        assertThat(result).contains("AccountDmo: 2 fields", "ContactPointPhoneDmo: 1 fields", "2 DMOs");
    }

    // ================================================================
    // d360_standard_mapping_create
    // ================================================================

    @Test
    void testCreate_createsAllMappings() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null),
                new FieldMapping("Id", "Id", true, "Equal")
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account__dll")).thenReturn(mapping);
        stubDloFields("Name", "Id", "Phone");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"))
            .thenReturn(Map.of("name", "mapping2"));

        String result = standardMappingTools.standardMappingCreate("Account__dll", null);

        assertThat(result).contains("\"succeeded\":2", "\"failed\":0", "\"attempted\":2", "\"totalDmos\":2");

        // Verify two POST calls were made
        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client, times(2)).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        List<Map> bodies = bodyCaptor.getAllValues();
        assertThat(bodies.get(0).get("sourceEntityDeveloperName")).isEqualTo("Account__dll");
        assertThat(bodies.get(0).get("targetEntityDeveloperName")).isEqualTo("AccountDmo");
        assertThat(bodies.get(1).get("targetEntityDeveloperName")).isEqualTo("ContactPointPhoneDmo");
    }

    @Test
    void testCreate_excludesDmos() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null)
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            )),
            new DmoMapping("FinancialCustomerDmo", List.of(
                new FieldMapping("Rating", "Rating", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account__dll")).thenReturn(mapping);
        stubDloFields("Name", "Phone", "Rating");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"));

        String result = standardMappingTools.standardMappingCreate(
            "Account__dll", "ContactPointPhoneDmo, FinancialCustomerDmo");

        assertThat(result).contains("\"attempted\":1", "\"succeeded\":1", "\"totalDmos\":3",
            "ContactPointPhoneDmo", "FinancialCustomerDmo", "\"skipped\"");

        verify(client, times(1)).post(anyString(), anyMap(), eq(Map.class));
    }

    @Test
    void testCreate_handlesPartialFailure() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null)
            )),
            new DmoMapping("ContactPointPhoneDmo", List.of(
                new FieldMapping("Phone", "TelephoneNumber", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account__dll")).thenReturn(mapping);
        stubDloFields("Name", "Phone");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"))
            .thenThrow(new ApiException(409, "Mapping already exists for ContactPointPhoneDmo", "/ssot/data-model-object-mappings"));

        String result = standardMappingTools.standardMappingCreate("Account__dll", null);

        assertThat(result).contains("\"succeeded\":1", "\"failed\":1",
            "\"status\":\"success\"", "\"status\":\"error\"", "Mapping already exists");
    }

    @Test
    void testCreate_returnsErrorWhenNoMapping() {
        when(mappingLookupService.lookup("UnknownObject")).thenReturn(null);
        when(mappingLookupService.size()).thenReturn(550);

        String result = standardMappingTools.standardMappingCreate("UnknownObject", null);

        assertThat(result).contains("\"error\"", "No standard mapping found");
        verifyNoInteractions(client);
    }

    @Test
    void testCreate_usesCorrectEndpointWithNoDataspace() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name", "Name", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account__dll")).thenReturn(mapping);
        stubDloFields("Name");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"));

        standardMappingTools.standardMappingCreate("Account__dll", null);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(pathCaptor.capture(), anyMap(), eq(Map.class));
        assertThat(pathCaptor.getValue()).isEqualTo("/ssot/data-model-object-mappings");
    }

    @Test
    void testCreate_sendsCorrectFieldMappings() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Contact", List.of(
            new DmoMapping("IndividualDmo", List.of(
                new FieldMapping("FirstName", "FirstName", false, null),
                new FieldMapping("LastName", "LastName", false, null),
                new FieldMapping("Id", "Id", true, "Equal")
            ))
        ));
        when(mappingLookupService.lookup("Contact__dll")).thenReturn(mapping);
        stubDloFields("FirstName", "LastName", "Id");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"));

        standardMappingTools.standardMappingCreate("Contact__dll", null);

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));

        Map body = bodyCaptor.getValue();
        assertThat(body.get("sourceEntityDeveloperName")).isEqualTo("Contact__dll");
        assertThat(body.get("targetEntityDeveloperName")).isEqualTo("IndividualDmo");

        List<Map<String, String>> fieldMappings = (List<Map<String, String>>) body.get("fieldMapping");
        assertThat(fieldMappings).hasSize(3);
        assertThat(fieldMappings.get(0).get("sourceFieldDeveloperName")).isEqualTo("FirstName");
        assertThat(fieldMappings.get(0).get("targetFieldDeveloperName")).isEqualTo("FirstName");
        assertThat(fieldMappings.get(2).get("sourceFieldDeveloperName")).isEqualTo("Id");
    }

    @Test
    void testCreate_unwrapsDataLakeObjectsArray() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Lead", List.of(
            new DmoMapping("IndividualDmo", List.of(
                new FieldMapping("Id__c", "Id", true, "Equal"),
                new FieldMapping("FirstName__c", "FirstName", false, null),
                new FieldMapping("LastName__c", "LastName", false, null),
                new FieldMapping("NotOnDlo__c", "SomeTarget", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Lead_Home__dll")).thenReturn(mapping);
        stubDloFields("Id__c", "FirstName__c", "LastName__c", "Phone__c");
        when(client.post(anyString(), anyMap(), eq(Map.class)))
            .thenReturn(Map.of("name", "mapping1"));

        String result = standardMappingTools.standardMappingCreate("Lead_Home__dll", null);

        assertThat(result).contains("\"succeeded\":1", "\"failed\":0", "\"dloFieldCount\":4",
            "\"skippedFields\":[\"NotOnDlo__c\"]");

        ArgumentCaptor<Map> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), bodyCaptor.capture(), eq(Map.class));
        List<Map<String, String>> fieldMappings = (List<Map<String, String>>) bodyCaptor.getValue().get("fieldMapping");
        assertThat(fieldMappings).hasSize(3);
        assertThat(fieldMappings).extracting(fm -> fm.get("sourceFieldDeveloperName"))
            .containsExactly("Id__c", "FirstName__c", "LastName__c");
    }

    @Test
    void testCreate_returnsErrorWhenDloHasNoFields() {
        SObjectDmoMappings mapping = new SObjectDmoMappings("Account", List.of(
            new DmoMapping("AccountDmo", List.of(
                new FieldMapping("Name__c", "Name", false, null)
            ))
        ));
        when(mappingLookupService.lookup("Account__dll")).thenReturn(mapping);
        when(client.get(contains("/ssot/data-lake-objects/"), eq(Map.class)))
            .thenReturn(Map.of("dataLakeObjects", List.of(Map.of())));

        String result = standardMappingTools.standardMappingCreate("Account__dll", null);

        assertThat(result).contains("\"error\":", "Account__dll", "returned no fields");
        verify(client, never()).post(anyString(), anyMap(), eq(Map.class));
    }
}
