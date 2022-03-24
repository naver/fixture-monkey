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

import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptions {
	private final List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;
	private final double nullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	public GenerateOptions(
		List<MatcherOperator<ArbitraryPropertyGenerator>> arbitraryPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		double nullInject,
		boolean nullableContainer,
		boolean defaultNotNull
	) {
		this.arbitraryPropertyGenerators = arbitraryPropertyGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.arbitraryGenerators = arbitraryGenerators;
		this.nullInject = nullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
	}

	public List<MatcherOperator<ArbitraryPropertyGenerator>> getArbitraryPropertyGenerators() {
		return this.arbitraryPropertyGenerators;
	}

	public ArbitraryPropertyGenerator getArbitraryPropertyGenerator(Property property) {
		return this.getArbitraryPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElseGet(ObjectArbitraryPropertyGenerator::new);
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers;
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.getPropertyNameResolvers().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(PropertyNameResolver.IDENTITY);
	}

	public List<MatcherOperator<ArbitraryGenerator>> getArbitraryGenerators() {
		return this.arbitraryGenerators;
	}

	public ArbitraryGenerator getArbitraryGenerator(Property property) {
		// TODO: if can not find default ArbitraryGenerator
		return this.getArbitraryGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Can not find ArbitraryGenerator."));
	}

	public double getNullInject() {
		return this.nullInject;
	}

	public boolean isNullableContainer() {
		return this.nullableContainer;
	}

	public boolean isDefaultNotNull() {
		return this.defaultNotNull;
	}

	public double getContainerNullInject() {
		if (this.isNullableContainer()) {
			return this.getNullInject();
		}

		return 0.0;
	}

	// TODO: equals and hashCode and toString
}
