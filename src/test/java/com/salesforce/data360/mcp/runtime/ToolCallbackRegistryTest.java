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
package com.salesforce.data360.mcp.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.data360.mcp.client.Data360Client;
import com.salesforce.data360.mcp.tools.datastream.DataStreamTools;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolCallbackRegistryTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private Data360Client client;

    private ObjectMapper objectMapper;
    private Validator validator;
    private ToolCallbackRegistry registry;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void invoke_knownTool_returnsResult() throws Exception {
        // Given
        TestToolBean testBean = new TestToolBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        String paramsJson = "{\"name\":\"World\",\"count\":3,\"active\":true}";

        // When
        String result = registry.invoke("test_tool", paramsJson);

        // Then
        assertThat(result).isEqualTo("Hello World x3");
    }

    @Test
    void invoke_unknownTool_throwsIllegalArgument() {
        // Given
        TestToolBean testBean = new TestToolBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        // When / Then
        assertThatThrownBy(() -> registry.invoke("unknown_tool", "{}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown tool: unknown_tool");
    }

    @Test
    void getSchema_returnsCorrectTypes() {
        // Given
        TestToolBean testBean = new TestToolBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        // When
        Map<String, Object> schema = registry.getSchema("test_tool");

        // Then
        assertThat(schema).isNotNull();
        assertThat(schema.get("type")).isEqualTo("object");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertThat(properties).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> nameProperty = (Map<String, Object>) properties.get("name");
        assertThat(nameProperty.get("type")).isEqualTo("string");
        assertThat(nameProperty.get("description")).isEqualTo("The name to greet");

        @SuppressWarnings("unchecked")
        Map<String, Object> countProperty = (Map<String, Object>) properties.get("count");
        assertThat(countProperty.get("type")).isEqualTo("integer");
        assertThat(countProperty.get("description")).isEqualTo("How many times");

        @SuppressWarnings("unchecked")
        Map<String, Object> activeProperty = (Map<String, Object>) properties.get("active");
        assertThat(activeProperty.get("type")).isEqualTo("boolean");
        assertThat(activeProperty.get("description")).isEqualTo("Is active");
    }

    @Test
    void getSchema_unknownTool_returnsNull() {
        // Given
        TestToolBean testBean = new TestToolBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        // When
        Map<String, Object> schema = registry.getSchema("unknown_tool");

        // Then
        assertThat(schema).isNull();
    }

    @Test
    void duplicateToolName_throwsIllegalState() {
        // Given
        TestToolBean testBean1 = new TestToolBean();
        DuplicateToolBean testBean2 = new DuplicateToolBean();

        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean1", "testBean2"});
        when(context.getType("testBean1")).thenReturn((Class) DataStreamTools.class);
        when(context.getType("testBean2")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean1")).thenReturn(testBean1);
        when(context.getBean("testBean2")).thenReturn(testBean2);

        // When / Then
        assertThatThrownBy(() -> new ToolCallbackRegistry(context, objectMapper, validator))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Duplicate tool name 'test_tool'")
            .hasMessageContaining("TestToolBean")
            .hasMessageContaining("DuplicateToolBean");
    }

    @Test
    void invoke_withValidatedRequest_valid_succeeds() throws Exception {
        // Given
        ValidationTestBean testBean = new ValidationTestBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        String paramsJson = "{\"request\":{\"name\":\"Test\",\"label\":\"Test Label\"}}";

        // When
        String result = registry.invoke("validation_tool", paramsJson);

        // Then
        assertThat(result).contains("Test");
        assertThat(result).contains("Test Label");
    }

    @Test
    void invoke_withValidatedRequest_missingRequired_throwsValidation() {
        // Given
        ValidationTestBean testBean = new ValidationTestBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        String paramsJson = "{\"request\":{\"label\":\"Test Label\"}}"; // missing required 'name'

        // When / Then
        assertThatThrownBy(() -> registry.invoke("validation_tool", paramsJson))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validation failed")
            .hasMessageContaining("name");
    }

    @Test
    void invoke_withValidatedRequest_blankRequired_throwsValidation() {
        // Given
        ValidationTestBean testBean = new ValidationTestBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        String paramsJson = "{\"request\":{\"name\":\"\",\"label\":\"Test Label\"}}"; // blank name

        // When / Then
        assertThatThrownBy(() -> registry.invoke("validation_tool", paramsJson))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validation failed")
            .hasMessageContaining("name");
    }

    @Test
    void invoke_withMissingTopLevelRequest_throwsValidation() {
        ValidationTestBean testBean = new ValidationTestBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        assertThatThrownBy(() -> registry.invoke("validation_tool", "{}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Missing required parameter: request");
    }

    @Test
    void invoke_realDataStreamTool_invalidNestedConnector_rejectedBeforeApiCall() {
        DataStreamTools dataStreamTools = new DataStreamTools(client);
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"dataStreamTools"});
        when(context.getType("dataStreamTools")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("dataStreamTools")).thenReturn(dataStreamTools);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        String paramsJson = "{\"request\":{\"name\":\"TestStream\",\"label\":\"Test Stream\",\"connectorInfo\":{\"connectorDetails\":{}}}}";

        assertThatThrownBy(() -> registry.invoke("d360_datastream_create", paramsJson))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("connectorInfo.connectorType");

        verifyNoInteractions(client);
    }

    @Test
    void getSchema_withNestedObject_generatesNestedSchema() {
        // Given
        SchemaTestBean testBean = new SchemaTestBean();
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"testBean"});
        when(context.getType("testBean")).thenReturn((Class) DataStreamTools.class);
        when(context.getBean("testBean")).thenReturn(testBean);

        registry = new ToolCallbackRegistry(context, objectMapper, validator);

        // When
        Map<String, Object> schema = registry.getSchema("schema_tool");

        // Then
        assertThat(schema).isNotNull();
        assertThat(schema.get("type")).isEqualTo("object");

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertThat(properties).containsKey("request");

        @SuppressWarnings("unchecked")
        Map<String, Object> requestProperty = (Map<String, Object>) properties.get("request");
        assertThat(requestProperty.get("type")).isEqualTo("object");
        assertThat(requestProperty.get("description")).isEqualTo("Schema envelope");

        @SuppressWarnings("unchecked")
        Map<String, Object> nestedProperties = (Map<String, Object>) requestProperty.get("properties");
        assertThat(nestedProperties).containsKeys("name", "children", "baseField");

        @SuppressWarnings("unchecked")
        Map<String, Object> nameProperty = (Map<String, Object>) nestedProperties.get("name");
        assertThat(nameProperty.get("type")).isEqualTo("string");
        assertThat(nameProperty.get("description")).isEqualTo("Envelope name");

        @SuppressWarnings("unchecked")
        Map<String, Object> childrenProperty = (Map<String, Object>) nestedProperties.get("children");
        assertThat(childrenProperty.get("type")).isEqualTo("array");
        assertThat(childrenProperty.get("description")).isEqualTo("Child items");

        @SuppressWarnings("unchecked")
        Map<String, Object> childItemSchema = (Map<String, Object>) childrenProperty.get("items");
        assertThat(childItemSchema.get("type")).isEqualTo("object");

        @SuppressWarnings("unchecked")
        Map<String, Object> childProperties = (Map<String, Object>) childItemSchema.get("properties");
        assertThat(childProperties).containsKey("childLabel");

        @SuppressWarnings("unchecked")
        Map<String, Object> childLabelProperty = (Map<String, Object>) childProperties.get("childLabel");
        assertThat(childLabelProperty.get("type")).isEqualTo("string");
        assertThat(childLabelProperty.get("description")).isEqualTo("Child label");

        @SuppressWarnings("unchecked")
        Map<String, Object> baseFieldProperty = (Map<String, Object>) nestedProperties.get("baseField");
        assertThat(baseFieldProperty.get("type")).isEqualTo("string");
        assertThat(baseFieldProperty.get("description")).isEqualTo("Base field");

        // Check required fields
        @SuppressWarnings("unchecked")
        java.util.List<String> requiredFields = (java.util.List<String>) requestProperty.get("required");
        assertThat(requiredFields).contains("name");
        assertThat(requiredFields).doesNotContain("children", "baseField");
    }

    // ── Test Helper Beans ──────────────────────────────────────────────────

    public static class TestToolBean {
        @McpTool(name = "test_tool", description = "A test tool")
        public String testMethod(
            @McpToolParam(description = "The name to greet") String name,
            @McpToolParam(description = "How many times") Integer count,
            @McpToolParam(description = "Is active") Boolean active
        ) {
            return "Hello " + name + " x" + count;
        }
    }

    public static class DuplicateToolBean {
        @McpTool(name = "test_tool", description = "A duplicate test tool")
        public String anotherMethod(
            @McpToolParam(description = "Some param") String param
        ) {
            return "duplicate";
        }
    }

    public static class ValidationTestBean {
        @McpTool(name = "validation_tool", description = "A tool with validated request")
        public String validationMethod(
            @McpToolParam(description = "The validated request") TestValidatedRequest request
        ) {
            return "Created: " + request.getName() + " - " + request.getLabel();
        }
    }

    public static class SchemaTestBean {
        @McpTool(name = "schema_tool", description = "A tool with nested schema")
        public String schemaMethod(
            @McpToolParam(description = "Schema envelope") SchemaEnvelope request
        ) {
            return "ok";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestValidatedRequest {
        @NotBlank
        private String name;

        @NotBlank
        private String label;

        private String optionalField;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getOptionalField() {
            return optionalField;
        }

        public void setOptionalField(String optionalField) {
            this.optionalField = optionalField;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchemaBase {
        @McpToolParam(description = "Base field")
        private String baseField;

        public String getBaseField() {
            return baseField;
        }

        public void setBaseField(String baseField) {
            this.baseField = baseField;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchemaEnvelope extends SchemaBase {
        @NotBlank
        @McpToolParam(description = "Envelope name")
        private String name;

        @McpToolParam(description = "Child items")
        private List<SchemaChild> children;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<SchemaChild> getChildren() {
            return children;
        }

        public void setChildren(List<SchemaChild> children) {
            this.children = children;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SchemaChild {
        @McpToolParam(description = "Child label")
        private String childLabel;

        public String getChildLabel() {
            return childLabel;
        }

        public void setChildLabel(String childLabel) {
            this.childLabel = childLabel;
        }
    }
}
