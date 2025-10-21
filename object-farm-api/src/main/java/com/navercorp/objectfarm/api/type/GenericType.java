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

package com.navercorp.objectfarm.api.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Represents a parameterized type with a container type, type arguments, and an optional owner type.
 * <p>
 * It is mainly used for the type with type arguments.
 * For example, {@code List<String>}, {@code Map<String, Integer>}, {@code CustomClass<String>}
 * <p>
 * This class is marked as experimental and may change in future releases.
 */
public final class GenericType implements ParameterizedType {
	private final Type containerType;
	private final Type[] typeArguments;
	@Nullable
	private final Type ownerType;

	/**
	 * Constructs a new {@code GenericType} instance with the specified container type, resolved type arguments,
	 * and an optional owner type.
	 *
	 * @param containerType         the container type
	 * @param resolvedTypeArguments the resolved type arguments
	 * @param ownerType             the owner type, or {@code null} if there is no owner type
	 */
	public GenericType(Type containerType, Type[] resolvedTypeArguments, @Nullable Type ownerType) {
		this.containerType = containerType;
		this.ownerType = ownerType;
		this.typeArguments = resolvedTypeArguments;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return typeArguments;
	}

	@Override
	public Type getRawType() {
		return containerType;
	}

	@Override
	@Nullable
	public Type getOwnerType() {
		return this.ownerType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerType, ownerType, Arrays.hashCode(typeArguments));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ParameterizedType)) {
			return false;
		}

		ParameterizedType that = (ParameterizedType)obj;
		return this.containerType.equals(that.getRawType())
			&& Arrays.equals(this.typeArguments, that.getActualTypeArguments())
			&& Objects.equals(this.ownerType, that.getOwnerType());
	}
}
