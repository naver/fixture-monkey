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

package com.navercorp.objectfarm.api.input;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod.CreationMethodType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Represents a member of a {@link SyntheticJvmType}.
 * <p>
 * This class defines a member (similar to a field) for types created from
 * schema definitions (JSON Schema, TypeScript-like syntax) rather than
 * actual Java classes.
 * <p>
 * Note: This is intentionally named "Member" instead of "Property" to avoid
 * confusion with the existing Property concept in Fixture Monkey.
 * <p>
 * Example usage:
 * <pre>{@code
 * SyntheticMember member = new SyntheticMember("name", new JavaType(String.class));
 * }</pre>
 */
public final class SyntheticMember {
	private final String name;
	private final JvmType type;
	private final CreationMethodType creationMethodType;

	/**
	 * Creates a new SyntheticMember with default creation method type (FIELD).
	 *
	 * @param name the member name
	 * @param type the member type
	 */
	public SyntheticMember(String name, JvmType type) {
		this(name, type, CreationMethodType.FIELD);
	}

	/**
	 * Creates a new SyntheticMember with a specified creation method type.
	 *
	 * @param name               the member name
	 * @param type               the member type
	 * @param creationMethodType the creation method type for this member
	 */
	public SyntheticMember(String name, JvmType type, CreationMethodType creationMethodType) {
		this.name = Objects.requireNonNull(name, "Member name must not be null");
		this.type = Objects.requireNonNull(type, "Member type must not be null");
		this.creationMethodType = Objects.requireNonNull(creationMethodType, "Creation method type must not be null");
	}

	/**
	 * Returns the member name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the member type.
	 *
	 * @return the JvmType
	 */
	public JvmType getType() {
		return type;
	}

	/**
	 * Returns the creation method type for this member.
	 *
	 * @return the creation method type
	 */
	public CreationMethodType getCreationMethodType() {
		return creationMethodType;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SyntheticMember that = (SyntheticMember)obj;
		return Objects.equals(name, that.name)
			&& Objects.equals(type, that.type)
			&& creationMethodType == that.creationMethodType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, creationMethodType);
	}

	@Override
	public String toString() {
		return "SyntheticMember{name='" + name + "', type=" + type + ", creationMethodType=" + creationMethodType + "}";
	}
}
