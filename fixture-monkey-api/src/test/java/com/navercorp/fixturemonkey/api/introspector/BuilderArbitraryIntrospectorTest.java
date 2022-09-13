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

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;

public class BuilderArbitraryIntrospectorTest {
	@Test
	void introspect() {
		// given
		BuilderArbitraryIntrospector sut = new BuilderArbitraryIntrospector();

		ArbitraryGeneratorContext context = makeContext(new TypeReference<BuilderSample>() {
		});

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		BuilderSample sample = (BuilderSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	@Test
	void introspectClassWithDefaultBuilderMethod() {
		// given
		BuilderArbitraryIntrospector sut = new BuilderArbitraryIntrospector();
		sut.setDefaultBuilderMethodName("customBuilder");

		ArbitraryGeneratorContext context = makeContext(new TypeReference<CustomBuilderMethodSample>() {
		});

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		CustomBuilderMethodSample sample = (CustomBuilderMethodSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	@Test
	void introspectClassWithDefaultBuildMethod() {
		// given
		BuilderArbitraryIntrospector sut = new BuilderArbitraryIntrospector();
		sut.setDefaultBuildMethodName("customBuild");

		ArbitraryGeneratorContext context = makeContext(new TypeReference<CustomBuildMethodSample>() {
		});

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		CustomBuildMethodSample sample = (CustomBuildMethodSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	@Test
	void introspectClassWithTypedBuilderMethod() {
		// given
		BuilderArbitraryIntrospector sut = new BuilderArbitraryIntrospector();
		sut.setBuilderMethodName(CustomBuilderMethodSample.class, "customBuilder");

		ArbitraryGeneratorContext context = makeContext(new TypeReference<CustomBuilderMethodSample>() {
		});

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		CustomBuilderMethodSample sample = (CustomBuilderMethodSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	@Test
	void introspectClassWithTypedBuildMethod() {
		// given
		BuilderArbitraryIntrospector sut = new BuilderArbitraryIntrospector();
		sut.setBuildMethodName(CustomBuildMethodSample.CustomBuildMethodSampleBuilder.class, "customBuild");

		ArbitraryGeneratorContext context = makeContext(new TypeReference<CustomBuildMethodSample>() {
		});

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(context);
		then(actual.getValue()).isNotNull();

		CustomBuildMethodSample sample = (CustomBuildMethodSample)actual.getValue().sample();
		then(sample.getName()).isNotNull();
		then(sample.getValue()).isNotNull();
	}

	@Getter
	@Builder
	static class BuilderSample {
		private String name;
		private int value;
	}

	@Getter
	@Builder
	static class CustomBuilderMethodSample {
		private String name;
		private int value;

		public static CustomBuilderMethodSampleBuilder customBuilder() {
			return new CustomBuilderMethodSampleBuilder();
		}
	}

	@Getter
	@Builder
	static class CustomBuildMethodSample {
		private String name;
		private int value;

		public static class CustomBuildMethodSampleBuilder {
			public CustomBuildMethodSample customBuild() {
				return new CustomBuildMethodSample(name, value);
			}
		}
	}

	private final ArbitraryIntrospector arbitraryIntrospector = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new EnumIntrospector(),
			new BooleanIntrospector(),
			new UuidIntrospector(),
			new JavaArbitraryIntrospector(),
			new JavaTimeArbitraryIntrospector()
		)
	);

	private <T> ArbitraryGeneratorContext makeContext(TypeReference<T> typeReference) {
		RootProperty rootProperty = new RootProperty(typeReference.getAnnotatedType());

		GenerateOptions generateOptions = GenerateOptions.DEFAULT_GENERATE_OPTIONS;
		ArbitraryProperty rootArbitraryProperty = getArbitraryProperty(rootProperty, null, generateOptions);

		List<ArbitraryProperty> childrenProperties = PropertyCache.getProperties(typeReference.getAnnotatedType())
			.stream()
			.map(it -> getArbitraryProperty(it, rootArbitraryProperty, generateOptions))
			.collect(toList());

		return new ArbitraryGeneratorContext(
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
	}

	private ArbitraryProperty getArbitraryProperty(
		Property property,
		@Nullable ArbitraryProperty ownerArbitraryProperty,
		GenerateOptions generateOptions
	) {
		ObjectPropertyGenerator rootGenerator = generateOptions.getObjectPropertyGenerator(property);
		return new ArbitraryProperty(
			rootGenerator.generate(
				new ObjectPropertyGeneratorContext(
					property,
					null,
					ownerArbitraryProperty,
					false,
					generateOptions
				)
			),
			null
		);
	}
}
