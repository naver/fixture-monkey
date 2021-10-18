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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpressionManipulator;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryTraverser;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

public class FixtureMonkey {
	private final ArbitraryOption options;
	private final ArbitraryGenerator defaultGenerator;
	@SuppressWarnings("rawtypes")
	private final ArbitraryValidator validator;
	private final Map<Class<?>, ArbitraryGenerator> generatorMap;
	private final ArbitraryCustomizers arbitraryCustomizers;

	@SuppressWarnings("rawtypes")
	public FixtureMonkey(
		ArbitraryOption options,
		ArbitraryGenerator defaultGenerator,
		ArbitraryValidator validator,
		Map<Class<?>, ArbitraryGenerator> generatorMap,
		ArbitraryCustomizers arbitraryCustomizers
	) {
		this.options = options;
		this.defaultGenerator = defaultGenerator;
		this.validator = validator;
		this.generatorMap = generatorMap;
		this.arbitraryCustomizers = arbitraryCustomizers;
		options.applyArbitraryBuilders(this);
	}

	/**
	 * Equivalent to {@code FixtureMonkey.builder().build()}
	 * @return FixtureMonkey
	 */
	public static FixtureMonkey create() {
		return FixtureMonkey.builder().build();
	}

	public static FixtureMonkeyBuilder builder() {
		return new FixtureMonkeyBuilder();
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMeBuilder(type, options).build().sampleStream();
	}

	public <T> Stream<T> giveMe(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMeBuilder(type, options, customizer).build().sampleStream();
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type).limit(size).collect(toList());
	}

	public <T> List<T> giveMe(Class<T> type, int size, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, customizer).limit(size).collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	public <T> T giveMeOne(Class<T> type, ArbitraryCustomizer<T> customizer) {
		return this.giveMe(type, 1, customizer).get(0);
	}

	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type, options).build();
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz) {
		return this.giveMeBuilder(clazz, options);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		Consumer<BuilderManipulator> onManipulated
	) {
		return this.giveMeBuilder(
			clazz,
			options,
			this.arbitraryCustomizers,
			onManipulated
		);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> clazz, ArbitraryOption options) {
		return this.giveMeBuilder(clazz, options, this.arbitraryCustomizers);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		return new ArbitraryBuilder<>(
			() -> value,
			new ArbitraryTraverser(options),
			defaultGenerator,
			validator,
			this.arbitraryCustomizers,
			this.generatorMap,
			(it) -> {
			}
		);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption options,
		ArbitraryCustomizer<T> customizer
	) {
		ArbitraryCustomizers newArbitraryCustomizers =
			this.arbitraryCustomizers.mergeWith(Collections.singletonMap(clazz, customizer));

		return this.giveMeBuilder(clazz, options, newArbitraryCustomizers);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption option,
		ArbitraryCustomizers customizers
	) {
		return this.giveMeBuilder(
			clazz,
			option,
			customizers,
			(it) -> {
			}
		);
	}

	private <T> ArbitraryBuilder<T> giveMeBuilder(
		Class<T> clazz,
		ArbitraryOption option,
		ArbitraryCustomizers customizers,
		Consumer<BuilderManipulator> onManipulated
	) {
		ArbitraryBuilder<T> defaultArbitraryBuilder = option.getDefaultArbitraryBuilder(clazz);
		if (defaultArbitraryBuilder != null) {
			return defaultArbitraryBuilder;
		}

		return new ArbitraryBuilder<>(
			clazz,
			option,
			defaultGenerator,
			this.validator,
			customizers,
			this.generatorMap,
			onManipulated
		);
	}
}
