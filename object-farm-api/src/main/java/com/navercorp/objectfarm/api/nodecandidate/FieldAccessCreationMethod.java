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

import java.lang.reflect.Field;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Property is set via direct field access using reflection.
 * <p>
 * This creation method is typically used with no-arg constructor instantiation
 * followed by reflective field setting, such as in {@code FieldReflectionArbitraryIntrospector}.
 * <p>
 * Example:
 * <pre>{@code
 * CreationMethod method = new FieldAccessCreationMethod(nameField);
 * Field field = ((FieldAccessCreationMethod) method).getField();
 * field.setAccessible(true);
 * field.set(instance, value);
 * }</pre>
 */
public final class FieldAccessCreationMethod implements CreationMethod {
	private final Field field;

	/**
	 * Creates a new FieldAccessCreationMethod.
	 *
	 * @param field the field used for direct access
	 */
	public FieldAccessCreationMethod(Field field) {
		this.field = Objects.requireNonNull(field, "Field must not be null");
	}

	@Override
	public CreationMethodType getType() {
		return CreationMethodType.FIELD;
	}

	/**
	 * Returns the field used for direct access.
	 *
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		FieldAccessCreationMethod that = (FieldAccessCreationMethod)obj;
		return Objects.equals(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field);
	}

	@Override
	public String toString() {
		return "FieldAccessCreationMethod{field=" + field.getDeclaringClass().getSimpleName()
			+ "." + field.getName() + "}";
	}
}
