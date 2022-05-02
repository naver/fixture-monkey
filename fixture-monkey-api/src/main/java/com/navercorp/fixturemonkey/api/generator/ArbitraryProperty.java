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

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryProperty {
	private final Property property;

	private final PropertyNameResolver propertyNameResolver;

	private final double nullInject;

	@Nullable
	private final Integer elementIndex;

	private final List<Property> childProperties;

	private final boolean container;

	public ArbitraryProperty(
		Property property,
		PropertyNameResolver propertyNameResolver,
		double nullInject,
		@Nullable Integer elementIndex,
		List<Property> childProperties,
		boolean container
	) {
		this.property = property;
		this.propertyNameResolver = propertyNameResolver;
		this.nullInject = nullInject;
		this.elementIndex = elementIndex;
		this.childProperties = childProperties;
		this.container = container;
	}

	public Property getProperty() {
		return this.property;
	}

	public PropertyNameResolver getPropertyNameResolver() {
		return this.propertyNameResolver;
	}

	public String getResolvePropertyName() {
		return this.getPropertyNameResolver().resolve(this.property);
	}

	public double getNullInject() {
		return this.nullInject;
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	public List<Property> getChildProperties() {
		return this.childProperties;
	}

	public boolean isContainer() {
		return this.container;
	}

	public boolean isRoot() {
		return this.property instanceof RootProperty;
	}
}
