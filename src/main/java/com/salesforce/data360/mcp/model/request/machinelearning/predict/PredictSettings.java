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
package com.salesforce.data360.mcp.model.request.machinelearning.predict;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.ai.mcp.annotation.McpToolParam;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictSettings {

    @Min(0)
    @McpToolParam(description = "Cap on prescriptions returned per row.", required = false)
    private Integer maxPrescriptions;

    @Min(0)
    @McpToolParam(description = "Cap on top contributing fields (factors[]) returned per row.", required = false)
    private Integer maxTopFactors;

    @Min(0)
    @Max(100)
    @McpToolParam(description = "Minimum prescription contribution percentage between 0 and 100.", required = false)
    private Integer prescriptionImpactPercentage;

    public Integer getMaxPrescriptions() {
        return maxPrescriptions;
    }

    public void setMaxPrescriptions(Integer maxPrescriptions) {
        this.maxPrescriptions = maxPrescriptions;
    }

    public Integer getMaxTopFactors() {
        return maxTopFactors;
    }

    public void setMaxTopFactors(Integer maxTopFactors) {
        this.maxTopFactors = maxTopFactors;
    }

    public Integer getPrescriptionImpactPercentage() {
        return prescriptionImpactPercentage;
    }

    public void setPrescriptionImpactPercentage(Integer prescriptionImpactPercentage) {
        this.prescriptionImpactPercentage = prescriptionImpactPercentage;
    }
}
