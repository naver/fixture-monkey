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
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.collection.LruCache;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryResolver {
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;

	private final GenerateOptions generateOptions;
	private final ManipulateOptions manipulateOptions;
	private final LruCache<Property, Arbitrary<?>> arbitrariesByProperty;

	public ArbitraryResolver(
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		GenerateOptions generateOptions,
		ManipulateOptions manipulateOptions,
		LruCache<Property, Arbitrary<?>> arbitrariesByProperty
	) {
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.generateOptions = generateOptions;
		this.manipulateOptions = manipulateOptions;
		this.arbitrariesByProperty = arbitrariesByProperty;
	}

	@SuppressWarnings("rawtypes")
	public Arbitrary<?> resolve(
		RootProperty rootProperty,
		List<ArbitraryManipulator> manipulators,
		List<MatcherOperator<? extends FixtureCustomizer>> customizers,
		Map<NodeResolver, ArbitraryContainerInfo> containerInfosByNodeResolver
	) {
		ArbitraryTree arbitraryTree = new ArbitraryTree(
			this.traverser.traverse(rootProperty, containerInfosByNodeResolver),
			generateOptions,
			customizers,
			arbitrariesByProperty,
			generateOptions.getUniqueProperties()
		);

		containerInfosByNodeResolver.keySet().stream()
			.flatMap(it -> it.resolve(arbitraryTree.findRoot()).stream())
			.forEach(it -> it.setManipulated(true));

		List<ArbitraryManipulator> registeredManipulators = getRegisteredToManipulators(
			manipulateOptions,
			arbitraryTree.getMetadata()
		);

		List<ArbitraryManipulator> joinedManipulators =
			Stream.concat(registeredManipulators.stream(), manipulators.stream())
				.collect(Collectors.toList());

		List<ArbitraryManipulator> optimizedManipulator = manipulatorOptimizer
			.optimize(joinedManipulators)
			.getManipulators();

		for (ArbitraryManipulator manipulator : optimizedManipulator) {
			manipulator.manipulate(arbitraryTree);
		}

		return arbitraryTree.generate();
	}

	private List<ArbitraryManipulator> getRegisteredToManipulators(
		ManipulateOptions manipulateOptions,
		ArbitraryTreeMetadata metadata
	) {
		List<ArbitraryManipulator> manipulators = new ArrayList<>();
		Map<Property, List<ArbitraryNode>> nodesByType = metadata.getNodesByProperty();
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders =
			manipulateOptions.getRegisteredArbitraryBuilders();

		for (Entry<Property, List<ArbitraryNode>> nodeByType : nodesByType.entrySet()) {
			Property property = nodeByType.getKey();
			List<ArbitraryNode> arbitraryNodes = nodeByType.getValue();

			ArbitraryBuilder<?> registeredArbitraryBuilder = registeredArbitraryBuilders.stream()
				.filter(it -> it.match(property))
				.findFirst()
				.map(MatcherOperator::getOperator)
				.orElse(null);

			if (registeredArbitraryBuilder == null) {
				continue;
			}

			NodeManipulator nodeManipulator = new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(registeredArbitraryBuilder::sample)
			);
			manipulators.add(
				new ArbitraryManipulator(
					new NodeResolver() {
						@Override
						public List<ArbitraryNode> resolve(ArbitraryNode arbitraryNode) {
							for (ArbitraryNode node : arbitraryNodes) {
								node.setManipulated(true);
							}
							return arbitraryNodes;
						}

						@Override
						public List<NextNodePredicate> toNextNodePredicate() {
							return Collections.emptyList(); // Do not need node predicate since it is SetLazyManipulator
						}
					},
					nodeManipulator
				)
			);
		}
		return manipulators;
	}
}
