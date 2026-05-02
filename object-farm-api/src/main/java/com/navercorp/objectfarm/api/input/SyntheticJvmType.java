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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod.CreationMethodType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A synthetic JvmType that represents types defined through schemas
 * rather than actual Java classes.
 * <p>
 * This allows JSON Schema and TypeScript-like definitions to be used
 * within the existing type infrastructure without requiring actual
 * Java class generation.
 * <p>
 * Example usage:
 * <pre>{@code
 * SyntheticJvmType userType = SyntheticJvmType.builder("User")
 *     .member("name", new JavaType(String.class))
 *     .member("age", new JavaType(Integer.class))
 *     .build();
 * }</pre>
 */
public final class SyntheticJvmType implements JvmType {
	private final String typeName;
	private final List<SyntheticMember> members;

	private SyntheticJvmType(String typeName, List<SyntheticMember> members) {
		this.typeName = Objects.requireNonNull(typeName, "Type name must not be null");
		this.members = Collections.unmodifiableList(new ArrayList<>(members));
	}

	/**
	 * Creates a new builder for SyntheticJvmType.
	 *
	 * @param typeName the name of the synthetic type
	 * @return a new builder instance
	 */
	public static Builder builder(String typeName) {
		return new Builder(typeName);
	}

	/**
	 * Returns the name of this synthetic type.
	 *
	 * @return the type name
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Returns the members of this synthetic type.
	 *
	 * @return unmodifiable list of members
	 */
	public List<SyntheticMember> getMembers() {
		return members;
	}

	/**
	 * Checks if this type is synthetic (always returns true).
	 *
	 * @return true
	 */
	public boolean isSynthetic() {
		return true;
	}

	/**
	 * Returns a marker class for synthetic types.
	 * This is used to identify synthetic types in the type system.
	 *
	 * @return SyntheticObject.class as a marker
	 */
	@Override
	public Class<?> getRawType() {
		return SyntheticObject.class;
	}

	@Override
	public List<? extends JvmType> getTypeVariables() {
		return Collections.emptyList();
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SyntheticJvmType that = (SyntheticJvmType)obj;
		return Objects.equals(typeName, that.typeName) && Objects.equals(members, that.members);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeName, members);
	}

	@Override
	public String toString() {
		return "SyntheticJvmType{typeName='" + typeName + "', members=" + members + "}";
	}

	/**
	 * Builder for creating SyntheticJvmType instances.
	 */
	public static final class Builder {
		private final String typeName;
		private final List<SyntheticMember> members = new ArrayList<>();

		private Builder(String typeName) {
			this.typeName = typeName;
		}

		/**
		 * Adds a member to the synthetic type with default creation method type (FIELD).
		 *
		 * @param name the member name
		 * @param type the member type
		 * @return this builder
		 */
		public Builder member(String name, JvmType type) {
			this.members.add(new SyntheticMember(name, type));
			return this;
		}

		/**
		 * Adds a member to the synthetic type with a specified creation method type.
		 *
		 * @param name               the member name
		 * @param type               the member type
		 * @param creationMethodType the creation method type for this member
		 * @return this builder
		 */
		public Builder member(String name, JvmType type, CreationMethodType creationMethodType) {
			this.members.add(new SyntheticMember(name, type, creationMethodType));
			return this;
		}

		/**
		 * Adds a member to the synthetic type.
		 *
		 * @param member the member to add
		 * @return this builder
		 */
		public Builder member(SyntheticMember member) {
			this.members.add(member);
			return this;
		}

		/**
		 * Adds multiple members to the synthetic type.
		 *
		 * @param members the members to add
		 * @return this builder
		 */
		public Builder members(List<SyntheticMember> members) {
			this.members.addAll(members);
			return this;
		}

		/**
		 * Builds the SyntheticJvmType.
		 *
		 * @return the built SyntheticJvmType
		 */
		public SyntheticJvmType build() {
			return new SyntheticJvmType(typeName, members);
		}
	}

	/**
	 * Marker class to identify synthetic types.
	 * This class is used as the raw type for all SyntheticJvmType instances.
	 */
	public static final class SyntheticObject {
		private SyntheticObject() {
		}
	}
}
