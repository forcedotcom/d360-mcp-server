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
package com.salesforce.data360.mcp.tools.machinelearning.setup;

import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.model.common.ApiException;
import com.salesforce.data360.mcp.model.request.machinelearning.AssetReferenceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.DataProfileRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.FieldSourceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.FilterCriterionInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.FilterInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.FilterValueInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.InputSourceInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.JoinCriteriaInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.JoinFieldsInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.JoinInput;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.OutcomeRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.RowCountRequest;
import com.salesforce.data360.mcp.model.request.machinelearning.setup.SetupFieldsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelSetupHelperToolsTest {

    @Mock
    private Data360Client client;

    private ModelSetupHelperTools tools;

    @BeforeEach
    void setUp() {
        tools = new ModelSetupHelperTools(client);
    }

    private static InputSourceInput dmoInput(String dmoName) {
        AssetReferenceInput src = new AssetReferenceInput();
        src.setName(dmoName);
        AssetReferenceInput ds = new AssetReferenceInput();
        ds.setName("default");
        InputSourceInput in = new InputSourceInput();
        in.setType("DataModelObject");
        in.setSource(src);
        in.setDataSpace(ds);
        return in;
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void querySetupFields_postsToCorrectPathWithBody() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("fields", List.of(), "transformations", List.of()));

        SetupFieldsRequest req = new SetupFieldsRequest();
        req.setInput(dmoInput("Sales__dlm"));
        req.setFilterFields(true);

        String result = tools.querySetupFields(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));

        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/query-setup-fields");
        assertThat(body.getValue()).containsEntry("filterFields", true);
        Map<String, Object> input = (Map<String, Object>) body.getValue().get("input");
        assertThat(input).containsEntry("type", "DataModelObject");
        assertThat((Map<String, Object>) input.get("source")).containsEntry("name", "Sales__dlm");
        assertThat(result).contains("fields");
    }

    @Test
    void querySetupFields_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(404, "Not found", "/ssot/machine-learning/query-setup-fields"));

        SetupFieldsRequest req = new SetupFieldsRequest();
        req.setInput(dmoInput("Missing__dlm"));

        String result = tools.querySetupFields(req);

        assertThat(result).contains("error", "404", "/ssot/machine-learning/query-setup-fields");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void queryDataProfile_postsProfileFieldNames() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("fields", List.of()));

        FieldSourceInput field = new FieldSourceInput();
        field.setName("Amount__c");
        field.setSource("Sales__dlm");
        field.setType("DataModelObject");

        DataProfileRequest req = new DataProfileRequest();
        req.setInput(dmoInput("Sales__dlm"));
        req.setProfileFieldNames(List.of(field));

        tools.queryDataProfile(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));

        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/query-data-profile");
        List<Map<String, Object>> profileFields = (List<Map<String, Object>>) body.getValue().get("profileFieldNames");
        assertThat(profileFields).hasSize(1);
        assertThat(profileFields.get(0))
            .containsEntry("name", "Amount__c")
            .containsEntry("source", "Sales__dlm")
            .containsEntry("type", "DataModelObject");
    }

    @Test
    void queryDataProfile_apiError_returnsStructuredJson() {
        when(client.post(anyString(), any(), eq(Map.class)))
            .thenThrow(new ApiException(500, "boom", "/ssot/machine-learning/query-data-profile"));

        DataProfileRequest req = new DataProfileRequest();
        req.setInput(dmoInput("Sales__dlm"));

        String result = tools.queryDataProfile(req);

        assertThat(result).contains("error", "500");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void queryOutcome_passesPredictionType() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("outcomes", List.of()));

        OutcomeRequest req = new OutcomeRequest();
        req.setInput(dmoInput("Sales__dlm"));
        req.setPredictionType("BinaryClassification");

        tools.queryOutcome(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(path.capture(), body.capture(), eq(Map.class));

        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/query-outcome");
        assertThat(body.getValue()).containsEntry("predictionType", "BinaryClassification");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void queryOutcome_omitsPredictionTypeWhenNull() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("outcomes", List.of()));

        OutcomeRequest req = new OutcomeRequest();
        req.setInput(dmoInput("Sales__dlm"));

        tools.queryOutcome(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        assertThat(body.getValue()).doesNotContainKey("predictionType");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void queryRowCount_postsToCorrectPath() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("rowCount", 1234));

        RowCountRequest req = new RowCountRequest();
        req.setInput(dmoInput("Sales__dlm"));

        String result = tools.queryRowCount(req);

        ArgumentCaptor<String> path = ArgumentCaptor.forClass(String.class);
        verify(client).post(path.capture(), any(Map.class), eq(Map.class));
        assertThat(path.getValue()).isEqualTo("/ssot/machine-learning/query-row-count");
        assertThat(result).contains("1234");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void filterShape_serializesCriteriaAndConjunctiveOperator() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("rowCount", 0));

        FieldSourceInput filterField = new FieldSourceInput();
        filterField.setName("Amount__c");
        filterField.setSource("Sales__dlm");
        filterField.setType("DataModelObject");
        FilterValueInput val = new FilterValueInput();
        val.setValue("1000");
        val.setType("Constant");
        FilterCriterionInput crit = new FilterCriterionInput();
        crit.setFilterField(filterField);
        crit.setOperator("GreaterThan");
        crit.setValues(List.of(val));

        FilterInput filter = new FilterInput();
        filter.setCriteria(List.of(crit));
        filter.setConjunctiveOperator("And");

        RowCountRequest req = new RowCountRequest();
        req.setInput(dmoInput("Sales__dlm"));
        req.setFilter(filter);

        tools.queryRowCount(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> filterMap = (Map<String, Object>) body.getValue().get("filter");
        assertThat(filterMap).containsEntry("conjunctiveOperator", "And");
        List<Map<String, Object>> criteria = (List<Map<String, Object>>) filterMap.get("criteria");
        assertThat(criteria).hasSize(1);
        assertThat(criteria.get(0)).containsEntry("operator", "GreaterThan");
        assertThat(criteria.get(0)).doesNotContainKey("field");
        Map<String, Object> ff = (Map<String, Object>) criteria.get(0).get("filterField");
        assertThat(ff).containsEntry("name", "Amount__c").containsEntry("source", "Sales__dlm").containsEntry("type", "DataModelObject");
        List<Map<String, Object>> values = (List<Map<String, Object>>) criteria.get(0).get("values");
        assertThat(values.get(0)).containsEntry("value", "1000").containsEntry("type", "Constant");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void joinShape_serializesCriteriaWithFieldPairs() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("rowCount", 0));

        JoinFieldsInput pair = new JoinFieldsInput();
        pair.setLeftField("CustomerId__c");
        pair.setRightField("Id__c");
        JoinCriteriaInput jc = new JoinCriteriaInput();
        jc.setFields(List.of(pair));
        jc.setOperator("And");
        jc.setType("Inner");
        jc.setLeft("Sales__dlm");
        jc.setRight("Customer__dlm");
        JoinInput join = new JoinInput();
        join.setCriteria(List.of(jc));

        RowCountRequest req = new RowCountRequest();
        req.setInput(dmoInput("Sales__dlm"));
        req.setJoin(join);

        tools.queryRowCount(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> joinMap = (Map<String, Object>) body.getValue().get("join");
        List<Map<String, Object>> criteria = (List<Map<String, Object>>) joinMap.get("criteria");
        assertThat(criteria).hasSize(1);
        assertThat(criteria.get(0))
            .containsEntry("type", "Inner")
            .containsEntry("operator", "And")
            .containsEntry("left", "Sales__dlm")
            .containsEntry("right", "Customer__dlm");
        List<Map<String, Object>> fieldPairs = (List<Map<String, Object>>) criteria.get(0).get("fields");
        assertThat(fieldPairs.get(0))
            .containsEntry("leftField", "CustomerId__c")
            .containsEntry("rightField", "Id__c");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void multiSourceShape_serializesSourcesArray() {
        when(client.post(anyString(), any(Map.class), eq(Map.class)))
            .thenReturn(Map.of("rowCount", 0));

        InputSourceInput primary = dmoInput("Sales__dlm");
        primary.setPrimary(true);
        InputSourceInput related = dmoInput("Customer__dlm");

        InputSourceInput multi = new InputSourceInput();
        multi.setType("MultiSourceObjects");
        multi.setSources(List.of(primary, related));

        RowCountRequest req = new RowCountRequest();
        req.setInput(multi);

        tools.queryRowCount(req);

        ArgumentCaptor<Map> body = ArgumentCaptor.forClass(Map.class);
        verify(client).post(anyString(), body.capture(), eq(Map.class));
        Map<String, Object> input = (Map<String, Object>) body.getValue().get("input");
        assertThat(input).containsEntry("type", "MultiSourceObjects");
        assertThat(input).doesNotContainKey("source");
        assertThat(input).doesNotContainKey("dataSpace");
        List<Map<String, Object>> sources = (List<Map<String, Object>>) input.get("sources");
        assertThat(sources).hasSize(2);
        assertThat(sources.get(0)).containsEntry("type", "DataModelObject").containsEntry("primary", true);
        assertThat((Map<String, Object>) sources.get(0).get("source")).containsEntry("name", "Sales__dlm");
        assertThat(sources.get(1)).doesNotContainKey("primary");
    }
}
