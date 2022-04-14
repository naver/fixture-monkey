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
package com.navercorp.fixturemonkey.api.introspector;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorTest.Season;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class FieldReflectionArbitraryIntrospectorTest {
	private final ArbitraryIntrospector arbitraryIntrospector = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new EnumIntrospector(),
			new BooleanIntrospector(),
			new UuidIntrospector(),
			new JavaArbitraryIntrospector(),
			new JavaTimeArbitraryIntrospector()
		)
	);

	@Test
	void introspect() {
		// given
		FieldReflectionArbitraryIntrospector sut = FieldReflectionArbitraryIntrospector.INSTANCE;

		TypeReference<FieldReflectionSample> typeReference = new TypeReference<FieldReflectionSample>() {
		};
		RootProperty rootProperty = new RootProperty(typeReference.getAnnotatedType());

		GenerateOptions generateOptions = GenerateOptions.DEFAULT_GENERATE_OPTIONS;
		ArbitraryPropertyGenerator rootGenerator = generateOptions.getArbitraryPropertyGenerator(rootProperty);
		ArbitraryProperty rootArbitraryProperty = rootGenerator.generate(
			new ArbitraryPropertyGeneratorContext(
				rootProperty,
				null,
				null,
				null,
				generateOptions
			)
		);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> generateOptions.getArbitraryPropertyGenerator(it)
				.generate(
					new ArbitraryPropertyGeneratorContext(
						it,
						null,
						null,
						rootArbitraryProperty,
						generateOptions
					)
				)
			)
			.collect(toList());

		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			rootArbitraryProperty,
			childrenProperties,
			null,
			(ctx, prop) -> this.arbitraryIntrospector.introspect(
				new ArbitraryGeneratorContext(
					prop,
					Collections.emptyList(),
					ctx,
					(ctx2, prop2) -> null
				)
			).getValue()
		);

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);

		then(actual.getValue()).isNotNull();

		FieldReflectionSample sample = (FieldReflectionSample)actual.getValue()
			.sample();
		then(sample.name).isNotNull();
		then(sample.value).isNotNull();
		then(sample.season).isNotNull();
		then(sample.finalValue).isEqualTo("final");
		then(sample.transientValue).isNull();
	}

	@Test
	void introspectWithPropertyNameResolver() {
		// given
		FieldReflectionArbitraryIntrospector sut = FieldReflectionArbitraryIntrospector.INSTANCE;

		TypeReference<FieldReflectionSample> typeReference = new TypeReference<FieldReflectionSample>() {
		};
		RootProperty rootProperty = new RootProperty(typeReference.getAnnotatedType());

		GenerateOptions generateOptions = new GenerateOptions(
			GenerateOptions.DEFAULT_ARBITRARY_PROPERTY_GENERATORS,
			Collections.singletonList(new MatcherOperator<>(p -> true, property -> "x_" + property.getName())),
			Collections.emptyList(),
			new DefaultNullInjectGenerator(),
			Collections.emptyList(),
			new ArbitraryContainerInfo(0, 3),
			GenerateOptions.DEFAULT_ARBITRARY_GENERATOR
		);
		ArbitraryPropertyGenerator rootGenerator = generateOptions.getArbitraryPropertyGenerator(rootProperty);
		ArbitraryProperty rootArbitraryProperty = rootGenerator.generate(
			new ArbitraryPropertyGeneratorContext(
				rootProperty,
				null,
				null,
				null,
				generateOptions
			)
		);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> generateOptions.getArbitraryPropertyGenerator(it)
				.generate(
					new ArbitraryPropertyGeneratorContext(
						it,
						null,
						null,
						rootArbitraryProperty,
						generateOptions
					)
				)
			)
			.collect(toList());

		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			rootArbitraryProperty,
			childrenProperties,
			null,
			(ctx, prop) -> this.arbitraryIntrospector.introspect(
				new ArbitraryGeneratorContext(
					prop,
					Collections.emptyList(),
					ctx,
					(ctx2, prop2) -> null
				)
			).getValue()
		);

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);

		then(actual.getValue()).isNotNull();

		FieldReflectionSample sample = (FieldReflectionSample)actual.getValue()
			.sample();
		then(sample.name).isNotNull();
		then(sample.value).isNotNull();
		then(sample.season).isNotNull();
		then(sample.finalValue).isEqualTo("final");
		then(sample.transientValue).isNull();
	}

	static class FieldReflectionSample {
		private String name;
		private int value;
		private Season season;
		private final String finalValue = "final";
		private transient String transientValue;
	}
}
