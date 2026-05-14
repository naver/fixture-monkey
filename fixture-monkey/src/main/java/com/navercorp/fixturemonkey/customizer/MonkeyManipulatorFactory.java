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
import java.util.List;
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
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.CustomizerDirective;
import com.navercorp.fixturemonkey.customizer.FilterDirective;
import com.navercorp.fixturemonkey.customizer.InnerSpecState.ManipulatorHolderSet;
import com.navercorp.fixturemonkey.customizer.JustDirective;
import com.navercorp.fixturemonkey.customizer.LazyDirective;
import com.navercorp.fixturemonkey.customizer.NullityDirective;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.SetDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.customizer.Values.Unique;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * It is for internal use only.
 */
@API(since = "0.4.10", status = Status.INTERNAL)
public final class MonkeyManipulatorFactory {

	private final AtomicInteger sequence;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;
	private final boolean expressionStrictMode;

	public MonkeyManipulatorFactory(
		AtomicInteger sequence,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators
	) {
		this(sequence, decomposedContainerValueFactory, containerPropertyGenerators, false);
	}

	public MonkeyManipulatorFactory(
		AtomicInteger sequence,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		boolean expressionStrictMode
	) {
		this.sequence = sequence;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.containerPropertyGenerators = containerPropertyGenerators;
		this.expressionStrictMode = expressionStrictMode;
	}

	public boolean isExpressionStrictMode() {
		return expressionStrictMode;
	}

	public PathDirective newDirective(PathExpression path, @Nullable Object value, int limit) {
		return convertToDirective(path, sequence.getAndIncrement(), value, limit, false);
	}

	public PathDirective newDirective(PathExpression path, @Nullable Object value) {
		return convertToDirective(path, sequence.getAndIncrement(), value, -1, false);
	}

	public <T> PathDirective newDirective(
		PathExpression path,
		Class<T> type,
		Predicate<T> filter,
		int limit
	) {
		return new FilterDirective(
			path,
			sequence.getAndIncrement(),
			limit,
			expressionStrictMode,
			false,
			type,
			filter
		);
	}

	public <T> PathDirective newDirective(
		PathExpression path,
		@Nullable Function<CombinableArbitrary<? extends T>, CombinableArbitrary<? extends T>> arbitraryCustomizer
	) {
		if (arbitraryCustomizer == null) {
			return newDirective(path, (Object)null);
		}

		return new CustomizerDirective<>(
			path,
			sequence.getAndIncrement(),
			-1,
			expressionStrictMode,
			false,
			arbitraryCustomizer
		);
	}

	public SizeDirective newSizeDirective(PathExpression path, int min, int max) {
		return new SizeDirective(path, sequence.getAndIncrement(), min, max);
	}

	public ManipulatorSet newManipulatorSet(ManipulatorHolderSet manipulatorHolderSet) {
		int baseSequence = sequence.getAndIncrement();

		List<PathDirective> directives = new ArrayList<>();

		List<PathDirective> setArbitraryManipulators = manipulatorHolderSet
			.getNodeResolverObjectHolders()
			.stream()
			.map(it -> convertToDirective(
				it.getPath(),
				baseSequence + it.getSequence(),
				it.getValue(),
				-1,
				false
			))
			.collect(toList());

		List<PathDirective> filterArbitraryManipulators = manipulatorHolderSet
			.getPostConditionManipulators()
			.stream()
			.map(it -> (PathDirective)new FilterDirective(
				it.getPath(),
				sequence.getAndIncrement(),
				-1,
				false,
				false,
				it.getType(),
				it.getPredicate()
			))
			.collect(toList());
		directives.addAll(setArbitraryManipulators);
		directives.addAll(filterArbitraryManipulators);

		List<PathDirective> sizeDirectives = manipulatorHolderSet
			.getSizeDirectives()
			.stream()
			.map(it -> (PathDirective)new SizeDirective(
				it.getPath(),
				baseSequence + it.getSequence(),
				new ArbitraryContainerInfo(it.getElementMinSize(), it.getElementMaxSize())
			))
			.collect(toList());
		directives.addAll(sizeDirectives);

		sequence.set(sequence.get() + directives.size());
		return new ManipulatorSet(directives);
	}

	public MonkeyManipulatorFactory copy() {
		return new MonkeyManipulatorFactory(
			new AtomicInteger(sequence.get()),
			decomposedContainerValueFactory,
			containerPropertyGenerators,
			expressionStrictMode
		);
	}

	private PathDirective convertToDirective(
		PathExpression path,
		int seq,
		@Nullable Object value,
		int limit,
		boolean registered
	) {
		if (value == null) {
			return new NullityDirective(path, seq, limit, expressionStrictMode, registered, true);
		} else if (value == Values.NOT_NULL) {
			return new NullityDirective(path, seq, limit, expressionStrictMode, registered, false);
		} else if (value instanceof Just) {
			return new JustDirective(path, seq, limit, expressionStrictMode, registered, (Just)value);
		} else if (value instanceof Arbitrary) {
			return new LazyDirective(
				path, seq, limit, expressionStrictMode, registered,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample()),
				decomposedContainerValueFactory,
				containerPropertyGenerators
			);
		} else if (value instanceof DefaultArbitraryBuilder) {
			return new LazyDirective(
				path, seq, limit, expressionStrictMode, registered,
				LazyArbitrary.lazy(() -> ((DefaultArbitraryBuilder<?>)value).sample()),
				decomposedContainerValueFactory,
				containerPropertyGenerators
			);
		} else if (value instanceof Supplier) {
			return new LazyDirective(
				path, seq, limit, expressionStrictMode, registered,
				LazyArbitrary.lazy((Supplier<?>)value),
				decomposedContainerValueFactory,
				containerPropertyGenerators
			);
		} else if (value instanceof LazyArbitrary) {
			return new LazyDirective(
				path, seq, limit, expressionStrictMode, registered,
				(LazyArbitrary<?>)value,
				decomposedContainerValueFactory,
				containerPropertyGenerators
			);
		} else if (value instanceof Unique) {
			return new JustDirective(
				path, seq, limit, expressionStrictMode, registered,
				Values.just(CombinableArbitrary.from(((Unique)value).getValueSupplier()).unique())
			);
		} else {
			return new SetDirective(
				path, seq, limit, expressionStrictMode, registered,
				value,
				decomposedContainerValueFactory,
				containerPropertyGenerators
			);
		}
	}
}
