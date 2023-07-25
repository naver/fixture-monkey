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

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
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
		List<ArbitraryManipulator> manipulators,
		List<ContainerInfoManipulator> containerInfoManipulators
	) {
		List<MatcherOperator<List<ContainerInfoManipulator>>> registeredContainerInfoManipulators =
			registeredArbitraryBuilders.stream()
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
			monkeyContext
		);

		List<ArbitraryManipulator> registeredManipulators = monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
			registeredArbitraryBuilders,
			objectTree.getMetadata().getNodesByProperty()
		);

		List<ArbitraryManipulator> joinedManipulators =
			Stream.concat(registeredManipulators.stream(), manipulators.stream())
				.collect(Collectors.toList());

		List<ArbitraryManipulator> optimizedManipulator = manipulatorOptimizer
			.optimize(joinedManipulators)
			.getManipulators();

		return new CombinableArbitrary() {
			private final LazyArbitrary<CombinableArbitrary<?>> lazyArbitrary = LazyArbitrary.lazy(
				() -> {
					for (ArbitraryManipulator manipulator : optimizedManipulator) {
						manipulator.manipulate(objectTree);
					}
					return objectTree.generate();
				}
			);

			@Override
			public Object combined() {
				return lazyArbitrary.getValue().combined();
			}

			@Override
			public Object rawValue() {
				return lazyArbitrary.getValue().rawValue();
			}

			@Override
			public void clear() {
				CombinableArbitrary<?> combinableArbitrary = lazyArbitrary.getValue();
				if (!combinableArbitrary.fixed() && optimizedManipulator.isEmpty()) {
					combinableArbitrary.clear();
				} else {
					lazyArbitrary.clear();
				}
			}

			@Override
			public boolean fixed() {
				return false;
			}
		};
	}
}
