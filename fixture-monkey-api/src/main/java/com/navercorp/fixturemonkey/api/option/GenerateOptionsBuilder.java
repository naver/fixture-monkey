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
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptionsBuilder {
	private List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators =
		new ArrayList<>(GenerateOptions.DEFAULT_ARBITRARY_PROPERTY_GENERATORS);
	private List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers = new ArrayList<>();
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

	public GenerateOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		return this;
	}

	public GenerateOptionsBuilder nullInjectGenerators(
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators
	) {
		this.nullInjectGenerators = nullInjectGenerators;
		return this;
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

	public GenerateOptionsBuilder defaultArbitraryGenerator(ArbitraryGenerator defaultArbitraryGenerator) {
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		return this;
	}

	public GenerateOptions build() {
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
			defaultIfNull(this.defaultArbitraryGenerator, DefaultArbitraryGenerator::new);

		return new GenerateOptions(
			this.arbitraryPropertyGenerators,
			this.propertyNameResolvers,
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
}
