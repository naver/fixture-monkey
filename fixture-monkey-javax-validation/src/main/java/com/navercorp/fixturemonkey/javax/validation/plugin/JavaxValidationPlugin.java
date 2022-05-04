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

import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.IntrospectorArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.IntrospectorTimeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.javax.validation.generator.JavaxValidationArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.javax.validation.generator.JavaxValidationNullInjectGenerator;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationArbitraryTypeIntrospector;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationBooleanIntrospector;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationConstraintGenerator;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationTimeArbitraryTypeIntrospector;
import com.navercorp.fixturemonkey.javax.validation.introspector.JavaxValidationTimeConstraintGenerator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationPlugin implements Plugin {
	private IntrospectorArbitraryGenerator introspectorArbitraryGenerator = new IntrospectorArbitraryGenerator() {
	};
	private JavaxValidationConstraintGenerator javaxValidationConstraintGenerator =
		new JavaxValidationConstraintGenerator();
	private IntrospectorTimeArbitraryGenerator introspectorTimeArbitraryGenerator =
		new IntrospectorTimeArbitraryGenerator() {
		};
	private JavaxValidationTimeConstraintGenerator javaxValidationTimeConstraintGenerator =
		new JavaxValidationTimeConstraintGenerator();

	public JavaxValidationPlugin introspectorArbitraryGenerator(
		IntrospectorArbitraryGenerator introspectorArbitraryGenerator
	) {
		this.introspectorArbitraryGenerator = introspectorArbitraryGenerator;
		return this;
	}

	public JavaxValidationPlugin validationConstraintGenerator(
		JavaxValidationConstraintGenerator javaxValidationConstraintGenerator
	) {
		this.javaxValidationConstraintGenerator = javaxValidationConstraintGenerator;
		return this;
	}

	public JavaxValidationPlugin introspectorTimeArbitraryGenerator(
		IntrospectorTimeArbitraryGenerator introspectorTimeArbitraryGenerator
	) {
		this.introspectorTimeArbitraryGenerator = introspectorTimeArbitraryGenerator;
		return this;
	}

	public JavaxValidationPlugin validationTimeConstraintGenerator(
		JavaxValidationTimeConstraintGenerator javaxValidationTimeConstraintGenerator
	) {
		this.javaxValidationTimeConstraintGenerator = javaxValidationTimeConstraintGenerator;
		return this;
	}

	@Override
	public void accept(GenerateOptionsBuilder optionsBuilder) {
		optionsBuilder
			.defaultNullInjectGenerator(new JavaxValidationNullInjectGenerator())
			.insertFirstArbitraryContainerInfoGenerator(
				prop -> true,
				new JavaxValidationArbitraryContainerInfoGenerator()
			)
			.priorityIntrospector(current ->
				new CompositeArbitraryIntrospector(
					Arrays.asList(
						new JavaArbitraryIntrospector(
							this.introspectorArbitraryGenerator,
							new JavaxValidationArbitraryTypeIntrospector(this.javaxValidationConstraintGenerator)
						),
						new JavaTimeArbitraryIntrospector(
							this.introspectorTimeArbitraryGenerator,
							new JavaxValidationTimeArbitraryTypeIntrospector(
								this.javaxValidationTimeConstraintGenerator
							)
						),
						new JavaxValidationBooleanIntrospector(),
						current
					)
				)
			);
	}
}
