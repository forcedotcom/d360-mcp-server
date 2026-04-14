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
package com.salesforce.data360.mcp.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUtilTest {

    @Test
    void shouldSerializeObjectToJson() {
        TestObject obj = new TestObject("test-name", 42);
        String json = JsonUtil.toJson(obj);

        assertThat(json).contains("test-name");
        assertThat(json).contains("42");
    }

    @Test
    void shouldDeserializeJsonToObject() {
        String json = "{\"name\":\"test-name\",\"value\":42}";
        TestObject obj = JsonUtil.fromJson(json, TestObject.class);

        assertThat(obj.getName()).isEqualTo("test-name");
        assertThat(obj.getValue()).isEqualTo(42);
    }

    @Test
    void shouldNotFailOnUnknownProperties() {
        String jsonWithExtra = "{\"name\":\"test\",\"value\":42,\"unknown\":\"extra\"}";
        TestObject obj = JsonUtil.fromJson(jsonWithExtra, TestObject.class);

        assertThat(obj.getName()).isEqualTo("test");
        assertThat(obj.getValue()).isEqualTo(42);
    }

    @Test
    void shouldNotFailOnEmptyBeans() {
        EmptyBean empty = new EmptyBean();
        String json = JsonUtil.toJson(empty);
        assertThat(json).isNotNull();
    }

    @Test
    void shouldHandleNullValue() {
        String json = JsonUtil.toJson(null);
        assertThat(json).isEqualTo("null");
    }

    @Test
    void shouldThrowExceptionOnInvalidJson() {
        assertThatThrownBy(() -> JsonUtil.fromJson("invalid-json", TestObject.class))
            .isInstanceOf(RuntimeException.class);
    }

    // Test classes
    public static class TestObject {
        private String name;
        private int value;

        public TestObject() {}

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class EmptyBean {
        // Empty class to test FAIL_ON_EMPTY_BEANS=false
    }
}
