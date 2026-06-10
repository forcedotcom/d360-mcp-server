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
package com.salesforce.data360.mcp.model.request.datatransform;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DataTransformBaseRequest to ensure base class inheritance works correctly
 * and covers getter/setter functionality.
 */
class DataTransformBaseRequestTest {

    // Concrete implementation for testing
    private static class TestTransformRequest extends DataTransformBaseRequest {
        // Concrete class for testing abstract base
    }

    @Test
    void testLabelGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        request.setLabel("Test Label");
        assertThat(request.getLabel()).isEqualTo("Test Label");
    }

    @Test
    void testNameGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        request.setName("test_transform");
        assertThat(request.getName()).isEqualTo("test_transform");
    }

    @Test
    void testTypeGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        request.setType("batch");
        assertThat(request.getType()).isEqualTo("batch");
    }

    @Test
    void testDescriptionGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        request.setDescription("Test description");
        assertThat(request.getDescription()).isEqualTo("Test description");
    }

    @Test
    void testDataSpaceNameGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        request.setDataSpaceName("test_dataspace");
        assertThat(request.getDataSpaceName()).isEqualTo("test_dataspace");
    }

    @Test
    void testDefinitionGetterSetter() {
        TestTransformRequest request = new TestTransformRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");

        request.setDefinition(definition);

        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDefinition().getType()).isEqualTo("DCSQL");
    }

    @Test
    void testNullValues() {
        TestTransformRequest request = new TestTransformRequest();

        assertThat(request.getLabel()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
        assertThat(request.getDefinition()).isNull();
    }

    @Test
    void testPrepareRequestInheritance() {
        DataTransformPrepareRequest prepareRequest = new DataTransformPrepareRequest();
        prepareRequest.setLabel("Prepare Test");
        prepareRequest.setType("batch");

        assertThat(prepareRequest.getLabel()).isEqualTo("Prepare Test");
        assertThat(prepareRequest.getType()).isEqualTo("batch");
    }

    @Test
    void testCreateRequestInheritance() {
        DataTransformCreateRequest createRequest = new DataTransformCreateRequest();
        createRequest.setLabel("Create Test");
        createRequest.setType("streaming");
        createRequest.setCreationType("Custom");

        assertThat(createRequest.getLabel()).isEqualTo("Create Test");
        assertThat(createRequest.getType()).isEqualTo("streaming");
        assertThat(createRequest.getCreationType()).isEqualTo("Custom");
    }

    @Test
    void testUpdateRequestInheritance() {
        DataTransformUpdateRequest updateRequest = new DataTransformUpdateRequest();
        updateRequest.setLabel("Update Test");
        updateRequest.setType("batch");
        updateRequest.setCreationType("System");

        assertThat(updateRequest.getLabel()).isEqualTo("Update Test");
        assertThat(updateRequest.getType()).isEqualTo("batch");
        assertThat(updateRequest.getCreationType()).isEqualTo("System");
    }

    @Test
    void testDataTransformDefinitionInput_ManifestGetterSetter() {
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        java.util.Map<String, Object> manifest = java.util.Map.of("nodes", java.util.Map.of());

        definition.setManifest(manifest);

        assertThat(definition.getManifest()).isNotNull();
        assertThat(definition.getManifest()).containsKey("nodes");
    }

    @Test
    void testDataTransformDefinitionInput_OutputDataObjectsGetterSetter() {
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        java.util.List<java.util.Map<String, Object>> outputDataObjects = java.util.List.of(
            java.util.Map.of("name", "test__dll")
        );

        definition.setOutputDataObjects(outputDataObjects);

        assertThat(definition.getOutputDataObjects()).isNotNull();
        assertThat(definition.getOutputDataObjects()).hasSize(1);
        assertThat(definition.getOutputDataObjects().get(0)).containsEntry("name", "test__dll");
    }

    @Test
    void testAllFieldsSetTogether() {
        TestTransformRequest request = new TestTransformRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");

        // Set all fields
        request.setLabel("Test Label");
        request.setName("test_name");
        request.setType("batch");
        request.setDefinition(definition);
        request.setDescription("Test Description");
        request.setDataSpaceName("test_dataspace");

        // Verify all fields
        assertThat(request.getLabel()).isEqualTo("Test Label");
        assertThat(request.getName()).isEqualTo("test_name");
        assertThat(request.getType()).isEqualTo("batch");
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDefinition().getType()).isEqualTo("DCSQL");
        assertThat(request.getDescription()).isEqualTo("Test Description");
        assertThat(request.getDataSpaceName()).isEqualTo("test_dataspace");
    }

    @Test
    void testOverwritingValues() {
        TestTransformRequest request = new TestTransformRequest();

        // Set initial values
        request.setLabel("Initial Label");
        request.setName("initial_name");
        assertThat(request.getLabel()).isEqualTo("Initial Label");
        assertThat(request.getName()).isEqualTo("initial_name");

        // Overwrite values
        request.setLabel("Updated Label");
        request.setName("updated_name");
        assertThat(request.getLabel()).isEqualTo("Updated Label");
        assertThat(request.getName()).isEqualTo("updated_name");
    }

    @Test
    void testSettingNullValues() {
        TestTransformRequest request = new TestTransformRequest();

        // Set values then set to null
        request.setLabel("Label");
        request.setName("name");
        request.setType("batch");
        request.setDescription("Description");
        request.setDataSpaceName("dataspace");

        request.setLabel(null);
        request.setName(null);
        request.setType(null);
        request.setDescription(null);
        request.setDataSpaceName(null);

        assertThat(request.getLabel()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
    }

    @Test
    void testDefinitionNull() {
        TestTransformRequest request = new TestTransformRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();

        request.setDefinition(definition);
        assertThat(request.getDefinition()).isNotNull();

        request.setDefinition(null);
        assertThat(request.getDefinition()).isNull();
    }

    @Test
    void testEmptyStrings() {
        TestTransformRequest request = new TestTransformRequest();

        request.setLabel("");
        request.setName("");
        request.setType("");
        request.setDescription("");
        request.setDataSpaceName("");

        assertThat(request.getLabel()).isEmpty();
        assertThat(request.getName()).isEmpty();
        assertThat(request.getType()).isEmpty();
        assertThat(request.getDescription()).isEmpty();
        assertThat(request.getDataSpaceName()).isEmpty();
    }

    @Test
    void testSpecialCharactersInStrings() {
        TestTransformRequest request = new TestTransformRequest();

        request.setLabel("Label with spaces & special chars!");
        request.setName("name_with_underscores_123");
        request.setDescription("Description\nwith\nnewlines");

        assertThat(request.getLabel()).isEqualTo("Label with spaces & special chars!");
        assertThat(request.getName()).isEqualTo("name_with_underscores_123");
        assertThat(request.getDescription()).contains("\n");
    }

    @Test
    void testPrepareRequestFullPopulation() {
        DataTransformPrepareRequest request = new DataTransformPrepareRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");
        definition.setVersion("1.0");

        request.setLabel("Prepare Request");
        request.setName("prepare_test");
        request.setType("batch");
        request.setDefinition(definition);
        request.setDescription("Full prepare request");
        request.setDataSpaceName("test_space");

        assertThat(request.getLabel()).isEqualTo("Prepare Request");
        assertThat(request.getName()).isEqualTo("prepare_test");
        assertThat(request.getType()).isEqualTo("batch");
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Full prepare request");
        assertThat(request.getDataSpaceName()).isEqualTo("test_space");
    }

    @Test
    void testCreateRequestFullPopulation() {
        DataTransformCreateRequest request = new DataTransformCreateRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("SQL");
        definition.setExpression("SELECT * FROM Account__dll");

        request.setLabel("Create Request");
        request.setName("create_test");
        request.setType("streaming");
        request.setDefinition(definition);
        request.setDescription("Full create request");
        request.setDataSpaceName("test_space");
        request.setCreationType("Custom");
        request.setCurrencyIsoCode("USD");
        request.setPrimarySource("salesforce");

        assertThat(request.getLabel()).isEqualTo("Create Request");
        assertThat(request.getName()).isEqualTo("create_test");
        assertThat(request.getType()).isEqualTo("streaming");
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Full create request");
        assertThat(request.getDataSpaceName()).isEqualTo("test_space");
        assertThat(request.getCreationType()).isEqualTo("Custom");
        assertThat(request.getCurrencyIsoCode()).isEqualTo("USD");
        assertThat(request.getPrimarySource()).isEqualTo("salesforce");
    }

    @Test
    void testUpdateRequestFullPopulation() {
        DataTransformUpdateRequest request = new DataTransformUpdateRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");

        request.setLabel("Update Request");
        request.setName("update_test");
        request.setType("batch");
        request.setDefinition(definition);
        request.setDescription("Full update request");
        request.setDataSpaceName("test_space");
        request.setCreationType("System");
        request.setCurrencyIsoCode("EUR");
        request.setPrimarySource("external");

        assertThat(request.getLabel()).isEqualTo("Update Request");
        assertThat(request.getName()).isEqualTo("update_test");
        assertThat(request.getType()).isEqualTo("batch");
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Full update request");
        assertThat(request.getDataSpaceName()).isEqualTo("test_space");
        assertThat(request.getCreationType()).isEqualTo("System");
        assertThat(request.getCurrencyIsoCode()).isEqualTo("EUR");
        assertThat(request.getPrimarySource()).isEqualTo("external");
    }

    @Test
    void testLabelSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        // Test with regular string
        request.setLabel("Label1");
        assertThat(request.getLabel()).isEqualTo("Label1");

        // Test with another value
        request.setLabel("Label2");
        assertThat(request.getLabel()).isEqualTo("Label2");

        // Test with null
        request.setLabel(null);
        assertThat(request.getLabel()).isNull();
    }

    @Test
    void testNameSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        request.setName("name1");
        assertThat(request.getName()).isEqualTo("name1");

        request.setName("name2");
        assertThat(request.getName()).isEqualTo("name2");

        request.setName(null);
        assertThat(request.getName()).isNull();
    }

    @Test
    void testTypeSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        request.setType("batch");
        assertThat(request.getType()).isEqualTo("batch");

        request.setType("streaming");
        assertThat(request.getType()).isEqualTo("streaming");

        request.setType(null);
        assertThat(request.getType()).isNull();
    }

    @Test
    void testDescriptionSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        request.setDescription("desc1");
        assertThat(request.getDescription()).isEqualTo("desc1");

        request.setDescription("desc2");
        assertThat(request.getDescription()).isEqualTo("desc2");

        request.setDescription(null);
        assertThat(request.getDescription()).isNull();
    }

    @Test
    void testDataSpaceNameSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        request.setDataSpaceName("space1");
        assertThat(request.getDataSpaceName()).isEqualTo("space1");

        request.setDataSpaceName("space2");
        assertThat(request.getDataSpaceName()).isEqualTo("space2");

        request.setDataSpaceName(null);
        assertThat(request.getDataSpaceName()).isNull();
    }

    @Test
    void testDefinitionSetterWithDifferentValues() {
        TestTransformRequest request = new TestTransformRequest();

        DataTransformDefinitionInput def1 = new DataTransformDefinitionInput();
        def1.setType("DCSQL");
        request.setDefinition(def1);
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDefinition().getType()).isEqualTo("DCSQL");

        DataTransformDefinitionInput def2 = new DataTransformDefinitionInput();
        def2.setType("SQL");
        request.setDefinition(def2);
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDefinition().getType()).isEqualTo("SQL");

        request.setDefinition(null);
        assertThat(request.getDefinition()).isNull();
    }

    @Test
    void testChainedSetters() {
        TestTransformRequest request = new TestTransformRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();
        definition.setType("DCSQL");

        // Chain multiple setters
        request.setLabel("Chained Label");
        request.setName("chained_name");
        request.setType("batch");
        request.setDefinition(definition);
        request.setDescription("Chained desc");
        request.setDataSpaceName("chained_space");

        // Verify all were set
        assertThat(request.getLabel()).isNotNull();
        assertThat(request.getName()).isNotNull();
        assertThat(request.getType()).isNotNull();
        assertThat(request.getDefinition()).isNotNull();
        assertThat(request.getDescription()).isNotNull();
        assertThat(request.getDataSpaceName()).isNotNull();
    }

    @Test
    void testGettersReturnSetValues() {
        TestTransformRequest request = new TestTransformRequest();

        String testLabel = "Test Label Value";
        String testName = "test_name_value";
        String testType = "batch_type";
        String testDesc = "Test Description Value";
        String testSpace = "test_space_value";

        request.setLabel(testLabel);
        request.setName(testName);
        request.setType(testType);
        request.setDescription(testDesc);
        request.setDataSpaceName(testSpace);

        // Verify getters return exact values
        assertThat(request.getLabel()).isSameAs(testLabel);
        assertThat(request.getName()).isSameAs(testName);
        assertThat(request.getType()).isSameAs(testType);
        assertThat(request.getDescription()).isSameAs(testDesc);
        assertThat(request.getDataSpaceName()).isSameAs(testSpace);
    }

    @Test
    void testDefinitionGetterReturnsSetValue() {
        TestTransformRequest request = new TestTransformRequest();
        DataTransformDefinitionInput definition = new DataTransformDefinitionInput();

        request.setDefinition(definition);

        // Verify getter returns exact object
        assertThat(request.getDefinition()).isSameAs(definition);
    }

    @Test
    void testUpdateRequestPartialUpdate_OnlyDescription() {
        // Test that update requests can set only one field (partial update)
        DataTransformUpdateRequest request = new DataTransformUpdateRequest();

        // Set only description - all other fields remain null
        request.setDescription("Updated description only");

        // Verify only description is set
        assertThat(request.getDescription()).isEqualTo("Updated description only");
        assertThat(request.getLabel()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDefinition()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
    }

    @Test
    void testUpdateRequestPartialUpdate_OnlyLabel() {
        // Test that update requests can set only label
        DataTransformUpdateRequest request = new DataTransformUpdateRequest();

        request.setLabel("Updated Label");

        // Verify only label is set
        assertThat(request.getLabel()).isEqualTo("Updated Label");
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDefinition()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
    }

    @Test
    void testUpdateRequestPartialUpdate_MultipleFieldsSubset() {
        // Test that update requests can set a subset of fields
        DataTransformUpdateRequest request = new DataTransformUpdateRequest();

        request.setLabel("New Label");
        request.setDescription("New Description");

        // Verify only set fields have values
        assertThat(request.getLabel()).isEqualTo("New Label");
        assertThat(request.getDescription()).isEqualTo("New Description");
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDefinition()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
    }

    @Test
    void testUpdateRequestAllFieldsOptional() {
        // Verify all inherited and own fields are optional by leaving them null
        DataTransformUpdateRequest request = new DataTransformUpdateRequest();

        // All fields should be null (no required field violations)
        assertThat(request.getLabel()).isNull();
        assertThat(request.getName()).isNull();
        assertThat(request.getType()).isNull();
        assertThat(request.getDefinition()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getDataSpaceName()).isNull();
        assertThat(request.getCreationType()).isNull();
        assertThat(request.getCurrencyIsoCode()).isNull();
        assertThat(request.getPrimarySource()).isNull();
    }
}
