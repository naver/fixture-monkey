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
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryProperty {
	private final ObjectProperty objectProperty;

	@Nullable
	private final ContainerProperty containerProperty;

	public ArbitraryProperty(
		ObjectProperty objectProperty,
		@Nullable ContainerProperty containerProperty
	) {
		this.objectProperty = objectProperty;
		this.containerProperty = containerProperty;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
	}

	@Nullable
	public ContainerProperty getContainerProperty() {
		return containerProperty;
	}

	public ArbitraryProperty withNullInject(double nullInject) {
		return new ArbitraryProperty(
			this.objectProperty.withNullInject(nullInject),
			this.containerProperty
		);
	}

	public ArbitraryProperty withElementProperties(List<Property> elementProperties) {
		return new ArbitraryProperty(
			this.objectProperty,
			this.containerProperty.withElementProperties(elementProperties)
		);
	}

	public ArbitraryProperty withChildPropertyListsByCandidateProperty(
		Map<Property, List<Property>> childPropertyListsByCandidateProperty
	) {
		return new ArbitraryProperty(
			this.objectProperty.withChildPropertyListsByCandidateProperty(childPropertyListsByCandidateProperty),
			this.containerProperty
		);
	}

	public ArbitraryProperty withContainerInfo(ArbitraryContainerInfo containerInfo) {
		return new ArbitraryProperty(
			this.objectProperty,
			this.containerProperty.withContainerInfo(containerInfo)
		);
	}

	public ArbitraryProperty withContainerProperty(ContainerProperty containerProperty) {
		return new ArbitraryProperty(
			this.objectProperty,
			containerProperty
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
		ArbitraryProperty that = (ArbitraryProperty)obj;
		return objectProperty.equals(that.objectProperty)
			&& Objects.equals(containerProperty, that.containerProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(objectProperty, containerProperty);
	}
}
