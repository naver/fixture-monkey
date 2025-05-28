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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.DefaultTreeMatcherMetadata;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContextProvider;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.tree.ObjectNode;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryResolver {
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;
	private final MonkeyContext monkeyContext;

	public ArbitraryResolver(
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyContext monkeyContext
	) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.monkeyContext = monkeyContext;
	}

	public CombinableArbitrary<?> resolve(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<MatcherOperator<ArbitraryBuilderContext>> standbyContexts
	) {
		FixtureMonkeyOptions fixtureMonkeyOptions = monkeyContext.getFixtureMonkeyOptions();

		List<ArbitraryManipulator> activeManipulators = activeContext.getManipulators();

		return new ResolvedCombinableArbitrary<>(
			rootProperty,
			() -> {
				ObjectTree objectTree = new ObjectTree(
					rootProperty,
					activeContext.newGenerateFixtureContext(),
					activeContext.newTraverseContext(rootProperty)
				);

				fixtureMonkeyOptions.getBuilderContextInitializers().stream()
					.filter(it -> it.match(new DefaultTreeMatcherMetadata(objectTree.getMetadata().getAnnotations())))
					.findFirst()
					.map(TreeMatcherOperator::getOperator)
					.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));

				return objectTree;
			},
			objectTree -> {
				Map<Property, List<ObjectNode>> rootNodesByProperty = Collections.singletonMap(
					rootProperty,
					Collections.singletonList(objectTree.getMetadata().getRootNode())
				);

				List<ArbitraryManipulator> registeredRootManipulators =
					monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
						standbyContexts,
						rootNodesByProperty
					);

				List<MatcherOperator<ArbitraryBuilderContext>> registeredPropertyArbitraryBuilderContexts =
					monkeyContext.getRegisteredArbitraryBuilders()
						.stream()
						.map(it -> new MatcherOperator<>(
							it.getMatcher(),
							((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext()
						))
						.collect(Collectors.toList());

				List<ArbitraryManipulator> registeredPropertyManipulators =
					monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
						registeredPropertyArbitraryBuilderContexts,
						objectTree.getMetadata().getNodesByProperty()
					);

				List<ArbitraryManipulator> registeredManipulators = new ArrayList<>();
				registeredManipulators.addAll(registeredRootManipulators);
				registeredManipulators.addAll(registeredPropertyManipulators);

				List<ArbitraryManipulator> joinedManipulators =
					Stream.concat(registeredManipulators.stream(), activeManipulators.stream())
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
			activeContext::isValidOnly
		);
	}
}
