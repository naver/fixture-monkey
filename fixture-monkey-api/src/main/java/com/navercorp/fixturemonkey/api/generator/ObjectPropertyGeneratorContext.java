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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectPropertyGeneratorContext {
	private final Property property;
	@Nullable
	private final Integer elementIndex;
	@Nullable
	private final ArbitraryProperty ownerProperty;
	private final boolean container;
	@Deprecated
	private final PropertyGenerator propertyGenerator;
	private final PropertyNameResolver propertyNameResolver;
	@Deprecated
	private final NullInjectGenerator nullInjectGenerator;

	@Deprecated
	public ObjectPropertyGeneratorContext(
		Property property,
		@Nullable Integer elementIndex,
		@Nullable ArbitraryProperty ownerProperty,
		boolean container,
		PropertyGenerator propertyGenerator,
		PropertyNameResolver propertyNameResolver,
		NullInjectGenerator nullInjectGenerator
	) {
		this.property = property;
		this.elementIndex = elementIndex;
		this.ownerProperty = ownerProperty;
		this.container = container;
		this.propertyGenerator = propertyGenerator;
		this.propertyNameResolver = propertyNameResolver;
		this.nullInjectGenerator = nullInjectGenerator;
	}

	public Property getProperty() {
		return this.property;
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	@Nullable
	public ArbitraryProperty getOwnerProperty() {
		return this.ownerProperty;
	}

	public boolean isContainer() {
		return this.container;
	}

	@Deprecated
	public PropertyGenerator getPropertyGenerator() {
		return propertyGenerator;
	}

	public PropertyNameResolver getPropertyNameResolver() {
		return propertyNameResolver;
	}

	@Deprecated
	public NullInjectGenerator getNullInjectGenerator() {
		return nullInjectGenerator;
	}

	public boolean isRootContext() {
		return this.property instanceof RootProperty;
	}
}
