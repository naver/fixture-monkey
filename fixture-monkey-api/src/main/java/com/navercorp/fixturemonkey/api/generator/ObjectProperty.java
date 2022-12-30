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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ObjectProperty {
	private final Property property;

	private final PropertyNameResolver propertyNameResolver;

	private final double nullInject;

	@Nullable
	private final Integer elementIndex;

	private final Map<Property, List<Property>> childPropertyListsByCandidateProperty;

	public ObjectProperty(
		Property property,
		PropertyNameResolver propertyNameResolver,
		double nullInject,
		@Nullable Integer elementIndex,
		Map<Property, List<Property>> childPropertyListsByCandidateProperty
	) {
		this.property = property;
		this.propertyNameResolver = propertyNameResolver;
		this.nullInject = nullInject;
		this.elementIndex = elementIndex;
		this.childPropertyListsByCandidateProperty = childPropertyListsByCandidateProperty;
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

	public double getNullInject() {
		return this.nullInject;
	}

	@Nullable
	public Integer getElementIndex() {
		return this.elementIndex;
	}

	public Map.Entry<Property, List<Property>> getChildPropertiesByResolvedProperty(Matcher matcher) {
		for (
			Entry<Property, List<Property>> childPropertyListByPossibleProperty :
			childPropertyListsByCandidateProperty.entrySet()
		) {
			Property property = childPropertyListByPossibleProperty.getKey();
			if (matcher.match(property)) {
				return childPropertyListByPossibleProperty;
			}
		}
		throw new IllegalArgumentException("No resolved property is found.");
	}

	public Map<Property, List<Property>> getChildPropertyListsByCandidateProperty() {
		return childPropertyListsByCandidateProperty;
	}

	public boolean isRoot() {
		return this.property instanceof RootProperty;
	}

	public ObjectProperty withNullInject(double nullInject) {
		return new ObjectProperty(
			this.property,
			this.propertyNameResolver,
			nullInject,
			this.elementIndex,
			this.childPropertyListsByCandidateProperty
		);
	}

	public ObjectProperty withChildPropertyListsByCandidateProperty(
		Map<Property, List<Property>> childPropertyListsByCandidateProperty
	) {
		return new ObjectProperty(
			this.property,
			this.propertyNameResolver,
			this.nullInject,
			this.elementIndex,
			childPropertyListsByCandidateProperty
		);
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
			&& Double.compare(that.nullInject, nullInject) == 0
			&& Objects.equals(elementIndex, that.elementIndex)
			&& childPropertyListsByCandidateProperty.equals(that.childPropertyListsByCandidateProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			property,
			getResolvedPropertyName(),
			nullInject,
			elementIndex,
			childPropertyListsByCandidateProperty
		);
	}
}
