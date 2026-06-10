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
package com.salesforce.data360.mcp.model.request.datatransform;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request body for preparing/validating a data transform before creation.
 * Similar to DataTransformCreateRequest but without outputDataObjects (will be generated).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTransformPrepareRequest extends DataTransformBaseRequest {
    // All common fields inherited from DataTransformBaseRequest
    // This class exists to distinguish prepare requests from create requests semantically
}
