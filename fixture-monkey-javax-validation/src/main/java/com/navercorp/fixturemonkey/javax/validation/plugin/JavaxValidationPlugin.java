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

package com.navercorp.fixturemonkey.javax.validation.plugin;

import java.util.Arrays;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.javax.validation.generator.JavaxValidationArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.javax.validation.generator.JavaxValidationNullInjectGenerator;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationBooleanIntrospector;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationConstraintGenerator;
import com.navercorp.fixturemonkey.javax.validation.validator.JavaxArbitraryValidator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaxValidationPlugin implements Plugin {
	private final JavaConstraintGenerator javaxValidationConstraintGenerator =
		new JavaxValidationConstraintGenerator();

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder
			.defaultNullInjectGeneratorOperator(JavaxValidationNullInjectGenerator::new)
			.insertFirstArbitraryContainerInfoGenerator(
				prop -> true,
				new JavaxValidationArbitraryContainerInfoGenerator()
			)
			.javaConstraintGenerator(javaxValidationConstraintGenerator)
			.priorityIntrospector(current ->
				new MatchArbitraryIntrospector(
					Arrays.asList(
						new JavaxValidationBooleanIntrospector(),
						current
					)
				)
			)
			.defaultArbitraryValidator(new JavaxArbitraryValidator());
	}
}
