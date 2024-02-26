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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryProperty {
	private final ObjectProperty objectProperty;
	private final boolean container;
	private final double nullInject;
	private final Map<Property, List<Property>> childPropertyListsByCandidateProperty;

	@Deprecated
	public ArbitraryProperty(ObjectProperty objectProperty, boolean container) {
		this.objectProperty = objectProperty;
		this.container = container;
		this.nullInject = objectProperty.getNullInject();
		this.childPropertyListsByCandidateProperty = objectProperty.getChildPropertyListsByCandidateProperty();
	}

	public ArbitraryProperty(
		ObjectProperty objectProperty,
		boolean container,
		double nullInject,
		Map<Property, List<Property>> childPropertyListsByCandidateProperty
	) {
		this.objectProperty = objectProperty;
		this.container = container;
		this.nullInject = nullInject;
		this.childPropertyListsByCandidateProperty = childPropertyListsByCandidateProperty;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
	}

	public boolean isContainer() {
		return container;
	}

	public ArbitraryProperty withNullInject(double nullInject) {
		return new ArbitraryProperty(this.objectProperty.withNullInject(nullInject), this.container);
	}

	public double getNullInject() {
		return nullInject;
	}

	public Map<Property, List<Property>> getChildPropertyListsByCandidateProperty() {
		return childPropertyListsByCandidateProperty;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryProperty that = (ArbitraryProperty)obj;
		return objectProperty.equals(that.objectProperty) && container == that.container;
	}

	@Override
	public int hashCode() {
		return Objects.hash(objectProperty, container);
	}
}
