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

package com.navercorp.fixturemonkey;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class LabMonkey extends FixtureMonkey {
	private final GenerateOptions generateOptions;
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final ArbitraryValidator validator;

	@SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
	public LabMonkey(
		GenerateOptions generateOptions,
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		ArbitraryValidator validator
	) {
		super(null, null, null, null, null);
		this.generateOptions = generateOptions;
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.validator = validator;
	}

	@Override
	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> type) {
		return new ArbitraryBuilder<>(
			new RootProperty(toAnnotatedType(type)),
			new ArrayList<>(),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions
			),
			this.validator
		);
	}

	@Override
	public <T> ArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
		return new ArbitraryBuilder<>(
			new RootProperty(type.getAnnotatedType()),
			new ArrayList<>(),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions
			),
			this.validator
		);
	}

	private <T> AnnotatedType toAnnotatedType(Class<T> type) {
		return new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}
		};
	}
}
