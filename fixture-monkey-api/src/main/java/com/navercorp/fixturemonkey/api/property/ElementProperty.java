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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
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
		JvmType elementType,
		@Nullable Integer index,
		int sequence
	) {
		this.containerProperty = containerProperty;
		this.elementType = elementType;
		this.index = index;
		this.sequence = sequence;
		this.annotationsMap = this.elementType.getAnnotations().stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public JvmType getJvmType() {
		return this.elementType;
	}

	public Property getContainerProperty() {
		return this.containerProperty;
	}

	@Override
	public Property getElementProperty() {
		return this;
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

	@Override
	public boolean equals(@Nullable Object obj) {
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
	@SuppressWarnings("argument")
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
