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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.IdentityNodeResolver;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.ManipulateOptionsBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.MonkeyContext;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class LabMonkey extends FixtureMonkey {
	private final GenerateOptions generateOptions;
	private final ManipulateOptionsBuilder manipulateOptionsBuilder;
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final ArbitraryValidator validator;
	private final MonkeyContext monkeyContext;

	@SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
	public LabMonkey(
		GenerateOptions generateOptions,
		ManipulateOptionsBuilder manipulateOptionsBuilder,
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		ArbitraryValidator validator,
		MonkeyContext monkeyContext
	) {
		super(null, null, null, null, null);
		this.generateOptions = generateOptions;
		this.manipulateOptionsBuilder = manipulateOptionsBuilder;
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.validator = validator;
		this.monkeyContext = monkeyContext;
		manipulateOptionsBuilder.propertyNameResolvers(generateOptions.getPropertyNameResolvers());
		manipulateOptionsBuilder.defaultPropertyNameResolver(generateOptions.getDefaultPropertyNameResolver());
		manipulateOptionsBuilder.sampleRegisteredArbitraryBuilder(this);
	}

	/**
	 * Equivalent to {@code LabMonkey.builder().build()}
	 * @return LabMonkey
	 */
	public static LabMonkey create() {
		return new LabMonkeyBuilder().build();
	}

	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(Class<T> type) {
		TypeReference<T> typeReference = new TypeReference<T>(type) {
		};
		return giveMeBuilder(typeReference);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
		ManipulateOptions manipulateOptions = manipulateOptionsBuilder.build();
		RootProperty rootProperty = new RootProperty(type.getAnnotatedType());

		ArbitraryBuilder<?> registered = manipulateOptions.getRegisteredArbitraryBuilders().stream()
			.filter(it -> it.match(rootProperty))
			.map(MatcherOperator::getOperator)
			.findAny()
			.orElse(null);

		if (registered != null) {
			return (DefaultArbitraryBuilder<T>)registered.copy();
		}

		return new DefaultArbitraryBuilder<>(
			manipulateOptions,
			rootProperty,
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions,
				manipulateOptions,
				monkeyContext
			),
			traverser,
			this.validator,
			new ArbitraryBuilderContext()
		);
	}

	@Override
	public <T> DefaultArbitraryBuilder<T> giveMeBuilder(T value) {
		ManipulateOptions manipulateOptions = manipulateOptionsBuilder.build();
		ArbitraryBuilderContext context = new ArbitraryBuilderContext();
		context.addManipulator(
			new ArbitraryManipulator(
				IdentityNodeResolver.INSTANCE,
				new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value)
			)
		);

		return new DefaultArbitraryBuilder<>(
			manipulateOptions,
			new RootProperty(new LazyAnnotatedType<>(() -> value)),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				generateOptions,
				manipulateOptions,
				monkeyContext
			),
			traverser,
			this.validator,
			context
		);
	}

	@Override
	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMeBuilder(type).build().sampleStream();
	}

	@Override
	public <T> Stream<T> giveMe(TypeReference<T> typeReference) {
		return this.giveMeBuilder(typeReference).build().sampleStream();
	}

	@Override
	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type).limit(size).collect(toList());
	}

	@Override
	public <T> List<T> giveMe(TypeReference<T> typeReference, int size) {
		return this.giveMe(typeReference).limit(size).collect(toList());
	}

	@Override
	public <T> T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	@Override
	public <T> T giveMeOne(TypeReference<T> typeReference) {
		return this.giveMe(typeReference, 1).get(0);
	}

	@Override
	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type).build();
	}

	@Override
	public <T> Arbitrary<T> giveMeArbitrary(TypeReference<T> typeReference) {
		return this.giveMeBuilder(typeReference).build();
	}

	public <T> Stream<T> giveMe(Class<T> type, FixtureCustomizer<T> customizer) {
		return this.giveMeBuilder(type)
			.customize(MatcherOperator.exactTypeMatchOperator(type, customizer))
			.sampleStream();
	}

	public <T> List<T> giveMe(Class<T> type, int size, FixtureCustomizer<T> customizer) {
		return this.giveMe(type, customizer).limit(size).collect(toList());
	}

	public <T> T giveMeOne(Class<T> type, FixtureCustomizer<T> customizer) {
		return this.giveMe(type, 1, customizer).get(0);
	}
}
