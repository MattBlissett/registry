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
package org.gbif.registry.utils;

import org.gbif.api.model.registry.MachineTag;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.Assert.assertTrue;

public class MachineTags extends JsonBackedData<MachineTag> {

  private static final MachineTags INSTANCE = new MachineTags();

  public static MachineTag newInstance() {
    return INSTANCE.newTypedInstance();
  }

  public MachineTags() {
    super("data/machine_tag.json", new TypeReference<MachineTag>() {});
  }

  @Test
  public void testConstraints() {
    ValidatorFactory validatorFactory =
        Validation.byProvider(HibernateValidator.class).configure().buildValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<MachineTag>> violations = validator.validate(MachineTags.newInstance());
    assertTrue(violations.isEmpty());
  }
}
