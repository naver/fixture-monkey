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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;

/**
 * It is a property for a fixed single element of a container. ex, Optional, Function, Supplier
 * It can be nested. For example, {@code Optional<Optional<String>>}.
 * <p>
 * The main differences between {@link SingleElementProperty} and {@link DefaultContainerElementProperty} are:
 * - {@link SingleElementProperty} is used for a fixed single element of a container.
 * It has no explict sequence and index. For example, {@code Optional<String> optional},
 * it can be referenced by {@code optional}.
 * - {@link DefaultContainerElementProperty} is used for an element of a container that can have multiple elements.
 * It has an explicit sequence and index. For example, {@code List<String> list},
 * it can be referenced by {@code list[0]}, {@code list[1]}.
 */
@API(since = "1.0.17", status = API.Status.EXPERIMENTAL)
public final class SingleElementProperty extends ElementProperty implements ContainerElementProperty {
	private final Property containerProperty;

	private final Property elementProperty;

	/**
	 * It is deprecated.
	 * Use {@link #SingleElementProperty(Property, Property)} instead.
	 */
	@Deprecated
	public SingleElementProperty(Property containerProperty) {
		super(containerProperty, containerProperty.getAnnotatedType(), null, 0);
		this.containerProperty = containerProperty;
		this.elementProperty = new TypeParameterProperty(containerProperty.getAnnotatedType());
	}

	public SingleElementProperty(Property containerProperty, Property elementProperty) {
		super(containerProperty, containerProperty.getAnnotatedType(), null, 0);
		this.containerProperty = containerProperty;
		this.elementProperty = elementProperty;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
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
		return this.containerProperty.getAnnotations();
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		Class<?> actualType = Types.getActualType(instance.getClass());

		if (isOptional(actualType)) {
			return getOptionalValue(instance);
		}

		return instance;
	}

	@Override
	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this;
	}

	@Override
	public int getSequence() {
		return 0;
	}

	@Nullable
	@Override
	public Integer getIndex() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SingleElementProperty that = (SingleElementProperty)obj;
		return Objects.equals(containerProperty, that.containerProperty)
			&& Objects.equals(elementProperty, that.elementProperty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementProperty);
	}

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}

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
}
