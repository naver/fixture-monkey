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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.4.2", status = Status.MAINTAINED)
public final class ConstructorProperty implements Property {
	private final Property parameterProperty;
	private final Constructor<?> constructor;
	@Nullable
	private final Property fieldProperty;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;
	@Nullable
	private final Boolean nullable;

	/**
	 * It is deprecated.
	 * Use {@link #ConstructorProperty(Property, Constructor, Property, Boolean)} instead.
	 */
	@Deprecated
	public ConstructorProperty(
		AnnotatedType annotatedType,
		Constructor<?> constructor,
		String parameterName,
		@Nullable Property fieldProperty,
		@Nullable Boolean nullable
	) {
		this(new TypeNameProperty(annotatedType, parameterName, nullable), constructor, fieldProperty, nullable);
	}

	public ConstructorProperty(
		Property parameterProperty,
		Constructor<?> constructor,
		@Nullable Property fieldProperty,
		@Nullable Boolean nullable
	) {
		this.parameterProperty = parameterProperty;
		this.constructor = constructor;
		this.fieldProperty = fieldProperty;
		this.annotations = new ArrayList<>(parameterProperty.getAnnotations());
		if (fieldProperty != null) {
			this.annotations.addAll(fieldProperty.getAnnotations());
		}
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
		this.nullable = nullable;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.parameterProperty.getAnnotatedType();
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	@Nullable
	public Property getFieldProperty() {
		return fieldProperty;
	}

	@Override
	public String getName() {
		return this.parameterProperty.getName();
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
	public Boolean isNullable() {
		if (nullable != null) {
			return nullable;
		}

		return parameterProperty.isNullable();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ConstructorProperty that = (ConstructorProperty)obj;
		return parameterProperty.equals(that.parameterProperty.getType())
			&& constructor.equals(that.constructor)
			&& Objects.equals(fieldProperty, that.fieldProperty)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameterProperty, constructor, fieldProperty, annotations);
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		if (fieldProperty != null) {
			return fieldProperty.getValue(instance);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "ConstructorProperty{"
			+ "parameterProperty=" + parameterProperty
			+ ", constructor=" + constructor
			+ ", fieldProperty=" + fieldProperty
			+ ", annotations=" + annotations
			+ ", annotationsMap=" + annotationsMap
			+ ", nullable=" + nullable
			+ '}';
	}
}
