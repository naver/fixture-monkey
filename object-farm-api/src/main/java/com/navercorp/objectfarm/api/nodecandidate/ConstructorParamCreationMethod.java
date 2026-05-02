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

package com.navercorp.objectfarm.api.nodecandidate;

import java.lang.reflect.Constructor;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Property is passed as a constructor parameter.
 * <p>
 * This creation method is used when values are passed to a constructor,
 * such as in {@code ConstructorPropertiesArbitraryIntrospector} or
 * {@code ConstructorArbitraryIntrospector}.
 * <p>
 * Example:
 * <pre>{@code
 * CreationMethod method = new ConstructorParamCreationMethod(constructor, 0);
 * Constructor<?> ctor = ((ConstructorParamCreationMethod) method).getConstructor();
 * int idx = ((ConstructorParamCreationMethod) method).getParameterIndex();
 * Object[] args = new Object[ctor.getParameterCount()];
 * args[idx] = value;
 * ctor.newInstance(args);
 * }</pre>
 */
public final class ConstructorParamCreationMethod implements CreationMethod {
	private final Constructor<?> constructor;
	private final int parameterIndex;

	/**
	 * Creates a new ConstructorParamCreationMethod.
	 *
	 * @param constructor    the constructor used for instantiation
	 * @param parameterIndex the index of the parameter in the constructor
	 */
	public ConstructorParamCreationMethod(Constructor<?> constructor, int parameterIndex) {
		this.constructor = Objects.requireNonNull(constructor, "Constructor must not be null");
		if (parameterIndex < 0) {
			throw new IllegalArgumentException("Parameter index must be non-negative");
		}
		this.parameterIndex = parameterIndex;
	}

	@Override
	public CreationMethodType getType() {
		return CreationMethodType.CONSTRUCTOR;
	}

	/**
	 * Returns the constructor used for instantiation.
	 *
	 * @return the constructor
	 */
	public Constructor<?> getConstructor() {
		return constructor;
	}

	/**
	 * Returns the index of the parameter in the constructor.
	 *
	 * @return the parameter index (0-based)
	 */
	public int getParameterIndex() {
		return parameterIndex;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ConstructorParamCreationMethod that = (ConstructorParamCreationMethod)obj;
		return parameterIndex == that.parameterIndex && Objects.equals(constructor, that.constructor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(constructor, parameterIndex);
	}

	@Override
	public String toString() {
		return "ConstructorParamCreationMethod{constructor=" + constructor.getDeclaringClass().getSimpleName()
			+ ", parameterIndex=" + parameterIndex + "}";
	}
}
