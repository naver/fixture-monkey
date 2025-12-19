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

package com.navercorp.fixturemonkey.api.tree;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeDefinition;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;

/**
 * It is for internal use. The breaking changes can happen often.
 * <p>
 * TreeProperty has immutable properties that consist of the object.
 */
@API(since = "1.1.0", status = Status.INTERNAL)
public class TreeProperty {
	private final ObjectProperty objectProperty;
	private final boolean container;
	private final List<? extends TypeDefinition> typeDefinitions;

	public TreeProperty(
		ObjectProperty objectProperty,
		boolean container,
		List<? extends TypeDefinition> typeDefinitions
	) {
		this.objectProperty = objectProperty;
		this.container = container;
		this.typeDefinitions = typeDefinitions;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
	}

	public boolean isContainer() {
		return container;
	}

	/**
	 * Retrieves the relationships between the parent property and child properties.
	 *
	 * @return the relationships between the parent property and child properties.
	 */
	public List<? extends TypeDefinition> getTypeDefinitions() {
		return typeDefinitions;
	}

	/**
	 * It can be converted into {@link ArbitraryProperty} for backward compatibility.
	 *
	 * @param nullInject the nullInject was in {@link ArbitraryProperty} but it is removed because it can be modified.
	 * @return the ArbitraryProperty
	 */
	public ArbitraryProperty toArbitraryProperty(double nullInject) {
		return new ArbitraryProperty(
			this.getObjectProperty(),
			this.isContainer(),
			nullInject,
			this.getTypeDefinitions().stream()
				.map(it -> new ConcreteTypeDefinition(
					it.getResolvedProperty(),
					it.getPropertyGenerator().generateChildProperties(it.getResolvedProperty())
				))
				.collect(Collectors.toList())
		);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TreeProperty that = (TreeProperty)obj;
		return container == that.container
			&& Objects.equals(objectProperty, that.objectProperty)
			&& Objects.equals(typeDefinitions, that.typeDefinitions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(objectProperty, container, typeDefinitions);
	}
}
