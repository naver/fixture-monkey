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

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ContainerInfoHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.FilterHolder;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.NodeResolverObjectHolder;
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
		boolean forced,
		int limit
	) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			convertToNodeManipulator(value, forced, limit)
		);
	}

	public ArbitraryManipulator newArbitraryManipulator(
		String expression,
		@Nullable Object value,
		boolean forced
	) {
		return new ArbitraryManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver(),
			convertToNodeManipulator(value, forced)
		);
	}

	public ArbitraryManipulator newArbitraryManipulator(NodeResolverObjectHolder objectHolder) {
		return new ArbitraryManipulator(
			objectHolder.getNodeResolver(),
			convertToNodeManipulator(objectHolder.getValue(), false)
		);
	}

	public ArbitraryManipulator newArbitraryManipulator(FilterHolder filterHolder) {
		return new ArbitraryManipulator(
			filterHolder.getNodeResolver(),
			new NodeFilterManipulator(
				filterHolder.getType(),
				filterHolder.getPredicate()
			)
		);
	}

	public ContainerInfoManipulator newContainerInfoManipulator(ContainerInfoHolder containerInfoHolder) {
		return new ContainerInfoManipulator(
			containerInfoHolder.getNodeResolver().toNextNodePredicate(),
			containerInfoHolder.getContainerInfo()
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
		return new ContainerInfoManipulator(
			monkeyExpressionFactory.from(expression).toNodeResolver().toNextNodePredicate(),
			new ArbitraryContainerInfo(
				min,
				max,
				true
			)
		);
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value, boolean forced, int limit) {
		NodeManipulator nodeManipulator = convertToNodeManipulator(value, forced);
		return new ApplyNodeCountManipulator(nodeManipulator, limit);
	}

	private NodeManipulator convertToNodeManipulator(@Nullable Object value, boolean forced) {
		if (value == null) {
			return new NodeNullityManipulator(true);
		} else if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample()),
				forced
			);
		} else if (value instanceof DefaultArbitraryBuilder) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((DefaultArbitraryBuilder<?>)value).sample()),
				forced
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy((Supplier<?>)value),
				forced
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				(LazyArbitrary<?>)value,
				forced
			);
		} else {
			return new NodeSetDecomposedValueManipulator<>(
				traverser,
				manipulateOptions,
				value,
				forced
			);
		}
	}
}
