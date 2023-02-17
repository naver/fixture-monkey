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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.resolver.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeFilterManipulator;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;

@API(since = "0.4.10", status = Status.EXPERIMENTAL)
public final class MonkeyManipulatorFactory {
	public static final Object NOT_NULL = new Object();

	private final AtomicInteger sequence = new AtomicInteger();
	private final MonkeyExpressionFactory monkeyExpressionFactory;
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;

	public MonkeyManipulatorFactory(
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions
	) {
		this.monkeyExpressionFactory = manipulateOptions.getDefaultMonkeyExpressionFactory();
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
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

	// TODO: temporary use
	public ArbitraryManipulator newNotNullArbitraryManipulator(String expression) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			new NodeNullityManipulator(false)
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
				max,
				newSequence
			)
		);
	}

	ManipulatorSet newManipulatorSet(ManipulatorHolderSet manipulatorHolderSet) {
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
					it.getElementMaxSize(),
					baseSequence + it.getSequence()
				)
			))
			.collect(Collectors.toList());

		sequence.set(sequence.get() + containerInfoManipulators.size() + arbitraryManipulators.size());
		return new ManipulatorSet(
			arbitraryManipulators,
			containerInfoManipulators
		);
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value, int limit) {
		NodeManipulator nodeManipulator = convertToNodeManipulator(sequence.getAndIncrement(), value);
		return new ApplyNodeCountManipulator(nodeManipulator, limit);
	}

	private NodeManipulator convertToNodeManipulator(int sequence, @Nullable Object value) {
		if (value == null) {
			return new NodeNullityManipulator(true);
		} else if (value == NOT_NULL) {
			return new NodeNullityManipulator(false);
		} else if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample())
			);
		} else if (value instanceof DefaultArbitraryBuilder) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((DefaultArbitraryBuilder<?>)value).sample())
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy((Supplier<?>)value)
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				sequence,
				traverser,
				manipulateOptions,
				(LazyArbitrary<?>)value
			);
		} else {
			return new NodeSetDecomposedValueManipulator<>(
				sequence,
				traverser,
				manipulateOptions,
				value
			);
		}
	}
}
