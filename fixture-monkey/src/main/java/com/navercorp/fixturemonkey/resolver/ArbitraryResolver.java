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

package com.navercorp.fixturemonkey.resolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryResolver {
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final MonkeyContext monkeyContext;
	private final List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders;

	public ArbitraryResolver(
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		MonkeyContext monkeyContext,
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders
	) {
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.monkeyContext = monkeyContext;
		this.registeredArbitraryBuilders = registeredArbitraryBuilders;
	}

	public CombinableArbitrary<?> resolve(
		RootProperty rootProperty,
		ArbitraryBuilderContext builderContext
	) {
		List<ArbitraryManipulator> manipulators = builderContext.getManipulators();
		List<ContainerInfoManipulator> containerInfoManipulators = builderContext.getContainerInfoManipulators();
		Map<Class<?>, List<Property>> propertyConfigurers = builderContext.getPropertyConfigurers();

		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators =
			registeredArbitraryBuilders.stream()
				.map(it -> new MatcherOperator<>(
					it.getMatcher(),
					((DefaultArbitraryBuilder<?>)it.getOperator()).getContext().getContainerInfoManipulators()
				))
				.collect(Collectors.toList());

		Map<Class<?>, ArbitraryIntrospector> arbitraryIntrospectorConfigurers =
			builderContext.getArbitraryIntrospectorsByType();

		return new ResolvedCombinableArbitrary<>(
			rootProperty,
			() -> new ObjectTree(
				rootProperty,
				this.traverser.traverse(
					rootProperty,
					containerInfoManipulators,
					registeredContainerInfoManipulators,
					propertyConfigurers
				),
				fixtureMonkeyOptions,
				monkeyContext,
				builderContext.isValidOnly(),
				arbitraryIntrospectorConfigurers
			),
			objectTree -> {
				List<ArbitraryManipulator> registeredManipulators =
					monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
						registeredArbitraryBuilders,
						objectTree.getMetadata().getNodesByProperty()
					);

				List<ArbitraryManipulator> joinedManipulators =
					Stream.concat(registeredManipulators.stream(), manipulators.stream())
						.collect(Collectors.toList());

				List<ArbitraryManipulator> optimizedManipulator = manipulatorOptimizer
					.optimize(joinedManipulators)
					.getManipulators();

				for (ArbitraryManipulator manipulator : optimizedManipulator) {
					manipulator.manipulate(objectTree);
				}
				return objectTree.generate();
			},
			fixtureMonkeyOptions.getGenerateMaxTries(),
			fixtureMonkeyOptions.getDefaultArbitraryValidator(),
			builderContext.isValidOnly()
		);
	}
}
