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

package com.navercorp.fixturemonkey.api.option;

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NOTNULL_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULLABLE_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions.DEFAULT_PROPERTY_GENERATORS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.container.DefaultDecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.JavaDefaultArbitraryGeneratorBuilder;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.PropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public class FixtureMonkeyOptionsBuilder {
	private List<MatcherOperator<PropertyGenerator>> propertyGenerators = new ArrayList<>(DEFAULT_PROPERTY_GENERATORS);
	private PropertyGenerator defaultPropertyGenerator = new DefaultPropertyGenerator();
	private List<MatcherOperator<ObjectPropertyGenerator>> arbitraryObjectPropertyGenerators =
		new ArrayList<>(FixtureMonkeyOptions.DEFAULT_OBJECT_PROPERTY_GENERATORS);
	private List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators =
		new ArrayList<>(FixtureMonkeyOptions.DEFAULT_CONTAINER_PROPERTY_GENERATORS);
	private ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers = new ArrayList<>();
	private PropertyNameResolver defaultPropertyNameResolver;
	private List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators = new ArrayList<>();
	private NullInjectGenerator defaultNullInjectGenerator;
	private List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators = new ArrayList<>();
	private ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	private List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators = new ArrayList<>();
	private ArbitraryGenerator defaultArbitraryGenerator;

	@SuppressWarnings("rawtypes")
	@Deprecated // It would be removed in 0.6.0
	private List<MatcherOperator<FixtureCustomizer>> arbitraryCustomizers = new ArrayList<>();
	private final JavaDefaultArbitraryGeneratorBuilder javaDefaultArbitraryGeneratorBuilder =
		DefaultArbitraryGenerator.javaBuilder();
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;
	private boolean nullableElement = false;
	private UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator = it -> it;
	private ArbitraryValidator defaultArbitraryValidator = (obj) -> {
	};
	private DecomposedContainerValueFactory additionalDecomposedContainerValueFactory = null;

	FixtureMonkeyOptionsBuilder() {
	}

	public FixtureMonkeyOptionsBuilder propertyGenerators(List<MatcherOperator<PropertyGenerator>> propertyGenerators) {
		this.propertyGenerators = propertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		MatcherOperator<PropertyGenerator> propertyGenerator
	) {
		List<MatcherOperator<PropertyGenerator>> result =
			insertFirst(this.propertyGenerators, propertyGenerator);
		return this.propertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Matcher matcher,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			new MatcherOperator<>(matcher, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Class<?> type,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.defaultPropertyGenerator = propertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryObjectPropertyGenerators(
		List<MatcherOperator<ObjectPropertyGenerator>> arbitraryObjectPropertyGenerators
	) {
		this.arbitraryObjectPropertyGenerators = arbitraryObjectPropertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> arbitraryObjectPropertyGenerator
	) {
		List<MatcherOperator<ObjectPropertyGenerator>> result =
			insertFirst(this.arbitraryObjectPropertyGenerators, arbitraryObjectPropertyGenerator);
		return this.arbitraryObjectPropertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Matcher matcher,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			new MatcherOperator<>(matcher, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerPropertyGenerators(
		List<MatcherOperator<ContainerPropertyGenerator>> arbitraryContainerPropertyGenerators
	) {
		this.containerPropertyGenerators = arbitraryContainerPropertyGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> arbitraryContainerPropertyGenerator
	) {
		List<MatcherOperator<ContainerPropertyGenerator>> result =
			insertFirst(this.containerPropertyGenerators, arbitraryContainerPropertyGenerator);
		return this.arbitraryContainerPropertyGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Matcher matcher,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			new MatcherOperator<>(matcher, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator defaultObjectPropertyGenerator
	) {
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		List<MatcherOperator<PropertyNameResolver>> result =
			insertFirst(this.propertyNameResolvers, propertyNameResolver);
		return this.propertyNameResolvers(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Matcher matcher,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(new MatcherOperator<>(matcher, propertyNameResolver));
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(
			MatcherOperator.assignableTypeMatchOperator(type, propertyNameResolver)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyNameResolver(PropertyNameResolver defaultPropertyNameResolver) {
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullInjectGenerators(
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators
	) {
		this.nullInjectGenerators = nullInjectGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		MatcherOperator<NullInjectGenerator> nullInjectGenerator
	) {
		List<MatcherOperator<NullInjectGenerator>> result =
			insertFirst(this.nullInjectGenerators, nullInjectGenerator);
		return this.nullInjectGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Matcher matcher,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(new MatcherOperator<>(matcher, nullInjectGenerator));
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(
			MatcherOperator.assignableTypeMatchOperator(type, nullInjectGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator) {
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGeneratorOperator(
		UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator
	) {
		this.defaultNullInjectGeneratorOperator = defaultNullInjectGeneratorOperator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerInfoGenerators(
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators
	) {
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> result =
			insertFirst(this.arbitraryContainerInfoGenerators, arbitraryContainerInfoGenerator);
		return this.arbitraryContainerInfoGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Matcher matcher,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			new MatcherOperator<>(matcher, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Class<?> type,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryContainerInfoGenerator(
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator) {
		this.defaultArbitraryContainerInfoGenerator = defaultArbitraryContainerInfoGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryGenerators(
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators
	) {
		this.arbitraryGenerators = arbitraryGenerators;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		MatcherOperator<ArbitraryGenerator> arbitraryGenerator
	) {
		List<MatcherOperator<ArbitraryGenerator>> result =
			insertFirst(this.arbitraryGenerators, arbitraryGenerator);
		return this.arbitraryGenerators(result);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		Matcher matcher,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			new MatcherOperator<>(matcher, arbitraryGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryGenerator(
		Class<?> type,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		return this.insertFirstArbitraryIntrospector(
			arbitraryIntrospector.getMatcher(),
			arbitraryIntrospector.getOperator()
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Matcher matcher,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryGenerator(
			matcher,
			new DefaultArbitraryGenerator(arbitraryIntrospector)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, new DefaultArbitraryGenerator(arbitraryIntrospector))
		);
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryGenerator(ArbitraryGenerator defaultArbitraryGenerator) {
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder priorityIntrospector(
		UnaryOperator<ArbitraryIntrospector> priorityIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.priorityIntrospector(priorityIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder containerIntrospector(
		UnaryOperator<ArbitraryIntrospector> containerIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.containerIntrospector(containerIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder objectIntrospector(
		UnaryOperator<ArbitraryIntrospector> objectIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.objectIntrospector(objectIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder fallbackIntrospector(
		UnaryOperator<ArbitraryIntrospector> fallbackIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.fallbackIntrospector(fallbackIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		this.javaDefaultArbitraryGeneratorBuilder.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaArbitraryResolver(
		JavaArbitraryResolver javaArbitraryResolver
	) {
		this.javaDefaultArbitraryGeneratorBuilder.javaArbitraryResolver(javaArbitraryResolver);
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		this.javaDefaultArbitraryGeneratorBuilder.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTimeArbitraryResolver(
		JavaTimeArbitraryResolver javaTimeArbitraryResolver
	) {
		this.javaDefaultArbitraryGeneratorBuilder.javaTimeArbitraryResolver(javaTimeArbitraryResolver);
		return this;
	}

	public FixtureMonkeyOptionsBuilder plugin(Plugin plugin) {
		plugin.accept((FixtureMonkeyOptionsBuilder)this);
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Deprecated // It would be removed in 0.6.0
	public FixtureMonkeyOptionsBuilder arbitraryCustomizers(
		List<MatcherOperator<FixtureCustomizer>> arbitraryCustomizers
	) {
		this.arbitraryCustomizers = arbitraryCustomizers;
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Deprecated // It would be removed in 0.6.0
	public FixtureMonkeyOptionsBuilder insertFirstFixtureCustomizer(
		MatcherOperator<FixtureCustomizer> arbitraryCustomizer
	) {
		List<MatcherOperator<FixtureCustomizer>> result =
			insertFirst(this.arbitraryCustomizers, arbitraryCustomizer);
		return arbitraryCustomizers(result);
	}

	@Deprecated // It would be removed in 0.6.0
	public FixtureMonkeyOptionsBuilder insertFirstFixtureCustomizer(
		Matcher matcher,
		FixtureCustomizer<?> fixtureCustomizer
	) {
		return this.insertFirstFixtureCustomizer(
			new MatcherOperator<>(matcher, fixtureCustomizer)
		);
	}

	@Deprecated // It would be removed in 0.6.0
	public <T> FixtureMonkeyOptionsBuilder insertFirstFixtureCustomizer(
		Class<T> type,
		FixtureCustomizer<? extends T> fixtureCustomizer
	) {
		return this.insertFirstFixtureCustomizer(
			MatcherOperator.assignableTypeMatchOperator(type, fixtureCustomizer)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableElement(boolean nullableElement) {
		this.nullableElement = nullableElement;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.defaultArbitraryValidator = arbitraryValidator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder additionalDecomposedContainerValueFactory(
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		this.additionalDecomposedContainerValueFactory = additionalDecomposedContainerValueFactory;
		return this;
	}

	public FixtureMonkeyOptions build() {
		ObjectPropertyGenerator defaultObjectPropertyGenerator = defaultIfNull(
			this.defaultObjectPropertyGenerator,
			() -> FixtureMonkeyOptions.DEFAULT_OBJECT_PROPERTY_GENERATOR
		);
		PropertyNameResolver defaultPropertyNameResolver = defaultIfNull(
			this.defaultPropertyNameResolver,
			() -> FixtureMonkeyOptions.DEFAULT_PROPERTY_NAME_RESOLVER
		);

		NullInjectGenerator defaultNullInjectGenerator = defaultIfNull(
			this.defaultNullInjectGenerator,
			() -> new DefaultNullInjectGenerator(
				DEFAULT_NULL_INJECT,
				nullableContainer,
				defaultNotNull,
				nullableElement,
				new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
				new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
			)
		);

		if (defaultNullInjectGeneratorOperator != null) {
			defaultNullInjectGenerator = defaultNullInjectGeneratorOperator.apply(defaultNullInjectGenerator);
		}

		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator = defaultIfNull(
			this.defaultArbitraryContainerInfoGenerator,
			() -> context -> new ArbitraryContainerInfo(0, FixtureMonkeyOptions.DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE)
		);
		ArbitraryGenerator defaultArbitraryGenerator =
			defaultIfNull(this.defaultArbitraryGenerator, this.javaDefaultArbitraryGeneratorBuilder::build);

		DecomposedContainerValueFactory decomposedContainerValueFactory = new DefaultDecomposedContainerValueFactory(
			additionalDecomposedContainerValueFactory
		);

		return new FixtureMonkeyOptions(
			this.propertyGenerators,
			this.defaultPropertyGenerator,
			this.arbitraryObjectPropertyGenerators,
			defaultObjectPropertyGenerator,
			this.containerPropertyGenerators,
			this.propertyNameResolvers,
			defaultPropertyNameResolver,
			this.nullInjectGenerators,
			defaultNullInjectGenerator,
			this.arbitraryContainerInfoGenerators,
			defaultArbitraryContainerInfoGenerator,
			this.arbitraryGenerators,
			this.arbitraryCustomizers,
			defaultArbitraryGenerator,
			this.defaultArbitraryValidator,
			decomposedContainerValueFactory
		);
	}

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}

	private static <T> List<T> insertFirst(List<T> list, T value) {
		List<T> result = new ArrayList<>();
		result.add(value);
		result.addAll(list);
		return result;
	}
}
