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
package com.salesforce.data360.mcp.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesTest {

    @Test
    void shouldBindConfigurationProperties() {
        AppProperties props = new AppProperties();
        props.setInstanceUrl("https://test.salesforce.com");
        props.setAccessToken("test-token-123");
        props.setLoginUrl("https://test.login.salesforce.com");
        props.setClientId("test-client-id");
        props.setClientSecret("test-client-secret");
        props.setAuthFlow("oauth");
        props.setApiVersion("66.0");

        assertThat(props.getInstanceUrl()).isEqualTo("https://test.salesforce.com");
        assertThat(props.getAccessToken()).isEqualTo("test-token-123");
        assertThat(props.getLoginUrl()).isEqualTo("https://test.login.salesforce.com");
        assertThat(props.getClientId()).isEqualTo("test-client-id");
        assertThat(props.getClientSecret()).isEqualTo("test-client-secret");
        assertThat(props.getAuthFlow()).isEqualTo("oauth");
        assertThat(props.getApiVersion()).isEqualTo("66.0");
    }

    @Test
    void shouldHaveDefaultValues() {
        AppProperties props = new AppProperties();
        assertThat(props.getLoginUrl()).isEqualTo("https://login.salesforce.com");
        assertThat(props.getApiVersion()).isEqualTo("66.0");
    }
}
