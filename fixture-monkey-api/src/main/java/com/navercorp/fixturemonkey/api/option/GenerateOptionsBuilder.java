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

package com.navercorp.fixturemonkey.api.option;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.module.Module;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptionsBuilder {
	private List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators =
		new ArrayList<>(GenerateOptions.DEFAULT_ARBITRARY_PROPERTY_GENERATORS);
	private ArbitraryPropertyGenerator defaultArbitraryPropertyGenerator;
	private List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers = new ArrayList<>();
	private PropertyNameResolver defaultPropertyNameResolver;
	private List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators = new ArrayList<>();
	private NullInjectGenerator defaultNullInjectGenerator;
	private List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators = new ArrayList<>();
	private Integer defaultArbitraryContainerSize;
	private ArbitraryContainerInfo defaultArbitraryContainerInfo;
	private List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators = new ArrayList<>();
	private ArbitraryGenerator defaultArbitraryGenerator;

	GenerateOptionsBuilder() {
	}

	public GenerateOptionsBuilder arbitraryPropertyGenerators(
		List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators
	) {
		this.arbitraryPropertyGenerators = arbitraryPropertyGenerators;
		return this;
	}

	public GenerateOptionsBuilder insertFirstArbitraryPropertyGenerator(
		MatcherOperator<ArbitraryPropertyGenerator> arbitraryPropertyGenerator
	) {
		List<MatcherOperator<ArbitraryPropertyGenerator>> result =
			insertFirst(this.arbitraryPropertyGenerators, arbitraryPropertyGenerator);
		return this.arbitraryPropertyGenerators(result);
	}

	public GenerateOptionsBuilder insertFirstArbitraryPropertyGenerator(
		Matcher matcher,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		return this.insertFirstArbitraryPropertyGenerator(new MatcherOperator<>(matcher, arbitraryPropertyGenerator));
	}

	public GenerateOptionsBuilder insertFirstArbitraryPropertyGenerator(
		Class<?> type,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		return this.insertFirstArbitraryPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryPropertyGenerator)
		);
	}

	public GenerateOptionsBuilder defaultArbitraryPropertyGenerator(
		ArbitraryPropertyGenerator defaultArbitraryPropertyGenerator
	) {
		this.defaultArbitraryPropertyGenerator = defaultArbitraryPropertyGenerator;
		return this;
	}

	public GenerateOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		return this;
	}

	public GenerateOptionsBuilder insertFirstPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		List<MatcherOperator<PropertyNameResolver>> result =
			insertFirst(this.propertyNameResolvers, propertyNameResolver);
		return this.propertyNameResolvers(result);
	}

	public GenerateOptionsBuilder insertFirstPropertyNameResolver(
		Matcher matcher,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(new MatcherOperator<>(matcher, propertyNameResolver));
	}

	public GenerateOptionsBuilder insertFirstPropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(
			MatcherOperator.assignableTypeMatchOperator(type, propertyNameResolver)
		);
	}

	public GenerateOptionsBuilder defaultPropertyNameResolver(PropertyNameResolver defaultPropertyNameResolver) {
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		return this;
	}

	public GenerateOptionsBuilder nullInjectGenerators(
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators
	) {
		this.nullInjectGenerators = nullInjectGenerators;
		return this;
	}

	public GenerateOptionsBuilder insertFirstNullInjectGenerators(
		MatcherOperator<NullInjectGenerator> nullInjectGenerator
	) {
		List<MatcherOperator<NullInjectGenerator>> result =
			insertFirst(this.nullInjectGenerators, nullInjectGenerator);
		return this.nullInjectGenerators(result);
	}

	public GenerateOptionsBuilder insertFirstNullInjectGenerators(
		Matcher matcher,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(new MatcherOperator<>(matcher, nullInjectGenerator));
	}

	public GenerateOptionsBuilder insertFirstNullInjectGenerators(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(
			MatcherOperator.assignableTypeMatchOperator(type, nullInjectGenerator)
		);
	}

	public GenerateOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator) {
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		return this;
	}

	public GenerateOptionsBuilder arbitraryContainerInfoGenerators(
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators
	) {
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		return this;
	}

	public GenerateOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> result =
			insertFirst(this.arbitraryContainerInfoGenerators, arbitraryContainerInfoGenerator);
		return this.arbitraryContainerInfoGenerators(result);
	}

	public GenerateOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Matcher matcher,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			new MatcherOperator<>(matcher, arbitraryContainerInfoGenerator)
		);
	}

	public GenerateOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Class<?> type,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryContainerInfoGenerator)
		);
	}

	public GenerateOptionsBuilder defaultArbitraryContainerSize(int defaultArbitraryContainerSize) {
		this.defaultArbitraryContainerSize = defaultArbitraryContainerSize;
		return this;
	}

	public GenerateOptionsBuilder defaultArbitraryContainerInfo(ArbitraryContainerInfo defaultArbitraryContainerInfo) {
		this.defaultArbitraryContainerInfo = defaultArbitraryContainerInfo;
		return this;
	}

	public GenerateOptionsBuilder arbitraryGenerators(
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators
	) {
		this.arbitraryGenerators = arbitraryGenerators;
		return this;
	}

	public GenerateOptionsBuilder insertFirstArbitraryGenerator(
		MatcherOperator<ArbitraryGenerator> arbitraryGenerator
	) {
		List<MatcherOperator<ArbitraryGenerator>> result =
			insertFirst(this.arbitraryGenerators, arbitraryGenerator);
		return this.arbitraryGenerators(result);
	}

	public GenerateOptionsBuilder insertFirstArbitraryGenerator(
		Matcher matcher,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			new MatcherOperator<>(matcher, arbitraryGenerator)
		);
	}

	public GenerateOptionsBuilder insertFirstArbitraryGenerator(
		Class<?> type,
		ArbitraryGenerator arbitraryGenerator
	) {
		return this.insertFirstArbitraryGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryGenerator)
		);
	}

	public GenerateOptionsBuilder insertFirstArbitraryIntrospector(
		Matcher matcher,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryGenerator(
			matcher,
			new DefaultArbitraryGenerator(arbitraryIntrospector)
		);
	}

	public GenerateOptionsBuilder insertFirstArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, new DefaultArbitraryGenerator(arbitraryIntrospector))
		);
	}

	public GenerateOptionsBuilder defaultArbitraryGenerator(ArbitraryGenerator defaultArbitraryGenerator) {
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		return this;
	}

	public GenerateOptionsBuilder module(Module module) {
		module.accept(this);
		return this;
	}

	public GenerateOptions build() {
		ArbitraryPropertyGenerator defaultArbitraryPropertyGenerator = defaultIfNull(
			this.defaultArbitraryPropertyGenerator,
			() -> GenerateOptions.DEFAULT_ARBITRARY_PROPERTY_GENERATOR
		);
		PropertyNameResolver defaultPropertyNameResolver = defaultIfNull(
			this.defaultPropertyNameResolver,
			() -> GenerateOptions.DEFAULT_PROPERTY_NAME_RESOLVER
		);
		NullInjectGenerator defaultNullInjectGenerator =
			defaultIfNull(this.defaultNullInjectGenerator, DefaultNullInjectGenerator::new);
		int defaultArbitraryContainerSize = defaultIfNull(
			this.defaultArbitraryContainerSize,
			() -> GenerateOptions.DEFAULT_ARBITRARY_CONTAINER_SIZE
		);
		ArbitraryContainerInfo defaultArbitraryContainerInfo =
			defaultIfNull(
				this.defaultArbitraryContainerInfo,
				() -> new ArbitraryContainerInfo(0, defaultArbitraryContainerSize)
			);
		ArbitraryGenerator defaultArbitraryGenerator =
			defaultIfNull(this.defaultArbitraryGenerator, () -> GenerateOptions.DEFAULT_ARBITRARY_GENERATOR);

		return new GenerateOptions(
			this.arbitraryPropertyGenerators,
			defaultArbitraryPropertyGenerator,
			this.propertyNameResolvers,
			defaultPropertyNameResolver,
			this.nullInjectGenerators,
			defaultNullInjectGenerator,
			this.arbitraryContainerInfoGenerators,
			defaultArbitraryContainerSize,
			defaultArbitraryContainerInfo,
			this.arbitraryGenerators,
			defaultArbitraryGenerator
		);
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
