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

import com.salesforce.data360.mcp.auth.AuthService;
import com.salesforce.data360.mcp.client.Data360Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for REST clients used to communicate with Data 360 APIs.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public Data360Client data360Client(
        @Value("${spring.ai.mcp.server.name}") String serverName,
        @Value("${spring.ai.mcp.server.version}") String serverVersion,
        AuthService authService,
        AppProperties properties
    ) {
        return new Data360Client(
            restClientBuilder(serverName, serverVersion).build(),
            authService,
            properties.getApiVersion()
        );
    }

    static final String SFORCE_CALL_OPTIONS = "Sforce-Call-Options";

    static RestClient.Builder restClientBuilder(String serverName, String serverVersion) {
        return RestClient.builder()
            .defaultHeader(SFORCE_CALL_OPTIONS, "client=" + serverName + "/" + serverVersion);
    }
}
