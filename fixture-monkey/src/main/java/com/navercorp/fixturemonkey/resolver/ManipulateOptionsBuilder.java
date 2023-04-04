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
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.expression.ArbitraryExpressionFactory;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ManipulateOptionsBuilder {
	private MonkeyExpressionFactory defaultMonkeyExpressionFactory;

	private boolean expressionStrictMode = false;

	private List<MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredArbitraryBuilders = new ArrayList<>();

	private List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredSampledArbitraryBuilders = new ArrayList<>();

	private DecomposedContainerValueFactory additionalDecomposedContainerValueFactory = null;

	private List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;

	private PropertyNameResolver defaultPropertyNameResolver;

	ManipulateOptionsBuilder() {
	}

	public ManipulateOptionsBuilder expressionStrictMode(boolean expressionStrictMode) {
		this.expressionStrictMode = expressionStrictMode;
		return this;
	}

	public ManipulateOptionsBuilder monkeyExpressionFactory(MonkeyExpressionFactory monkeyExpressionFactory) {
		this.defaultMonkeyExpressionFactory = monkeyExpressionFactory;
		return this;
	}

	public ManipulateOptionsBuilder register(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> arbitraryBuilderSupplier
	) {
		registeredArbitraryBuilders = insertFirst(registeredArbitraryBuilders, arbitraryBuilderSupplier);
		return this;
	}

	public ManipulateOptionsBuilder additionalDecomposedContainerValueFactory(
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		this.additionalDecomposedContainerValueFactory = additionalDecomposedContainerValueFactory;
		return this;
	}

	public ManipulateOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		return this;
	}

	public ManipulateOptionsBuilder defaultPropertyNameResolver(PropertyNameResolver defaultPropertyNameResolver) {
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		return this;
	}

	public ManipulateOptions build() {
		defaultMonkeyExpressionFactory = defaultIfNull(
			this.defaultMonkeyExpressionFactory,
			ArbitraryExpressionFactory::new
		);

		if (expressionStrictMode) {
			MonkeyExpressionFactory currentMonkeyExpressionFactory = defaultMonkeyExpressionFactory;
			defaultMonkeyExpressionFactory = expression ->
				() -> new ApplyStrictModeResolver(currentMonkeyExpressionFactory.from(expression).toNodeResolver());
		}

		DecomposedContainerValueFactory decomposedContainerValueFactory = new DefaultDecomposedContainerValueFactory(
			additionalDecomposedContainerValueFactory
		);

		return new ManipulateOptions(
			defaultMonkeyExpressionFactory,
			registeredSampledArbitraryBuilders,
			decomposedContainerValueFactory,
			propertyNameResolvers,
			defaultPropertyNameResolver
		);
	}

	public void sampleRegisteredArbitraryBuilder(FixtureMonkey fixtureMonkey) {
		for (MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder
			: registeredArbitraryBuilders) {
			registeredSampledArbitraryBuilders.add(
				new MatcherOperator<>(
					registeredArbitraryBuilder.getMatcher(),
					registeredArbitraryBuilder.getOperator().apply(fixtureMonkey)
				)
			);
		}
	}

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}

	private static <T> List<T> insertFirst(List<T> list, T value) {
		List<T> result = new ArrayList<>();
		result.add(value);
		result.addAll(list);
		return result;
	}
}
