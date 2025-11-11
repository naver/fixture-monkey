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

package com.navercorp.fixturemonkey.api.property;

import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It is for internal use only. It can be changed or removed at any time.
 * <p>
 * Represents a concrete type definition with a resolved concrete property and a list of child properties.
 * Instances of this class are immutable once created.
 */
@API(since = "1.0.16", status = Status.INTERNAL)
public final class ConcreteTypeDefinition implements TypeDefinition {
	private final Property concreteProperty;
	private final List<Property> childPropertyLists;

	public ConcreteTypeDefinition(Property concreteProperty, List<Property> childPropertyLists) {
		this.concreteProperty = concreteProperty;
		this.childPropertyLists = childPropertyLists;
	}

	public Property getConcreteProperty() {
		return concreteProperty;
	}

	public List<Property> getChildPropertyLists() {
		return childPropertyLists;
	}

	@Override
	public Property getResolvedProperty() {
		return concreteProperty;
	}

	@Override
	public PropertyGenerator getPropertyGenerator() {
		return p -> childPropertyLists;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ConcreteTypeDefinition that = (ConcreteTypeDefinition)obj;
		return Objects.equals(concreteProperty, that.concreteProperty)
			&& Objects.equals(childPropertyLists, that.childPropertyLists);
	}

	@Override
	public int hashCode() {
		return Objects.hash(concreteProperty, childPropertyLists);
	}
}
