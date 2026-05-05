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

import com.salesforce.data360.mcp.model.auth.OAuthTokenResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SfCliAuthTest {

    private static final String OK_JSON = """
        {"status":0,"result":{"accessToken":"00D_test_token","instanceUrl":"https://sftutor.my.salesforce.com","username":"me@example.com"}}
        """;

    @Test
    void parsesAccessTokenAndInstanceUrlFromSfCliOutput() {
        SfCliAuth auth = new SfCliAuth("sftutor",
            cmd -> new SfCliAuth.ProcessResult(0, OK_JSON, ""));

        OAuthTokenResponse response = auth.authenticate();

        assertThat(response.accessToken()).isEqualTo("00D_test_token");
        assertThat(response.instanceUrl()).isEqualTo("https://sftutor.my.salesforce.com");
        assertThat(response.expiresIn()).isEqualTo(SfCliAuth.CACHE_TTL_SECONDS);
    }

    @Test
    void invokesSfCliWithExpectedArguments() {
        java.util.concurrent.atomic.AtomicReference<List<String>> captured =
            new java.util.concurrent.atomic.AtomicReference<>();
        SfCliAuth auth = new SfCliAuth("my-alias", cmd -> {
            captured.set(cmd);
            return new SfCliAuth.ProcessResult(0, OK_JSON, "");
        });

        auth.authenticate();

        assertThat(captured.get())
            .containsExactly("sf", "org", "display", "--target-org", "my-alias", "--json");
    }

    @Test
    void throwsWhenCliExitsNonZero() {
        SfCliAuth auth = new SfCliAuth("missing",
            cmd -> new SfCliAuth.ProcessResult(1, "", "No org for alias"));

        assertThatThrownBy(auth::authenticate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sf CLI failed")
            .hasMessageContaining("missing")
            .hasMessageContaining("No org for alias");
    }

    @Test
    void throwsWhenOutputIsNotJson() {
        SfCliAuth auth = new SfCliAuth("sftutor",
            cmd -> new SfCliAuth.ProcessResult(0, "not-json", ""));

        assertThatThrownBy(auth::authenticate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid JSON");
    }

    @Test
    void throwsWhenRequiredFieldsMissing() {
        SfCliAuth auth = new SfCliAuth("sftutor",
            cmd -> new SfCliAuth.ProcessResult(0, "{\"status\":0,\"result\":{}}", ""));

        assertThatThrownBy(auth::authenticate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("missing accessToken or instanceUrl");
    }
}
