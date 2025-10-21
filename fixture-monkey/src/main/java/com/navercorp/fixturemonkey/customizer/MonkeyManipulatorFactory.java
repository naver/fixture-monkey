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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.customizer.Values.Unique;
import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodePredicateResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.ObjectNode;
import com.navercorp.fixturemonkey.tree.StaticNodeResolver;

/**
 * It is for internal use only.
 */
@API(since = "0.4.10", status = Status.INTERNAL)
public final class MonkeyManipulatorFactory {
	private final AtomicInteger sequence;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;

	public MonkeyManipulatorFactory(
		AtomicInteger sequence,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators
	) {
		this.sequence = sequence;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.containerPropertyGenerators = containerPropertyGenerators;
	}

	public ArbitraryManipulator newArbitraryManipulator(
		NodeResolver nodeResolver,
		@Nullable Object value,
		int limit
	) {
		return new ArbitraryManipulator(
			nodeResolver,
			convertToNodeManipulator(value, limit)
		);
	}

	public ArbitraryManipulator newArbitraryManipulator(
		NodeResolver nodeResolver,
		@Nullable Object value
	) {
		return new ArbitraryManipulator(
			nodeResolver,
			convertToNodeManipulator(sequence.getAndIncrement(), value)
		);
	}

	public <T> ArbitraryManipulator newArbitraryManipulator(
		NodeResolver nodeResolver,
		Class<T> type,
		Predicate<T> filter,
		int limit
	) {
		return new ArbitraryManipulator(
			nodeResolver,
			new ApplyNodeCountManipulator(
				new NodeFilterManipulator(type, filter),
				limit
			)
		);
	}

	public <T> ArbitraryManipulator newArbitraryManipulator(
		NodeResolver nodeResolver,
		Function<CombinableArbitrary<? extends T>, CombinableArbitrary<? extends T>> arbitraryCustomizer
	) {
		if (arbitraryCustomizer == null) {
			return newArbitraryManipulator(nodeResolver, (Object)null);
		}

		return new ArbitraryManipulator(
			nodeResolver,
			new NodeCustomizerManipulator<>(arbitraryCustomizer)
		);
	}

	public ContainerInfoManipulator newContainerInfoManipulator(
		List<NextNodePredicate> nextNodePredicates,
		int min,
		int max
	) {
		int newSequence = sequence.getAndIncrement();

		return new ContainerInfoManipulator(
			nextNodePredicates,
			new ArbitraryContainerInfo(
				min,
				max
			),
			newSequence
		);
	}

	public List<ArbitraryManipulator> newRegisteredArbitraryManipulators( // TODO: Fragmented registered
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		Map<Property, List<ObjectNode>> nodesByType
	) {
		List<ArbitraryManipulator> manipulators = new ArrayList<>();

		for (Entry<Property, List<ObjectNode>> nodeByType : nodesByType.entrySet()) {
			Property property = nodeByType.getKey();
			List<ObjectNode> objectNodes = nodeByType.getValue();

			ArbitraryBuilderContext activeContext = findRegisteredArbitraryBuilderContext(
				standbyContexts,
				property
			);

			if (activeContext == null) {
				continue;
			}

			List<ArbitraryManipulator> arbitraryManipulators = activeContext.getManipulators().stream()
				.map(
					it -> new ArbitraryManipulator(
						new CompositeNodeResolver(new StaticNodeResolver(objectNodes), it.getNodeResolver()),
						it.getNodeManipulator()
					)
				)
				.collect(toList());

			manipulators.addAll(arbitraryManipulators);
		}
		return manipulators;
	}

	private ArbitraryBuilderContext findRegisteredArbitraryBuilderContext(
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		Property property
	) {
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> priorityOperators = standbyContexts
			.stream()
			.filter(it -> it.match(property))
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.collect(toList());

		List<PriorityMatcherOperator<ArbitraryBuilderContext>> highestPriorityOperators
			= getHighestPriorityOperators(priorityOperators);

		if (highestPriorityOperators.size() > 1) {
			Collections.shuffle(highestPriorityOperators, Randoms.current());
		}

		return highestPriorityOperators.stream()
			.findFirst()
			.map(MatcherOperator::getOperator)
			.orElse(null);
	}

	private List<PriorityMatcherOperator<ArbitraryBuilderContext>> getHighestPriorityOperators(
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> priorityOperators
	) {
		if (priorityOperators.isEmpty()) {
			return priorityOperators;
		}

		int highestPriority = priorityOperators.get(0).getPriority();

		return priorityOperators.stream()
			.filter(it -> it.getPriority() == highestPriority)
			.collect(toList());
	}

	public ManipulatorSet newManipulatorSet(ManipulatorHolderSet manipulatorHolderSet) {
		int baseSequence = sequence.getAndIncrement();

		List<ArbitraryManipulator> arbitraryManipulators = new ArrayList<>();

		List<ArbitraryManipulator> setArbitraryManipulators = manipulatorHolderSet.getNodeResolverObjectHolders()
			.stream()
			.map(
				it -> {
					List<NodeResolver> nextNodeResolvers = it.getNextNodePredicates().stream()
						.map(NodePredicateResolver::new)
						.collect(toList());

					CompositeNodeResolver compositeNodeResolver = new CompositeNodeResolver(nextNodeResolvers);
					return new ArbitraryManipulator(
						compositeNodeResolver,
						convertToNodeManipulator(baseSequence + it.getSequence(), it.getValue())
					);
				}
			)
			.collect(toList());

		List<ArbitraryManipulator> filterArbitraryManipulators = manipulatorHolderSet.getPostConditionManipulators()
			.stream()
			.map(
				it -> {
					List<NodeResolver> nextNodeResolvers = it.getNextNodePredicates().stream()
						.map(NodePredicateResolver::new)
						.collect(toList());

					CompositeNodeResolver compositeNodeResolver = new CompositeNodeResolver(nextNodeResolvers);
					return new ArbitraryManipulator(
						compositeNodeResolver,
						new NodeFilterManipulator(it.getType(), it.getPredicate())
					);
				}
			)
			.collect(toList());
		arbitraryManipulators.addAll(setArbitraryManipulators);
		arbitraryManipulators.addAll(filterArbitraryManipulators);

		List<ContainerInfoManipulator> containerInfoManipulators = manipulatorHolderSet.getContainerInfoManipulators()
			.stream()
			.map(it -> new ContainerInfoManipulator(
				it.getNextNodePredicates(),
				new ArbitraryContainerInfo(
					it.getElementMinSize(),
					it.getElementMaxSize()
				),
				baseSequence + it.getSequence()
			))
			.collect(toList());

		sequence.set(sequence.get() + containerInfoManipulators.size() + arbitraryManipulators.size());
		return new ManipulatorSet(
			arbitraryManipulators,
			containerInfoManipulators
		);
	}

	public MonkeyManipulatorFactory copy() {
		return new MonkeyManipulatorFactory(
			new AtomicInteger(sequence.get()),
			decomposedContainerValueFactory,
			containerPropertyGenerators
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
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample())
			);
		} else if (value instanceof DefaultArbitraryBuilder) {
			return new NodeSetLazyManipulator<>(
				sequence,
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				LazyArbitrary.lazy(() -> ((DefaultArbitraryBuilder<?>)value).sample())
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				sequence,
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				LazyArbitrary.lazy((Supplier<?>)value)
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				sequence,
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				(LazyArbitrary<?>)value
			);
		} else if (value instanceof Unique) {
			return new NodeSetJustManipulator(
				Values.just(CombinableArbitrary.from(((Unique)value).getValueSupplier()).unique())
			);
		} else {
			return new NodeSetDecomposedValueManipulator<>(
				sequence,
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				value
			);
		}
	}
}
