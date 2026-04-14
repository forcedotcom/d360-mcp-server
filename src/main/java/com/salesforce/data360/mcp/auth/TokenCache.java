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

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory token cache with TTL expiration.
 */
public class TokenCache {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private String accessToken;
    private String instanceUrl;
    private Instant expiresAt;

    /**
     * Store access token with a specific TTL duration (e.g., from OAuth expires_in).
     */
    public void store(String accessToken, String instanceUrl, Duration ttl) {
        lock.writeLock().lock();
        try {
            this.accessToken = accessToken;
            this.instanceUrl = instanceUrl;
            this.expiresAt = Instant.now().plus(ttl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieve cached access token if present and not expired.
     */
    public Optional<String> getAccessToken() {
        lock.readLock().lock();
        try {
            if (isValid()) {
                return Optional.of(accessToken);
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieve cached instance URL if present and not expired.
     */
    public Optional<String> getInstanceUrl() {
        lock.readLock().lock();
        try {
            if (isValid()) {
                return Optional.of(instanceUrl);
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clear all cached data.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            this.accessToken = null;
            this.instanceUrl = null;
            this.expiresAt = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isValid() {
        return accessToken != null
            && instanceUrl != null
            && expiresAt != null
            && Instant.now().isBefore(expiresAt);
    }
}
