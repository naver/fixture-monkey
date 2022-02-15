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

package com.navercorp.fixturemonkey.api.generator;

import java.util.List;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.TypeMatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryPropertyGeneratorContext {
	private final Property property;

	@Nullable
	private final Integer elementIndex;

	private final List<TypeMatcherOperator<PropertyNameResolver>> propertyNameResolvers;

	private final double nullInject;

	private final boolean nullableContainer;

	private final boolean defaultNotNull;

	public ArbitraryPropertyGeneratorContext(
		Property property,
		@Nullable Integer elementIndex,
		List<TypeMatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		double nullInject,
		boolean nullableContainer,
		boolean defaultNotNull
	) {
		this.property = property;
		this.elementIndex = elementIndex;
		this.propertyNameResolvers = propertyNameResolvers;
		this.nullInject = nullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
	}

	public Property getProperty() {
		return this.property;
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	public List<TypeMatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
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
}
