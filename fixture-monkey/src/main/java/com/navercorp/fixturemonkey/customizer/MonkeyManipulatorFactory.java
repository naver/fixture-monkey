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

package com.navercorp.fixturemonkey.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.resolver.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.ObjectNode;
import com.navercorp.fixturemonkey.tree.PropertyPredicate;

@API(since = "0.4.10", status = Status.MAINTAINED)
public final class MonkeyManipulatorFactory {
	private final AtomicInteger sequence;
	private final MonkeyExpressionFactory monkeyExpressionFactory;
	private final ArbitraryTraverser traverser;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;

	public MonkeyManipulatorFactory(
		AtomicInteger sequence,
		MonkeyExpressionFactory monkeyExpressionFactory,
		ArbitraryTraverser traverser,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.sequence = sequence;
		this.monkeyExpressionFactory = monkeyExpressionFactory;
		this.traverser = traverser;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
	}

	public ArbitraryManipulator newArbitraryManipulator(
		String expression,
		@Nullable Object value,
		int limit
	) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			convertToNodeManipulator(value, limit)
		);
	}

	public ArbitraryManipulator newArbitraryManipulator(
		String expression,
		@Nullable Object value
	) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			convertToNodeManipulator(sequence.getAndIncrement(), value)
		);
	}

	public <T> ArbitraryManipulator newArbitraryManipulator(
		String expression,
		Class<T> type,
		Predicate<T> filter,
		int limit
	) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			new ApplyNodeCountManipulator(
				new NodeFilterManipulator(type, filter),
				limit
			)
		);
	}

	public ContainerInfoManipulator newContainerInfoManipulator(
		String expression,
		int min,
		int max
	) {
		int newSequence = sequence.getAndIncrement();

		return new ContainerInfoManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver().toNextNodePredicate(),
			new ArbitraryContainerInfo(
				min,
				max
			),
			newSequence
		);
	}

	public List<ArbitraryManipulator> newRegisteredArbitraryManipulators(
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders,
		Map<Property, List<ObjectNode>> nodesByType
	) {
		List<ArbitraryManipulator> manipulators = new ArrayList<>();

		for (Entry<Property, List<ObjectNode>> nodeByType : nodesByType.entrySet()) {
			Property property = nodeByType.getKey();
			List<ObjectNode> objectNodes = nodeByType.getValue();

			DefaultArbitraryBuilder<?> registeredArbitraryBuilder =
				(DefaultArbitraryBuilder<?>)registeredArbitraryBuilders.stream()
					.filter(it -> it.match(property))
					.findFirst()
					.map(MatcherOperator::getOperator)
					.filter(it -> it instanceof DefaultArbitraryBuilder<?>)
					.orElse(null);

			if (registeredArbitraryBuilder == null) {
				continue;
			}

			ArbitraryBuilderContext context = registeredArbitraryBuilder.getContext();
			List<ArbitraryManipulator> arbitraryManipulators = context.getManipulators().stream()
				.map(it -> it.withPrependNodeResolver(prependPropertyNodeResolver(property, objectNodes)))
				.collect(Collectors.toList());

			manipulators.addAll(arbitraryManipulators);
		}
		return manipulators;
	}

	public ManipulatorSet newManipulatorSet(ManipulatorHolderSet manipulatorHolderSet) {
		int baseSequence = sequence.getAndIncrement();

		List<ArbitraryManipulator> arbitraryManipulators = new ArrayList<>();

		List<ArbitraryManipulator> setArbitraryManipulators = manipulatorHolderSet.getNodeResolverObjectHolders()
			.stream()
			.map(it -> new ArbitraryManipulator(
					it.getNodeResolver(),
					convertToNodeManipulator(baseSequence + it.getSequence(), it.getValue())
				)
			)
			.collect(Collectors.toList());

		List<ArbitraryManipulator> filterArbitraryManipulators = manipulatorHolderSet.getPostConditionManipulators()
			.stream()
			.map(it -> new ArbitraryManipulator(
				it.getNodeResolver(),
				new NodeFilterManipulator(it.getType(), it.getPredicate())
			))
			.collect(Collectors.toList());
		arbitraryManipulators.addAll(setArbitraryManipulators);
		arbitraryManipulators.addAll(filterArbitraryManipulators);

		List<ContainerInfoManipulator> containerInfoManipulators = manipulatorHolderSet.getContainerInfoManipulators()
			.stream()
			.map(it -> new ContainerInfoManipulator(
				it.getNodeResolver().toNextNodePredicate(),
				new ArbitraryContainerInfo(
					it.getElementMinSize(),
					it.getElementMaxSize()
				),
				baseSequence + it.getSequence()
			))
			.collect(Collectors.toList());

		sequence.set(sequence.get() + containerInfoManipulators.size() + arbitraryManipulators.size());
		return new ManipulatorSet(
			arbitraryManipulators,
			containerInfoManipulators
		);
	}

	public MonkeyManipulatorFactory copy() {
		return new MonkeyManipulatorFactory(
			new AtomicInteger(sequence.get()),
			monkeyExpressionFactory,
			traverser,
			decomposedContainerValueFactory
		);
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value, int limit) {
		NodeManipulator nodeManipulator = convertToNodeManipulator(sequence.getAndIncrement(), value);
		return new ApplyNodeCountManipulator(nodeManipulator, limit);
	}

	private NodeManipulator convertToNodeManipulator(int sequence, @Nullable Object value) {
		if (value == null) {
			return new NodeNullityManipulator(true);
		} else if (value == Values.NOT_NULL) {
			return new NodeNullityManipulator(false);
		} else if (value instanceof Just) {
			return new NodeSetJustManipulator((Just)value);
		} else if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				decomposedContainerValueFactory,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample())
			);
		} else if (value instanceof DefaultArbitraryBuilder) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				decomposedContainerValueFactory,
				LazyArbitrary.lazy(() -> ((DefaultArbitraryBuilder<?>)value).sample())
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				decomposedContainerValueFactory,
				LazyArbitrary.lazy((Supplier<?>)value)
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				decomposedContainerValueFactory,
				(LazyArbitrary<?>)value
			);
		} else {
			return new NodeSetDecomposedValueManipulator<>(
				sequence,
				traverser,
				decomposedContainerValueFactory,
				value
			);
		}
	}

	private NodeResolver prependPropertyNodeResolver(Property property, List<ObjectNode> objectNodes) {
		return new NodeResolver() {
			@Override
			public List<ObjectNode> resolve(ObjectNode objectNode) {
				for (ObjectNode node : objectNodes) {
					ObjectNode parent = node.getParent();
					while (parent != null) {
						parent.setManipulated(true);
						parent = parent.getParent();
					}
					node.setManipulated(true);
				}
				return objectNodes;
			}

			@Override
			public List<NextNodePredicate> toNextNodePredicate() {
				return Collections.singletonList(new PropertyPredicate(property));
			}
		};
	}
}
