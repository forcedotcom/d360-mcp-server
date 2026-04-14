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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestClientConfigTest {

    @Test
    public void restClientSendsUserAgentHeader() {
        RestClient.Builder builder = RestClientConfig.restClientBuilder(
            "data360-mcp-server-oss", "1.0.0"
        );
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();

        server.expect(requestTo("https://example.test/ping"))
            .andExpect(header(HttpHeaders.USER_AGENT, "data360-mcp-server-oss/1.0.0"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.get().uri("https://example.test/ping").retrieve().toBodilessEntity();

        server.verify();
    }
}
