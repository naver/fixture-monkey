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
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionFactory;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ManipulateOptionsBuilder {
	private MonkeyExpressionFactory defaultMonkeyExpressionFactory;

	private boolean expressionStrictMode = false;

	private List<MatcherOperator<Function<LabMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredArbitraryBuilders = new ArrayList<>();

	private List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredSampledArbitraryBuilders = new ArrayList<>();

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
		MatcherOperator<Function<LabMonkey, ? extends ArbitraryBuilder<?>>> arbitraryBuilderSupplier
	) {
		registeredArbitraryBuilders = insertFirst(registeredArbitraryBuilders, arbitraryBuilderSupplier);
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

		return new ManipulateOptions(defaultMonkeyExpressionFactory, registeredSampledArbitraryBuilders);
	}

	public void sampleRegisteredArbitraryBuilder(LabMonkey labMonkey) {
		registeredSampledArbitraryBuilders = registeredArbitraryBuilders.stream()
			.map(operator -> new MatcherOperator<>(
					operator.getMatcher(),
					operator.getOperator().apply(labMonkey)
				)
			)
			.collect(Collectors.toList());
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
