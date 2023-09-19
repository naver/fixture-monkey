/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.jakarta.validation.plugin;

import java.util.Arrays;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.jakarta.validation.generator.JakartaValidationArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.generator.JakartaValidationNullInjectGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationBooleanIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationConstraintGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.validator.JakartaArbitraryValidator;

@API(since = "0.4.10", status = Status.MAINTAINED)
public final class JakartaValidationPlugin implements Plugin {
	private final JavaConstraintGenerator jakartaValidationConstraintGenerator =
		new JakartaValidationConstraintGenerator();

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder
			.defaultNullInjectGeneratorOperator(JakartaValidationNullInjectGenerator::new)
			.insertFirstArbitraryContainerInfoGenerator(
				prop -> true,
				new JakartaValidationArbitraryContainerInfoGenerator()
			)
			.javaConstraintGenerator(jakartaValidationConstraintGenerator)
			.priorityIntrospector(current ->
				new CompositeArbitraryIntrospector(
					Arrays.asList(
						new JakartaValidationBooleanIntrospector(),
						current
					)
				)
			)
			.defaultArbitraryValidator(new JakartaArbitraryValidator());
	}
}
