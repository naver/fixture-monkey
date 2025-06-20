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

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;

/**
 * CreationMethod for synthetic type members.
 * <p>
 * Since synthetic types don't have actual Java reflection objects,
 * this only stores the intended creation method type and member name.
 * This is useful for schema-defined types (JSON Schema, TypeScript)
 * where the creation method is determined by the schema definition.
 * <p>
 * Example:
 * <pre>{@code
 * CreationMethod method = new SyntheticMemberCreationMethod(
 *     CreationMethodType.FIELD, "name"
 * );
 * }</pre>
 */
public final class SyntheticMemberCreationMethod implements CreationMethod {
	private final CreationMethodType type;
	private final String memberName;

	/**
	 * Creates a new SyntheticMemberCreationMethod.
	 *
	 * @param type       the intended creation method type
	 * @param memberName the name of the synthetic member
	 */
	public SyntheticMemberCreationMethod(CreationMethodType type, String memberName) {
		this.type = Objects.requireNonNull(type, "Creation method type must not be null");
		this.memberName = Objects.requireNonNull(memberName, "Member name must not be null");
	}

	@Override
	public CreationMethodType getType() {
		return type;
	}

	/**
	 * Returns the name of the synthetic member.
	 *
	 * @return the member name
	 */
	public String getMemberName() {
		return memberName;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SyntheticMemberCreationMethod that = (SyntheticMemberCreationMethod)obj;
		return type == that.type && Objects.equals(memberName, that.memberName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, memberName);
	}

	@Override
	public String toString() {
		return "SyntheticMemberCreationMethod{type=" + type + ", memberName='" + memberName + "'}";
	}
}
