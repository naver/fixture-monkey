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

package com.navercorp.fixturemonkey.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.LazyPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.tree.TraverseContext;
import com.navercorp.fixturemonkey.api.tree.TreeNodeManipulator;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.tree.GenerateFixtureContext;
import com.navercorp.fixturemonkey.tree.ObjectTree;

/**
 * {@link FixtureMonkey} → {@link ArbitraryBuilder} → {@link ObjectTree} → {@link CombinableArbitrary}
 * 1:N							1:N					1:1
 * <p>
 * It is a context within {@link ArbitraryBuilder}. It represents a status of the {@link ArbitraryBuilder}.
 * The {@link ArbitraryBuilder} should be the same if the {@link ArbitraryBuilderContext} is the same.
 * <p>
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "0.4.0", status = Status.INTERNAL)
public final class ArbitraryBuilderContext {
	private final List<ArbitraryManipulator> manipulators;
	private final List<ContainerInfoManipulator> containerInfoManipulators;
	private final Map<Class<?>, List<Property>> propertyConfigurers;
	private final Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType;
	private final MonkeyContext monkeyContext;

	@Nullable
	private Boolean optionValidOnly;

	@Nullable
	private Boolean customizedValidOnly;

	@Nullable
	private FixedState fixedState = null;
	@Nullable
	private CombinableArbitrary<?> fixedCombinableArbitrary;

	private ArbitraryBuilderContext(
		List<ArbitraryManipulator> manipulators,
		List<ContainerInfoManipulator> containerInfoManipulators,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorsByType,
		@Nullable FixedState fixedState,
		@Nullable CombinableArbitrary<?> fixedCombinableArbitrary,
		MonkeyContext monkeyContext
	) {
		this.manipulators = manipulators;
		this.containerInfoManipulators = containerInfoManipulators;
		this.propertyConfigurers = propertyConfigurers;
		this.arbitraryIntrospectorsByType = arbitraryIntrospectorsByType;
		this.fixedState = fixedState;
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
		this.monkeyContext = monkeyContext;
	}

	/**
	 * It is in {@link ArbitraryBuilderContext} due to MonkeyContext is in api module.
	 * It will be removed when all related class migrate to api module.
	 */
	@Deprecated
	public static ArbitraryBuilderContext newBuilderContext(MonkeyContext monkeyContext) {
		return new ArbitraryBuilderContext(
			new ArrayList<>(),
			new ArrayList<>(),
			new HashMap<>(),
			new HashMap<>(),
			null, null,
			monkeyContext
		);
	}

	public ArbitraryBuilderContext copy() {
		List<ContainerInfoManipulator> copiedContainerInfoManipulators = this.containerInfoManipulators.stream()
			.map(ContainerInfoManipulator::copy)
			.collect(Collectors.toList());

		ArbitraryBuilderContext copiedContext = new ArbitraryBuilderContext(
			new ArrayList<>(this.manipulators),
			copiedContainerInfoManipulators,
			new HashMap<>(propertyConfigurers),
			new HashMap<>(arbitraryIntrospectorsByType),
			fixedState,
			fixedCombinableArbitrary,
			monkeyContext
		);

		copiedContext.setCustomizedValidOnly(customizedValidOnly);
		copiedContext.setOptionValidOnly(optionValidOnly);

		return copiedContext;
	}

	public void addManipulator(ArbitraryManipulator arbitraryManipulator) {
		this.manipulators.add(arbitraryManipulator);
	}

	public void addManipulators(Collection<ArbitraryManipulator> arbitraryManipulators) {
		this.manipulators.addAll(arbitraryManipulators);
	}

	public List<ArbitraryManipulator> getManipulators() {
		return Collections.unmodifiableList(manipulators);
	}

	public void addContainerInfoManipulator(ContainerInfoManipulator containerInfo) {
		this.containerInfoManipulators.add(containerInfo);
	}

	public void addContainerInfoManipulators(List<ContainerInfoManipulator> containerInfoManipulators) {
		this.containerInfoManipulators.addAll(containerInfoManipulators);
	}

	public List<TreeNodeManipulator> getContainerInfoManipulators() {
		return Collections.unmodifiableList(containerInfoManipulators);
	}

	public void putPropertyConfigurer(Class<?> type, List<Property> propertyConfigurer) {
		this.propertyConfigurers.put(type, propertyConfigurer);
	}

	public void putArbitraryIntrospector(Class<?> type, ArbitraryIntrospector arbitraryIntrospector) {
		this.arbitraryIntrospectorsByType.put(type, arbitraryIntrospector);
	}

	public Map<Class<?>, ArbitraryIntrospector> getArbitraryIntrospectorsByType() {
		return arbitraryIntrospectorsByType;
	}

	public Map<Class<?>, List<Property>> getPropertyConfigurers() {
		return propertyConfigurers;
	}

	public void setOptionValidOnly(@Nullable Boolean optionValidOnly) {
		this.optionValidOnly = optionValidOnly;
	}

	public void setCustomizedValidOnly(@Nullable Boolean customizedValidOnly) {
		this.customizedValidOnly = customizedValidOnly;
	}

	public boolean isValidOnly() {
		if (this.customizedValidOnly != null) {
			return this.customizedValidOnly;
		}

		if (this.optionValidOnly != null) {
			return this.optionValidOnly;
		}
		return true;
	}

	public void markFixed() {
		if (fixedState != null
			&& fixedState.getFixedManipulateSize() == this.manipulators.size()
			&& fixedState.getFixedContainerManipulatorSize() == this.containerInfoManipulators.size()) {
			return;
		}

		fixedState = new FixedState(this.manipulators.size(), this.containerInfoManipulators.size());
		fixedCombinableArbitrary = null;
	}

	public boolean isFixed() {
		return fixedState != null;
	}

	public boolean fixedExpired() {
		return manipulators.size() > Objects.requireNonNull(fixedState).getFixedManipulateSize()
			|| containerInfoManipulators.size() > fixedState.getFixedContainerManipulatorSize();
	}

	public void renewFixed(CombinableArbitrary<?> fixedCombinableArbitrary) {
		this.markFixed();
		this.fixedCombinableArbitrary = fixedCombinableArbitrary;
	}

	@Nullable
	public CombinableArbitrary<?> getFixedCombinableArbitrary() {
		return fixedCombinableArbitrary;
	}

	public TraverseContext newTraverseContext(
		TreeRootProperty rootProperty,
		Map<Class<?>, List<Property>> registeredPropertyConfigurer
	) {
		List<MatcherOperator<List<TreeNodeManipulator>>> registeredTreeNodeManipulators =
			monkeyContext.getRegisteredArbitraryBuilders()
				.stream()
				.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
				.map(it -> new MatcherOperator<>(
					it.getMatcher(),
					((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext()
						.getContainerInfoManipulators()
				))
				.collect(Collectors.toList()); // TODO: Fragmented registered

		TreeNodeManipulator registeredRootTreeManipulator = registeredTreeNodeManipulators.stream()
			.filter(it -> it.match(rootProperty))
			.flatMap(it -> it.getOperator().stream())
			.findFirst()
			.orElse(null);

		List<TreeNodeManipulator> activeTreeNodeManipulators = new ArrayList<>(this.getContainerInfoManipulators());
		if (registeredRootTreeManipulator != null) {
			activeTreeNodeManipulators.add(0, registeredRootTreeManipulator);
		}

		FixtureMonkeyOptions fixtureMonkeyOptions = this.monkeyContext.getFixtureMonkeyOptions();
		Map<Class<?>, List<Property>> concatPropertyConfigurer = new HashMap<>(this.getPropertyConfigurers());
		concatPropertyConfigurer.putAll(registeredPropertyConfigurer);
		concatPropertyConfigurer = Collections.unmodifiableMap(concatPropertyConfigurer);

		return new TraverseContext(
			rootProperty,
			new ArrayList<>(),
			activeTreeNodeManipulators,
			registeredTreeNodeManipulators,
			concatPropertyConfigurer,
			this.isValidOnly(),
			initializeResolvedPropertyGenerator(
				concatPropertyConfigurer,
				fixtureMonkeyOptions.getPropertyGenerators(),
				fixtureMonkeyOptions.getDefaultArbitraryGenerator(),
				fixtureMonkeyOptions.getDefaultPropertyGenerator()
			),
			fixtureMonkeyOptions.getObjectPropertyGenerators(),
			fixtureMonkeyOptions.getDefaultObjectPropertyGenerator(),
			fixtureMonkeyOptions.getContainerPropertyGenerators(),
			fixtureMonkeyOptions.getPropertyNameResolvers(),
			fixtureMonkeyOptions.getDefaultPropertyNameResolver(),
			fixtureMonkeyOptions.getCandidateConcretePropertyResolvers(),
			fixtureMonkeyOptions.getArbitraryContainerInfoGenerators(),
			fixtureMonkeyOptions.getDefaultArbitraryContainerInfoGenerator(),
			fixtureMonkeyOptions.getNullInjectGenerators(),
			fixtureMonkeyOptions.getDefaultNullInjectGenerator()
		);
	}

	public GenerateFixtureContext newGenerateFixtureContext(
		Map<Class<?>, ArbitraryIntrospector> registeredArbitraryIntrospectorsByType
	) {
		Map<Class<?>, ArbitraryIntrospector> concat = new HashMap<>(arbitraryIntrospectorsByType);
		concat.putAll(registeredArbitraryIntrospectorsByType);
		concat = Collections.unmodifiableMap(concat);

		return new GenerateFixtureContext(
			concat,
			this::isValidOnly,
			monkeyContext
		);
	}

	private static class FixedState {
		private final int fixedManipulateSize;
		private final int fixedContainerManipulatorSize;

		public FixedState(int fixedManipulateSize, int fixedContainerManipulatorSize) {
			this.fixedManipulateSize = fixedManipulateSize;
			this.fixedContainerManipulatorSize = fixedContainerManipulatorSize;
		}

		public int getFixedManipulateSize() {
			return fixedManipulateSize;
		}

		public int getFixedContainerManipulatorSize() {
			return fixedContainerManipulatorSize;
		}
	}

	private static LazyPropertyGenerator initializeResolvedPropertyGenerator(
		Map<Class<?>, List<Property>> propertyConfigurers,
		List<MatcherOperator<PropertyGenerator>> optionalPropertyGenerators,
		ArbitraryGenerator defaultArbitraryGenerator,
		PropertyGenerator defaultPropertyGenerator
	) {
		PropertyGenerator resolvedPropertyGenerator = property -> {
			Class<?> type = Types.getActualType(property.getType());
			List<Property> propertyConfigurer = propertyConfigurers.get(type);
			if (propertyConfigurer != null) {
				return propertyConfigurer;
			}

			PropertyGenerator propertyGenerator = optionalPropertyGenerators.stream()
				.filter(it -> it.match(property))
				.map(MatcherOperator::getOperator)
				.findFirst()
				.orElse(null);

			if (propertyGenerator != null) {
				return propertyGenerator.generateChildProperties(property);
			}

			PropertyGenerator defaultArbitraryGeneratorPropertyGenerator =
				defaultArbitraryGenerator.getRequiredPropertyGenerator(property);

			if (defaultArbitraryGeneratorPropertyGenerator != null) {
				return defaultArbitraryGeneratorPropertyGenerator.generateChildProperties(property);
			}

			return defaultPropertyGenerator.generateChildProperties(property);
		};

		return new LazyPropertyGenerator(resolvedPropertyGenerator);
	}
}
