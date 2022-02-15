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

import java.lang.reflect.Type;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryOptions {
	private final List<TypeMatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final ArbitraryGenerator arbitraryGenerator;
	private final double nullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	public ArbitraryOptions(
		List<TypeMatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		ArbitraryGenerator arbitraryGenerator,
		double nullInject,
		boolean nullableContainer,
		boolean defaultNotNull
	) {
		this.propertyNameResolvers = propertyNameResolvers;
		this.arbitraryGenerator = arbitraryGenerator;
		this.nullInject = nullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
	}

	public PropertyNameResolver getPropertyNameResolver(Type type) {
		return this.propertyNameResolvers.stream()
			.filter(it -> it.getTypeMatcher().match(type))
			.map(TypeMatcherOperator::getOperator)
			.findFirst()
			.orElse(PropertyNameResolver.IDENTITY);
	}

	public ArbitraryGenerator getArbitraryGenerator() {
		return this.arbitraryGenerator;
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
}
