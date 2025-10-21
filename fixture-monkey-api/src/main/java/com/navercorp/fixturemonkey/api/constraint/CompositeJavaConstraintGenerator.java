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

package com.navercorp.fixturemonkey.api.constraint;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "1.0.17", status = Status.EXPERIMENTAL)
public final class CompositeJavaConstraintGenerator implements JavaConstraintGenerator {
	private final List<JavaConstraintGenerator> javaConstraintGenerators;

	public CompositeJavaConstraintGenerator(List<JavaConstraintGenerator> javaConstraintGenerators) {
		this.javaConstraintGenerators = javaConstraintGenerators;
	}

	@Nullable
	@Override
	public JavaStringConstraint generateStringConstraint(ArbitraryGeneratorContext context) {
		for (JavaConstraintGenerator javaConstraintGenerator : javaConstraintGenerators) {
			JavaStringConstraint constraint = javaConstraintGenerator.generateStringConstraint(context);
			if (constraint != null) {
				return constraint;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public JavaIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context) {
		for (JavaConstraintGenerator javaConstraintGenerator : javaConstraintGenerators) {
			JavaIntegerConstraint constraint = javaConstraintGenerator.generateIntegerConstraint(context);
			if (constraint != null) {
				return constraint;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public JavaDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context) {
		for (JavaConstraintGenerator javaConstraintGenerator : javaConstraintGenerators) {
			JavaDecimalConstraint constraint = javaConstraintGenerator.generateDecimalConstraint(context);
			if (constraint != null) {
				return constraint;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public JavaContainerConstraint generateContainerConstraint(ArbitraryGeneratorContext context) {
		for (JavaConstraintGenerator javaConstraintGenerator : javaConstraintGenerators) {
			JavaContainerConstraint constraint = javaConstraintGenerator.generateContainerConstraint(context);
			if (constraint != null) {
				return constraint;
			}
		}
		return null;
	}

	@Nullable
	@Override
	public JavaDateTimeConstraint generateDateTimeConstraint(ArbitraryGeneratorContext context) {
		for (JavaConstraintGenerator javaConstraintGenerator : javaConstraintGenerators) {
			JavaDateTimeConstraint constraint = javaConstraintGenerator.generateDateTimeConstraint(context);
			if (constraint != null) {
				return constraint;
			}
		}
		return null;
	}
}
