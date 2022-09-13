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

package com.navercorp.fixturemonkey.javax.validation.generator;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationNullInjectGenerator implements NullInjectGenerator {
	private final NullInjectGenerator delegate;

	public JavaxValidationNullInjectGenerator() {
		this(new DefaultNullInjectGenerator());
	}

	public JavaxValidationNullInjectGenerator(NullInjectGenerator delegate) {
		this.delegate = delegate;
	}

	@Override
	public double generate(ObjectPropertyGeneratorContext context) {
		Set<Class<? extends Annotation>> annotations = context.getProperty().getAnnotations().stream()
			.map(Annotation::annotationType)
			.collect(toSet());

		if (annotations.contains(Null.class)) {
			return 1.0d;
		}

		double nullInject = this.delegate.generate(context);
		if (nullInject == 0.0d) {
			return nullInject;
		}

		if (annotations.contains(NotNull.class)) {
			return 0.0d;
		}

		if (Types.getActualType(context.getProperty().getType()) == String.class) {
			if (annotations.contains(NotBlank.class) || annotations.contains(NotEmpty.class)) {
				return 0.0d;
			}
		}

		if (context.isContainer() && annotations.contains(NotEmpty.class)) {
			return 0.0d;
		}

		return nullInject;
	}
}
