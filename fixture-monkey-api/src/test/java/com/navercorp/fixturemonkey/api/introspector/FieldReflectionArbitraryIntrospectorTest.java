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

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorTest.Season;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
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
		ArbitraryProperty rootArbitraryProperty = getArbitraryProperty(rootProperty, generateOptions);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> getArbitraryProperty(it, generateOptions))
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
					(ctx2, prop2) -> null,
					Collections.emptyList()
				)
			).getValue(),
			Collections.emptyList()
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
		GenerateOptions generateOptions = GenerateOptions.builder()
			.propertyNameResolvers(
				Collections.singletonList(new MatcherOperator<>(p -> true, property -> "x_" + property.getName()))
			)
			.build();

		ArbitraryProperty rootArbitraryProperty = getArbitraryProperty(rootProperty, generateOptions);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> getArbitraryProperty(it, generateOptions))
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
					(ctx2, prop2) -> null,
					Collections.emptyList()
				)
			).getValue(),
			Collections.emptyList()
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

	private ArbitraryProperty getArbitraryProperty(Property property, GenerateOptions generateOptions) {
		ObjectPropertyGenerator rootGenerator = generateOptions.getObjectPropertyGenerator(property);
		return new ArbitraryProperty(
			rootGenerator.generate(
				new ObjectPropertyGeneratorContext(
					property,
					null,
					null,
					false,
					generateOptions
				)
			),
			null
		);
	}
}
