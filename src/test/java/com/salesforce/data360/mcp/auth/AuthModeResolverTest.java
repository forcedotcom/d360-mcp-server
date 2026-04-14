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
package com.salesforce.data360.mcp.auth;

import com.salesforce.data360.mcp.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthModeResolverTest {

    private AppProperties properties;

    @BeforeEach
    public void setUp() {
        properties = new AppProperties();
    }

    @Test
    public void returnsAccessTokenWhenTokenAndInstanceUrlSet() {
        properties.setAccessToken("tok_123");
        properties.setInstanceUrl("https://myorg.salesforce.com");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.ACCESS_TOKEN);
    }

    @Test
    public void accessTokenTakesPriorityOverOtherFlows() {
        properties.setAccessToken("tok_123");
        properties.setInstanceUrl("https://myorg.salesforce.com");
        properties.setAuthFlow("client_credentials");
        properties.setClientId("cid");
        properties.setClientSecret("csec");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.ACCESS_TOKEN);
    }

    @Test
    public void returnsClientCredentialsWhenFlowAndCredentialsSet() {
        properties.setAuthFlow("client_credentials");
        properties.setClientId("cid");
        properties.setClientSecret("csec");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.CLIENT_CREDENTIALS);
    }

    @Test
    public void returnsNoneWhenClientCredentialsFlowMissingSecret() {
        properties.setAuthFlow("client_credentials");
        properties.setClientId("cid");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.NONE);
    }

    @Test
    public void returnsNoneWhenClientCredentialsFlowMissingClientId() {
        properties.setAuthFlow("client_credentials");
        properties.setClientSecret("csec");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.NONE);
    }

    @Test
    public void returnsNoneWhenNothingConfigured() {
        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.NONE);
    }

    @Test
    public void treatsBlankStringsAsNotSet() {
        properties.setAccessToken("   ");
        properties.setInstanceUrl("https://myorg.salesforce.com");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.NONE);
    }

    @Test
    public void treatsEmptyStringsAsNotSet() {
        properties.setAccessToken("");
        properties.setInstanceUrl("https://myorg.salesforce.com");

        assertThat(AuthModeResolver.determineAuthMode(properties))
            .isEqualTo(AuthModeResolver.NONE);
    }
}
