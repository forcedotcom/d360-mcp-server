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

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCacheTest {

    @Test
    void returnsStoredToken() {
        TokenCache cache = new TokenCache();
        cache.store("test-token", "https://test.salesforce.com", Duration.ofMinutes(10));

        assertThat(cache.getAccessToken()).isEqualTo(Optional.of("test-token"));
        assertThat(cache.getInstanceUrl()).isEqualTo(Optional.of("https://test.salesforce.com"));
    }

    @Test
    void returnsEmptyWhenNothingStored() {
        TokenCache cache = new TokenCache();

        assertThat(cache.getAccessToken()).isEmpty();
        assertThat(cache.getInstanceUrl()).isEmpty();
    }

    @Test
    void expiresAfterTTL() throws InterruptedException {
        TokenCache cache = new TokenCache();
        cache.store("test-token", "https://test.salesforce.com", Duration.ofMillis(50));

        assertThat(cache.getAccessToken()).isPresent();

        Thread.sleep(100);

        assertThat(cache.getAccessToken()).isEmpty();
        assertThat(cache.getInstanceUrl()).isEmpty();
    }

    @Test
    void clearRemovesToken() {
        TokenCache cache = new TokenCache();
        cache.store("test-token", "https://test.salesforce.com", Duration.ofMinutes(10));

        assertThat(cache.getAccessToken()).isPresent();

        cache.clear();

        assertThat(cache.getAccessToken()).isEmpty();
        assertThat(cache.getInstanceUrl()).isEmpty();
    }
}
