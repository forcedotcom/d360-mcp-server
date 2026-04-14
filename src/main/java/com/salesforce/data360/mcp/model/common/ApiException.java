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
package com.salesforce.data360.mcp.model.common;

/**
 * Exception thrown for Data 360 API failures — both HTTP error responses
 * and lower-level connection errors.
 *
 * <p>HTTP errors carry a non-zero {@link #getStatusCode()} and the request path;
 * connection errors set {@code statusCode = 0} and leave the path null.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;
    private final String requestPath;

    public ApiException(int statusCode, String responseBody, String requestPath) {
        super(String.format("Data 360 API error %d on %s: %s", statusCode, requestPath, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.requestPath = requestPath;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
        this.requestPath = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getRequestPath() {
        return requestPath;
    }
}
