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

import static com.navercorp.fixturemonkey.jakarta.validation.matcher.JakartaMatchers.JAKARTA_PACKAGE_MATCHER;

import java.util.Arrays;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.jakarta.validation.generator.JakartaValidationArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.generator.JakartaValidationNullInjectGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationBooleanIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationConstraintGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationJavaArbitraryResolver;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationJavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.jakarta.validation.introspector.JakartaValidationTimeConstraintGenerator;
import com.navercorp.fixturemonkey.jakarta.validation.validator.JakartaArbitraryValidator;

@API(since = "0.4.10", status = Status.MAINTAINED)
public final class JakartaValidationPlugin implements Plugin {
	private JavaTypeArbitraryGenerator javaTypeArbitraryGenerator = new JavaTypeArbitraryGenerator() {
	};
	private JakartaValidationConstraintGenerator jakartaValidationConstraintGenerator =
		new JakartaValidationConstraintGenerator();
	private JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator =
		new JavaTimeTypeArbitraryGenerator() {
		};
	private JakartaValidationTimeConstraintGenerator jakartaValidationTimeConstraintGenerator =
		new JakartaValidationTimeConstraintGenerator();

	public JakartaValidationPlugin javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		this.javaTypeArbitraryGenerator = javaTypeArbitraryGenerator;
		return this;
	}

	public JakartaValidationPlugin validationConstraintGenerator(
		JakartaValidationConstraintGenerator jakartaValidationConstraintGenerator
	) {
		this.jakartaValidationConstraintGenerator = jakartaValidationConstraintGenerator;
		return this;
	}

	public JakartaValidationPlugin javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		this.javaTimeTypeArbitraryGenerator = javaTimeTypeArbitraryGenerator;
		return this;
	}

	public JakartaValidationPlugin validationTimeConstraintGenerator(
		JakartaValidationTimeConstraintGenerator jakartaValidationTimeConstraintGenerator
	) {
		this.jakartaValidationTimeConstraintGenerator = jakartaValidationTimeConstraintGenerator;
		return this;
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder
			.defaultNullInjectGeneratorOperator(JakartaValidationNullInjectGenerator::new)
			.insertFirstArbitraryContainerInfoGenerator(
				JAKARTA_PACKAGE_MATCHER,
				new JakartaValidationArbitraryContainerInfoGenerator()
			)
			.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator)
			.javaArbitraryResolver(
				new JakartaValidationJavaArbitraryResolver(this.jakartaValidationConstraintGenerator)
			)
			.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator)
			.javaTimeArbitraryResolver(
				new JakartaValidationJavaTimeArbitraryResolver(
					this.jakartaValidationTimeConstraintGenerator
				)
			)
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
