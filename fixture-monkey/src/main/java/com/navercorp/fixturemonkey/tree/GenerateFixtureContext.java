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

package com.navercorp.fixturemonkey.tree;

import static com.navercorp.fixturemonkey.api.type.Types.nullSafe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.CompositeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ValidateArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.tree.TraverseNode;
import com.navercorp.fixturemonkey.api.tree.TraverseNodeContext;
import com.navercorp.fixturemonkey.api.tree.TraverseNodeMetadata;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;

/**
 * It is used to generate an instance of the JVM class.
 */
@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public final class GenerateFixtureContext implements TraverseNodeContext {
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorConfigurer;
	private final MonkeyContext monkeyContext;
	private final Supplier<Boolean> validOnly;

	private ObjectNode objectNode = null;
	private LazyArbitrary<Boolean> childNotCacheable = null;

	private final List<NodeManipulator> manipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final List<Predicate> arbitraryFilters = new ArrayList<>();
	private final List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> arbitraryCustomizers =
		new ArrayList<>();
	@Nullable
	private CombinableArbitrary<?> arbitrary;

	public GenerateFixtureContext(
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorConfigurer,
		Supplier<Boolean> validOnly,
		MonkeyContext monkeyContext
	) {
		this.arbitraryIntrospectorConfigurer = arbitraryIntrospectorConfigurer;
		this.validOnly = validOnly;
		this.monkeyContext = monkeyContext;
	}

	private static boolean initializeChildNotCacheable(ObjectNode objectNode) {
		objectNode.expand();
		for (ObjectNode child : objectNode.getChildren().asList()) {
			GenerateFixtureContext childGenerateFixtureContext = child.getObjectNodeContext();
			if (childGenerateFixtureContext.manipulated()
				|| childGenerateFixtureContext.childNotCacheable.getValue()
				|| child.getMetadata().getTreeProperty().isContainer()) {
				return true;
			}
		}

		return false;
	}

	public void addManipulator(NodeManipulator nodeManipulator) {
		this.manipulators.add(nodeManipulator);
	}

	@SuppressWarnings("rawtypes")
	public List<Predicate> getArbitraryFilters() {
		return arbitraryFilters;
	}

	@SuppressWarnings("rawtypes")
	public void addArbitraryFilter(Predicate filter) {
		this.arbitraryFilters.add(filter);
	}

	public void addGeneratedArbitraryCustomizer(
		Function<CombinableArbitrary<?>, CombinableArbitrary<?>> arbitraryCustomizer
	) {
		this.arbitraryCustomizers.add(arbitraryCustomizer);
	}

	public List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> getGeneratedArbitraryCustomizers() {
		return arbitraryCustomizers;
	}

	public void addArbitraryCustomizer(Function<CombinableArbitrary<?>, CombinableArbitrary<?>> arbitraryCustomizer) {
		this.arbitraryCustomizers.add(arbitraryCustomizer);
	}

	public boolean manipulated() {
		return !manipulators.isEmpty() || objectNode.getMetadata().manipulated();
	}

	public boolean cacheable() {
		return !manipulated()
			&& !objectNode.getMetadata().getTreeProperty().isContainer()
			&& !childNotCacheable.getValue();
	}

	@Nullable
	public CombinableArbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public void setArbitrary(@Nullable CombinableArbitrary<?> arbitrary) {
		this.arbitrary = arbitrary;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public CombinableArbitrary<?> generate(@Nullable ArbitraryGeneratorContext parentContext) {
		FixtureMonkeyOptions fixtureMonkeyOptions = monkeyContext.getFixtureMonkeyOptions();

		CombinableArbitrary<?> generated;
		if (this.getArbitrary() != null) {
			generated = this.getArbitrary()
				.injectNull(this.objectNode.getMetadata().getNullInject());
		} else {
			CombinableArbitrary<?> cached = monkeyContext.getCachedArbitrary(
				objectNode.getMetadata().getOriginalProperty());

			if (this.cacheable() && cached != null) {
				generated = cached;
			} else {
				ArbitraryGeneratorContext childArbitraryGeneratorContext = this.generateContext(parentContext);
				ArbitraryIntrospector arbitraryIntrospector = arbitraryIntrospectorConfigurer.get(
					Types.getActualType(objectNode.getMetadata().getOriginalProperty().getType())
				);
				generated = getArbitraryGenerator(arbitraryIntrospector)
					.generate(childArbitraryGeneratorContext);

				List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> customizers =
					this.getGeneratedArbitraryCustomizers();

				for (Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer : customizers) {
					generated = customizer.apply(generated);
				}

				if (this.cacheable()) {
					monkeyContext.putCachedArbitrary(
						objectNode.getMetadata().getOriginalProperty(),
						generated
					);
				}
			}
		}

		List<Predicate> arbitraryFilters = this.getArbitraryFilters();
		for (Predicate predicate : arbitraryFilters) {
			generated = generated.filter(fixtureMonkeyOptions.getGenerateMaxTries(), predicate);
		}

		return generated;
	}

	private ArbitraryGenerator getArbitraryGenerator(@Nullable ArbitraryIntrospector arbitraryIntrospector) {
		FixtureMonkeyOptions fixtureMonkeyOptions = monkeyContext.getFixtureMonkeyOptions();
		ArbitraryGenerator arbitraryGenerator = fixtureMonkeyOptions.getDefaultArbitraryGenerator();

		if (arbitraryIntrospector != null) {
			arbitraryGenerator = new CompositeArbitraryGenerator(
				Arrays.asList(
					new IntrospectedArbitraryGenerator(arbitraryIntrospector),
					arbitraryGenerator
				)
			);
		}

		if (validOnly.get()) {
			arbitraryGenerator = new CompositeArbitraryGenerator(
				Arrays.asList(
					arbitraryGenerator,
					new ValidateArbitraryGenerator(
						fixtureMonkeyOptions.getJavaConstraintGenerator(),
						fixtureMonkeyOptions.getDecomposedContainerValueFactory()
					)
				)
			);
		}

		return arbitraryGenerator;
	}

	public ArbitraryGeneratorContext generateContext(
		@Nullable ArbitraryGeneratorContext parentContext
	) {
		Map<ArbitraryProperty, ObjectNode> childNodesByArbitraryProperty = new HashMap<>();
		List<ArbitraryProperty> childrenProperties = new ArrayList<>();

		ArbitraryProperty arbitraryProperty =
			objectNode.getMetadata().getTreeProperty()
				.toArbitraryProperty(objectNode.getMetadata().getNullInject());
		Property resolvedParentProperty = objectNode.getMetadata().getResolvedTypeDefinition().getResolvedProperty();
		objectNode.expand();
		List<ObjectNode> children = nullSafe(objectNode.getChildren()).asList().stream()
			.filter(it -> Types.isAssignable(
				Types.getActualType(resolvedParentProperty.getType()),
				Types.getActualType(it.getMetadata().getResolvedParentProperty().getType()))
			)
			.collect(Collectors.toList());

		for (ObjectNode childNode : children) {
			TraverseNodeMetadata childNodeMetadata = childNode.getMetadata();
			ArbitraryProperty childArbitraryProperty =
				childNodeMetadata.getTreeProperty().toArbitraryProperty(childNodeMetadata.getNullInject());
			childNodesByArbitraryProperty.put(childArbitraryProperty, childNode);
			childrenProperties.add(childArbitraryProperty);
		}

		MonkeyGeneratorContext monkeyGeneratorContext = monkeyContext.newGeneratorContext(
			objectNode.getMetadata().getRootProperty()
		);
		FixtureMonkeyOptions fixtureMonkeyOptions = monkeyContext.getFixtureMonkeyOptions();
		ArbitraryGeneratorLoggingContext loggingContext = new ArbitraryGeneratorLoggingContext(
			fixtureMonkeyOptions.isEnableLoggingFail());

		return new ArbitraryGeneratorContext(
			resolvedParentProperty,
			arbitraryProperty,
			childrenProperties,
			parentContext,
			(currentContext, prop) -> {
				ObjectNode node = childNodesByArbitraryProperty.get(prop);
				if (node == null) {
					return CombinableArbitrary.NOT_GENERATED;
				}

				return node.getObjectNodeContext().generate(currentContext);
			},
			objectNode.getMetadata().getLazyPropertyPath(),
			monkeyGeneratorContext,
			fixtureMonkeyOptions.getGenerateUniqueMaxTries(),
			arbitraryProperty.getNullInject(),
			loggingContext
		);
	}

	@Override
	public void setTraverseNode(TraverseNode objectNode) {
		this.objectNode = (ObjectNode)objectNode;
		this.childNotCacheable = LazyArbitrary.lazy(() -> initializeChildNotCacheable(this.objectNode));
	}

	@Override
	public GenerateFixtureContext newChildNodeContext() {
		return new GenerateFixtureContext(
			this.arbitraryIntrospectorConfigurer,
			this.validOnly,
			this.monkeyContext
		);
	}
}
