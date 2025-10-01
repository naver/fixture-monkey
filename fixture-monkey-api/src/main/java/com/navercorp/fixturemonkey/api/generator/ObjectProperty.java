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

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectProperty {
	private final Property property;

	private final PropertyNameResolver propertyNameResolver;

	@Nullable
	private final Integer elementIndex;

	public ObjectProperty(
		Property property,
		PropertyNameResolver propertyNameResolver,
		@Nullable Integer elementIndex
	) {
		this.property = property;
		this.propertyNameResolver = propertyNameResolver;
		this.elementIndex = elementIndex;
	}

	public Property getProperty() {
		return this.property;
	}

	public PropertyNameResolver getPropertyNameResolver() {
		return this.propertyNameResolver;
	}

	public String getResolvedPropertyName() {
		return this.getPropertyNameResolver().resolve(this.property);
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	public boolean isRoot() {
		return this.property instanceof TreeRootProperty;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ObjectProperty that = (ObjectProperty)obj;
		return property.equals(that.property)
			&& Objects.equals(getResolvedPropertyName(), that.getResolvedPropertyName())
			&& Objects.equals(elementIndex, that.elementIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			property,
			getResolvedPropertyName(),
			elementIndex
		);
	}
}
