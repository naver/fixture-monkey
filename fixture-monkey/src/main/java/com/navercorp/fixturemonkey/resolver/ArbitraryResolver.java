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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
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
	private final ManipulateOptions manipulateOptions;
	private final MonkeyContext monkeyContext;

	public ArbitraryResolver(
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		ManipulateOptions manipulateOptions,
		MonkeyContext monkeyContext
	) {
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.manipulateOptions = manipulateOptions;
		this.monkeyContext = monkeyContext;
	}

	@SuppressWarnings("rawtypes")
	public Arbitrary<?> resolve(
		RootProperty rootProperty,
		List<ArbitraryManipulator> manipulators,
		List<MatcherOperator<? extends FixtureCustomizer>> builderCustomizers,
		List<ContainerInfoManipulator> containerInfoManipulators
	) {
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators =
			manipulateOptions.getRegisteredArbitraryBuilders()
				.stream()
				.map(it -> new MatcherOperator<>(
					it.getMatcher(),
					((DefaultArbitraryBuilder<?>)it.getOperator()).getContext().getContainerInfoManipulators()
				))
				.collect(Collectors.toList());

		ObjectTree objectTree = new ObjectTree(
			rootProperty,
			this.traverser.traverse(
				rootProperty,
				containerInfoManipulators,
				registeredContainerInfoManipulators
			),
			fixtureMonkeyOptions,
			monkeyContext,
			builderCustomizers
		);

		List<ArbitraryManipulator> registeredManipulators = monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
			manipulateOptions.getRegisteredArbitraryBuilders(),
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
	}
}
