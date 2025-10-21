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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class DefaultContainerElementProperty extends ElementProperty implements ContainerElementProperty {
	private final Property containerProperty;
	private final Property elementProperty;
	private final int sequence;
	@Nullable
	private final Integer index;

	public DefaultContainerElementProperty(
		Property containerProperty,
		Property elementProperty,
		@Nullable Integer index,
		int sequence
	) {
		super(containerProperty, elementProperty.getAnnotatedType(), index, sequence);
		this.containerProperty = containerProperty;
		this.elementProperty = elementProperty;
		this.index = index;
		this.sequence = sequence;
	}

	@Override
	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this.elementProperty;
	}

	@Override
	public int getSequence() {
		return this.sequence;
	}

	@Nullable
	@Override
	public Integer getIndex() {
		return this.index;
	}

	@Override
	public Type getType() {
		return this.elementProperty.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.elementProperty.getAnnotatedType();
	}

	@Nullable
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.elementProperty.getAnnotations();
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		// TODO: should split as a implementation of ContainerElementProperty
		Class<?> actualType = Types.getActualType(instance.getClass());
		if (isOptional(actualType)) {
			return getOptionalValue(instance);
		}

		if (actualType.isArray()) {
			if (Array.getLength(instance) == 0) {
				return null;
			}

			return Array.get(instance, sequence);
		}

		if (List.class.isAssignableFrom(actualType)) {
			List<?> list = (List<?>)instance;
			if (list.isEmpty()) {
				return null;
			}
			return list.get(sequence);
		}

		if (Supplier.class.isAssignableFrom(actualType)) {
			return instance;
		}

		throw new IllegalArgumentException("given element value has no match sequence : " + sequence);
	}

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}

	@Nullable
	private Object getOptionalValue(Object obj) {
		Class<?> actualType = Types.getActualType(obj.getClass());
		if (Optional.class.isAssignableFrom(actualType)) {
			return ((Optional<?>)obj).orElse(null);
		}

		if (OptionalInt.class.isAssignableFrom(actualType)) {
			return ((OptionalInt)obj).orElse(0);
		}

		if (OptionalLong.class.isAssignableFrom(actualType)) {
			return ((OptionalLong)obj).orElse(0L);
		}

		if (OptionalDouble.class.isAssignableFrom(actualType)) {
			return ((OptionalDouble)obj).orElse(Double.NaN);
		}

		throw new IllegalArgumentException("given value is not optional, actual type : " + actualType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DefaultContainerElementProperty that = (DefaultContainerElementProperty)obj;
		return sequence == that.sequence
			&& Objects.equals(containerProperty, that.containerProperty)
			&& Objects.equals(elementProperty, that.elementProperty)
			&& Objects.equals(index, that.index);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementProperty, sequence, index);
	}
}
