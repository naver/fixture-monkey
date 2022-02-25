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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class GenerateOptions {
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final double nullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	public GenerateOptions(
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		double nullInject,
		boolean nullableContainer,
		boolean defaultNotNull
	) {
		this.arbitraryGenerators = arbitraryGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.nullInject = nullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
	}

	public List<MatcherOperator<ArbitraryGenerator>> getArbitraryGenerators() {
		return this.arbitraryGenerators;
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers;
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

	// TODO: equals and hashCode and toString
}
