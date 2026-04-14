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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import jakarta.validation.Validator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Discovers all @McpTool methods from Spring beans and provides invocation + schema.
 * Uses reflection to bridge the Spring AI 1.1.4 (@Tool) / 2.0.0-M4 (@McpTool) gap.
 *
 * <p>This registry depends on {@link ApplicationContext} and iterates every bean at
 * construction time. Consumers that are themselves @McpTool-annotated (notably
 * {@code ExecuteTool} and {@code PayloadExamplesTool}) therefore have a cyclic
 * dependency: the registry scans them, and they depend on the registry. Those
 * consumers must inject this bean with {@code @Lazy} to break the cycle.
 */
@Component
public class ToolCallbackRegistry {

    public record ToolEntry(Object bean, Method method, String name, String description, List<ParamEntry> params) {}
    public record ParamEntry(String name, String description, Class<?> type, boolean required) {}

    private static final Logger log = LoggerFactory.getLogger(ToolCallbackRegistry.class);
    private static final String TOOLS_PACKAGE = "com.salesforce.data360.mcp.tools.";

    private final Map<String, ToolEntry> entries = new LinkedHashMap<>();
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ToolCallbackRegistry(ApplicationContext context, ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        for (String beanName : context.getBeanDefinitionNames()) {
            Class<?> type = context.getType(beanName);
            if (type == null || !type.getName().startsWith(TOOLS_PACKAGE)) {
                continue;
            }
            Object bean;
            try {
                bean = context.getBean(beanName);
            } catch (Exception e) {
                log.warn("Failed to get bean '{}': {}", beanName, e.getMessage());
                continue;
            }

            for (Method m : bean.getClass().getMethods()) {
                McpTool ann = m.getAnnotation(McpTool.class);
                if (ann != null) {
                    List<ParamEntry> params = extractParams(m);
                    ToolEntry existing = entries.get(ann.name());
                    if (existing != null) {
                        throw new IllegalStateException(
                            "Duplicate tool name '" + ann.name() + "': declared in both "
                            + existing.bean().getClass().getName() + " and "
                            + bean.getClass().getName());
                    }
                    entries.put(ann.name(), new ToolEntry(bean, m, ann.name(), ann.description(), params));
                    log.info("Registered tool: {} from bean: {}", ann.name(), beanName);
                }
            }
        }
        log.info("ToolCallbackRegistry: registered {} tools total", entries.size());
    }

    /** Invoke a tool by name with a JSON params string. */
    public String invoke(String toolName, String paramsJson) throws Exception {
        ToolEntry entry = entries.get(toolName);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }

        Map<String, Object> params = paramsJson != null && !paramsJson.isBlank()
            ? objectMapper.readValue(paramsJson, Map.class)
            : Map.of();

        Object result = invokeForResult(toolName, params);
        if (result == null) {
            return "{}";
        }
        return result instanceof String s ? s : objectMapper.writeValueAsString(result);
    }

    public Object invokeForResult(String toolName, Map<String, Object> params) throws Exception {
        ToolEntry entry = entries.get(toolName);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }

        Object[] args = buildArgs(entry, params != null ? params : Map.of());
        return entry.method().invoke(entry.bean(), args);
    }

    /** Get the schema (param names, types, descriptions, required flags) for a tool. */
    public Map<String, Object> getSchema(String toolName) {
        ToolEntry entry = entries.get(toolName);
        if (entry == null) return null;
        SchemaDefinition schemaDefinition = buildSchemaDefinition(entry);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        schema.put("properties", schemaDefinition.properties());
        if (!schemaDefinition.required().isEmpty()) schema.put("required", schemaDefinition.required());
        return schema;
    }

    public McpSchema.JsonSchema getJsonSchema(String toolName) {
        ToolEntry entry = entries.get(toolName);
        if (entry == null) return null;

        SchemaDefinition schemaDefinition = buildSchemaDefinition(entry);
        return new McpSchema.JsonSchema(
            "object", schemaDefinition.properties(), schemaDefinition.required(), null, null, null);
    }

    public ToolEntry get(String toolName) { return entries.get(toolName); }
    public int size() { return entries.size(); }

    private record SchemaDefinition(Map<String, Object> properties, List<String> required) {}

    private static SchemaDefinition buildSchemaDefinition(ToolEntry entry) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ParamEntry p : entry.params()) {
            Map<String, Object> prop = buildPropertySchema(p.type(), null, p.description(), new LinkedHashSet<>());
            properties.put(p.name(), prop);
            if (p.required()) required.add(p.name());
        }

        return new SchemaDefinition(properties, required);
    }

    private static List<ParamEntry> extractParams(Method method) {
        List<ParamEntry> params = new ArrayList<>();
        for (Parameter p : method.getParameters()) {
            McpToolParam ann = p.getAnnotation(McpToolParam.class);
            String name = ann != null ? paramName(p, ann) : p.getName();
            String desc = ann != null ? ann.description() : "";
            boolean required = ann == null || ann.required();
            params.add(new ParamEntry(name, desc, p.getType(), required));
        }
        return params;
    }

    private static String paramName(Parameter p, McpToolParam ann) {
        // @McpToolParam doesn't have a name() — use the parameter name from bytecode
        return p.getName();
    }

    private Object[] buildArgs(ToolEntry entry, Map<String, Object> params) {
        Object[] args = new Object[entry.params().size()];
        for (int i = 0; i < entry.params().size(); i++) {
            ParamEntry pe = entry.params().get(i);
            Object value = params.get(pe.name());

            if (value == null && !pe.required()) {
                args[i] = null;
            } else if (value == null) {
                throw new IllegalArgumentException("Missing required parameter: " + pe.name());
            } else {
                args[i] = convertParam(value, pe.type());
                // Validate POJO arguments
                if (args[i] != null && !isPrimitiveOrCommon(pe.type())) {
                    Set<ConstraintViolation<Object>> violations = validator.validate(args[i]);
                    if (!violations.isEmpty()) {
                        String msg = violations.stream()
                            .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .collect(Collectors.joining(", "));
                        throw new IllegalArgumentException("Validation failed for parameter '" + pe.name() + "': " + msg);
                    }
                }
            }
        }
        return args;
    }

    private static boolean isPrimitiveOrCommon(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type)
            || type == Boolean.class || Map.class.isAssignableFrom(type)
            || List.class.isAssignableFrom(type);
    }

    private Object convertParam(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) return value;
        if (targetType == String.class) return value.toString();
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value.toString());
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value.toString());
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value.toString());
        if (targetType == Double.class || targetType == double.class) return Double.parseDouble(value.toString());
        // For POJOs/Maps — use Jackson to deserialize
        return objectMapper.convertValue(value, targetType);
    }

    private static Map<String, Object> buildPropertySchema(
        Class<?> type,
        Type genericType,
        String description,
        Set<Class<?>> visited
    ) {
        Map<String, Object> schema = new LinkedHashMap<>();
        Object typeOrSchema = jsonTypeOrSchema(type, genericType, visited);
        if (typeOrSchema instanceof String s) {
            schema.put("type", s);
        } else if (typeOrSchema instanceof Map) {
            schema.putAll((Map<String, Object>) typeOrSchema);
        }
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        return schema;
    }

    private static Object jsonTypeOrSchema(Class<?> type, Type genericType, Set<Class<?>> visited) {
        if (type == String.class) return "string";
        if (type == Integer.class || type == int.class || type == Long.class || type == long.class) return "integer";
        if (type == Double.class || type == double.class || type == Float.class || type == float.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        if (Map.class.isAssignableFrom(type)) return "object";
        if (type.isArray() || List.class.isAssignableFrom(type)) {
            return buildArraySchema(type, genericType, visited);
        }
        // POJO — generate nested schema
        return buildNestedSchema(type, visited);
    }

    private static Map<String, Object> buildArraySchema(Class<?> type, Type genericType, Set<Class<?>> visited) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");

        Type itemType = resolveItemType(type, genericType);
        Class<?> itemClass = rawClass(itemType);
        if (itemClass != null) {
            Object itemSchema = jsonTypeOrSchema(itemClass, itemType, new LinkedHashSet<>(visited));
            Map<String, Object> items = new LinkedHashMap<>();
            if (itemSchema instanceof String s) {
                items.put("type", s);
            } else if (itemSchema instanceof Map) {
                items.putAll((Map<String, Object>) itemSchema);
            }
            schema.put("items", items);
        }
        return schema;
    }

    private static Map<String, Object> buildNestedSchema(Class<?> type, Set<Class<?>> visited) {
        if (!visited.add(type)) {
            return Map.of("type", "object");
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        try {
            for (Field field : allFields(type)) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;

                McpToolParam param = field.getAnnotation(McpToolParam.class);
                String description = param != null ? param.description() : null;
                String fieldName = jsonFieldName(field);

                Map<String, Object> fieldSchema = buildPropertySchema(
                    field.getType(),
                    field.getGenericType(),
                    description,
                    new LinkedHashSet<>(visited));
                properties.put(fieldName, fieldSchema);

                // Check for validation annotations
                if (field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class)
                    || field.isAnnotationPresent(jakarta.validation.constraints.NotBlank.class)
                    || field.isAnnotationPresent(jakarta.validation.constraints.NotEmpty.class)) {
                    required.add(fieldName);
                }
            }
        } finally {
            visited.remove(type);
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) schema.put("required", required);
        return schema;
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            fields.addAll(0, Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String jsonFieldName(Field field) {
        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && jsonProperty.value() != null && !jsonProperty.value().isBlank()) {
            return jsonProperty.value();
        }
        return field.getName();
    }

    private static Type resolveItemType(Class<?> type, Type genericType) {
        if (type.isArray()) {
            return type.getComponentType();
        }
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return actualTypeArguments[0];
            }
        }
        return null;
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType() instanceof Class<?> clazz) {
            return clazz;
        }
        return null;
    }
}
