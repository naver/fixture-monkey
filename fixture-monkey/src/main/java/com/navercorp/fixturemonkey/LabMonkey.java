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

package com.navercorp.fixturemonkey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.RootNodeResolver;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class LabMonkey extends FixtureMonkey {
	private final GenerateOptions generateOptions;
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final ArbitraryValidator validator;

	@SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
	public LabMonkey(
		GenerateOptions generateOptions,
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		ArbitraryValidator validator
	) {
		super(null, null, null, null, null);
		this.generateOptions = generateOptions;
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.validator = validator;
	}

	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(Class<T> type) {
		TypeReference<T> typeReference = new TypeReference<T>(type) {
		};
		return giveMeBuilder(typeReference);
	}

	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
		return new DefaultArbitraryBuilder<>(
			generateOptions,
			new RootProperty(type.getAnnotatedType()),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions
			),
			traverser,
			this.validator,
			new ArrayList<>(),
			new HashSet<>()
		);
	}

	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(T value) {
		List<ArbitraryManipulator> manipulators = new ArrayList<>();
		manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetDecomposedValueManipulator<>(traverser, value)
			)
		);

		return new DefaultArbitraryBuilder<>(
			generateOptions,
			new RootProperty(new LazyAnnotatedType<>(() -> value)),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions
			),
			traverser,
			this.validator,
			manipulators,
			new HashSet<>()
		);
	}
}
