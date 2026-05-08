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
import com.salesforce.data360.mcp.client.ClientContext;
import com.salesforce.data360.mcp.client.Data360Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestClientConfigTest {

    @AfterEach
    void tearDown() {
        ClientContext.clear();
    }

    @Test
    public void requestIncludesServerCallOptions() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        AuthService authService = mock(AuthService.class);
        when(authService.getInstanceUrl()).thenReturn("https://example.test");
        when(authService.getAccessToken()).thenReturn("token");

        Data360Client client = new Data360Client(builder.build(), authService, "66.0",
            "client=data360-mcp-server-oss/1.0.0");

        server.expect(requestTo("https://example.test/services/data/v66.0/ssot/test"))
            .andExpect(header("Sforce-Call-Options", "client=data360-mcp-server-oss/1.0.0"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.get("/ssot/test", String.class);
        server.verify();
    }

    @Test
    public void requestIncludesMcpClientInfoWhenPresent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        AuthService authService = mock(AuthService.class);
        when(authService.getInstanceUrl()).thenReturn("https://example.test");
        when(authService.getAccessToken()).thenReturn("token");

        Data360Client client = new Data360Client(builder.build(), authService, "66.0",
            "client=data360-mcp-server-oss/1.0.0");

        ClientContext.set("claude-code/1.0.31");

        server.expect(requestTo("https://example.test/services/data/v66.0/ssot/test"))
            .andExpect(header("Sforce-Call-Options",
                "client=data360-mcp-server-oss/1.0.0/claude-code/1.0.31"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.get("/ssot/test", String.class);
        server.verify();
    }
}
