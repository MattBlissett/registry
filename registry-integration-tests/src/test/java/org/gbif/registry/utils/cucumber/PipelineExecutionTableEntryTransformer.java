/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
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
package org.gbif.registry.utils.cucumber;

import org.gbif.api.model.pipelines.PipelineExecution;
import org.gbif.api.model.pipelines.StepType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.cucumber.datatable.TableEntryTransformer;

public class PipelineExecutionTableEntryTransformer
    implements TableEntryTransformer<PipelineExecution> {

  @Override
  public PipelineExecution transform(Map<String, String> entry) {
    PipelineExecution result = new PipelineExecution();
    Optional.ofNullable(entry.get("stepsToRun"))
        .map(p -> p.split(","))
        .map(p -> Arrays.stream(p).map(StepType::valueOf).collect(Collectors.toList()))
        .ifPresent(result::setStepsToRun);

    result.setRerunReason(entry.get("rerunReason"));
    result.setRemarks(entry.get("remarks"));

    return result;
  }
}
