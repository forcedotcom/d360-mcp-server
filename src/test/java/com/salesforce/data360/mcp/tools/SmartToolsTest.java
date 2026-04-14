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

import com.salesforce.data360.mcp.model.request.smart.FieldInput;
import com.salesforce.data360.mcp.model.request.smart.FieldOverrideInput;
import com.salesforce.data360.mcp.service.SmartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for SmartTools - verifies that SmartTools delegates to SmartService
 * and converts FieldInput/FieldOverrideInput DTOs to List<Map> correctly.
 */
@ExtendWith(MockitoExtension.class)
public class SmartToolsTest {

    @Mock
    private SmartService smartService;

    @Test
    void testSmartMappingSuggest_delegatesToService() {
        SmartTools tools = new SmartTools(smartService);

        when(smartService.smartMappingSuggest(anyList(), anyList(), any(), any(), any(), any()))
            .thenReturn("{\"result\":\"ok\"}");

        List<FieldInput> sourceFields = List.of(
            new FieldInput("Email", "Email Address", "Text")
        );
        List<FieldInput> targetFields = List.of(
            new FieldInput("email", "Email", "Text")
        );
        List<FieldOverrideInput> overrides = List.of(
            new FieldOverrideInput("KQ_Id", "ssot__ExternalId__c")
        );

        tools.smartMappingSuggest(sourceFields, targetFields, "DLO", "DMO", 0.5, overrides);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, Object>>> srcCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, Object>>> tgtCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, Object>>> overrideCaptor = ArgumentCaptor.forClass(List.class);

        verify(smartService).smartMappingSuggest(
            srcCaptor.capture(), tgtCaptor.capture(),
            eq("DLO"), eq("DMO"), eq(0.5), overrideCaptor.capture());

        assertThat(srcCaptor.getValue()).hasSize(1);
        assertThat(srcCaptor.getValue().get(0).get("name")).isEqualTo("Email");
        assertThat(srcCaptor.getValue().get(0).get("label")).isEqualTo("Email Address");
        assertThat(srcCaptor.getValue().get(0).get("dataType")).isEqualTo("Text");

        assertThat(tgtCaptor.getValue()).hasSize(1);
        assertThat(tgtCaptor.getValue().get(0).get("name")).isEqualTo("email");

        assertThat(overrideCaptor.getValue()).hasSize(1);
        assertThat(overrideCaptor.getValue().get(0).get("source")).isEqualTo("KQ_Id");
        assertThat(overrideCaptor.getValue().get(0).get("target")).isEqualTo("ssot__ExternalId__c");
    }

    @Test
    void testSmartMappingSuggest_nullOverrides() {
        SmartTools tools = new SmartTools(smartService);

        when(smartService.smartMappingSuggest(anyList(), anyList(), any(), any(), any(), isNull()))
            .thenReturn("{\"result\":\"ok\"}");

        tools.smartMappingSuggest(List.of(), List.of(), "DLO", "DMO", 0.5, null);

        verify(smartService).smartMappingSuggest(anyList(), anyList(), eq("DLO"), eq("DMO"), eq(0.5), isNull());
    }

    @Test
    void testPreviewFieldMatches_delegatesToService() {
        SmartTools tools = new SmartTools(smartService);

        when(smartService.previewFieldMatches(anyList(), anyList(), any(), any(), any()))
            .thenReturn("{\"result\":\"ok\"}");

        List<FieldInput> sourceFields = List.of(
            new FieldInput("Email", null, "Text"),
            new FieldInput("FirstName", "First Name", "Text")
        );
        List<FieldInput> targetFields = List.of(
            new FieldInput("email", null, "Text")
        );

        tools.previewFieldMatches(sourceFields, targetFields, "DLO", "DMO", 0.5);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, Object>>> srcCaptor = ArgumentCaptor.forClass(List.class);
        verify(smartService).previewFieldMatches(
            srcCaptor.capture(), anyList(), eq("DLO"), eq("DMO"), eq(0.5));

        assertThat(srcCaptor.getValue()).hasSize(2);
        assertThat(srcCaptor.getValue().get(0).get("name")).isEqualTo("Email");
    }

    @Test
    void testSmartDatastreamCreate_delegatesToService() {
        SmartTools tools = new SmartTools(smartService);

        when(smartService.smartDatastreamCreate(any(), any()))
            .thenReturn("{\"result\":\"ok\"}");

        tools.smartDatastreamCreate("{}", true);

        verify(smartService).smartDatastreamCreate("{}", true);
    }

    @Test
    void testEventDateRecommend_delegatesToService() {
        SmartTools tools = new SmartTools(smartService);

        when(smartService.eventDateRecommend(any(), any()))
            .thenReturn("{\"result\":\"ok\"}");

        tools.eventDateRecommend("[]", "Engagement");

        verify(smartService).eventDateRecommend("[]", "Engagement");
    }
}
