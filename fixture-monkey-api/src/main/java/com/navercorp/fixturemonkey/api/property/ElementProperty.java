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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * It is deprecated.
 * Use {@link DefaultContainerElementProperty}, {@link SingleElementProperty} instead.
 */
@Deprecated
@API(since = "0.4.0", status = Status.DEPRECATED)
public class ElementProperty implements ContainerElementProperty {
	private final Property containerProperty;

	private final JvmType elementType;

	@Nullable
	private final Integer index;

	private final int sequence;

	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public ElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	) {
		this.containerProperty = containerProperty;
		this.elementType = Types.toJvmType(elementType, Collections.emptyList());
		this.index = index;
		this.sequence = sequence;
		this.annotationsMap = this.elementType.getAnnotations().stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.elementType.getRawType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.elementType.getAnnotatedType();
	}

	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this;
	}

	public AnnotatedType getElementType() {
		return this.elementType.getAnnotatedType();
	}

	@Nullable
	public Integer getIndex() {
		return this.index;
	}

	public int getSequence() {
		return sequence;
	}

	@Override
	@Nullable
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.elementType.getAnnotations();
	}

	@Override
	public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return Optional.ofNullable(this.annotationsMap.get(annotationClass))
			.map(annotationClass::cast);
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ElementProperty that = (ElementProperty)obj;
		return containerProperty.equals(that.containerProperty)
			&& elementType.equals(that.elementType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementType);
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
}
