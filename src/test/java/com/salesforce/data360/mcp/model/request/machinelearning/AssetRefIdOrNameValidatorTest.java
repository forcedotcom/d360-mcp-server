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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AssetRefIdOrNameValidatorTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) factory.close();
    }

    @Test
    void emptyRef_violates() {
        Set<ConstraintViolation<AssetReferenceInput>> violations = validator.validate(new AssetReferenceInput());
        assertThat(violations)
            .singleElement()
            .satisfies(v -> assertThat(v.getMessage()).contains("id or name"));
    }

    @Test
    void blankIdAndName_violates() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setId("   ");
        ref.setName("");
        Set<ConstraintViolation<AssetReferenceInput>> violations = validator.validate(ref);
        assertThat(violations).hasSize(1);
    }

    @Test
    void idOnly_passes() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setId("12lSG0000005K7lYAE");
        assertThat(validator.validate(ref)).isEmpty();
    }

    @Test
    void nameOnly_passes() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setName("my_model");
        assertThat(validator.validate(ref)).isEmpty();
    }

    @Test
    void both_passes() {
        AssetReferenceInput ref = new AssetReferenceInput();
        ref.setId("12lSG0000005K7lYAE");
        ref.setName("my_model");
        assertThat(validator.validate(ref)).isEmpty();
    }
}
