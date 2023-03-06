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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ElementProperty implements Property {
	private final Property containerProperty;

	private final AnnotatedType elementType;

	@Nullable
	private final Integer index;

	private final int sequence;

	private final List<Annotation> annotations;

	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public ElementProperty(
		Property containerProperty,
		AnnotatedType elementType,
		@Nullable Integer index,
		int sequence
	) {
		this.containerProperty = containerProperty;
		this.elementType = elementType;
		this.index = index;
		this.sequence = sequence;
		this.annotations = Arrays.asList(this.elementType.getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.elementType;
	}

	public Property getContainerProperty() {
		return this.containerProperty;
	}

	public AnnotatedType getElementType() {
		return this.elementType;
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
		return this.annotations;
	}

	@Override
	public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return Optional.ofNullable(this.annotationsMap.get(annotationClass))
			.map(annotationClass::cast);
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		Class<?> actualType = Types.getActualType(obj.getClass());
		if (isOptional(actualType)) {
			return getOptionalValue(obj);
		}

		if (actualType.isArray()) {
			if (Array.getLength(obj) == 0) {
				return null;
			}

			return Array.get(obj, sequence);
		}

		if (List.class.isAssignableFrom(actualType)) {
			List<?> list = (List<?>)obj;
			if (list.isEmpty()) {
				return null;
			}
			return list.get(sequence);
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
			&& elementType.equals(that.elementType)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(containerProperty, elementType, annotations);
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
