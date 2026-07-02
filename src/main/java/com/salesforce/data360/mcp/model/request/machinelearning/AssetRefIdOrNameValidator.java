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
package com.salesforce.data360.mcp.model.request.machinelearning;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that an {@link AssetReferenceInput} has at least one of
 * {@code id} or {@code name} set. A null reference is treated as valid —
 * pair with {@code @NotNull} where the reference itself is required.
 */
public class AssetRefIdOrNameValidator
        implements ConstraintValidator<AssetRefIdOrName, AssetReferenceInput> {

    @Override
    public boolean isValid(AssetReferenceInput value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        boolean hasId = value.getId() != null && !value.getId().isBlank();
        boolean hasName = value.getName() != null && !value.getName().isBlank();
        return hasId || hasName;
    }
}
