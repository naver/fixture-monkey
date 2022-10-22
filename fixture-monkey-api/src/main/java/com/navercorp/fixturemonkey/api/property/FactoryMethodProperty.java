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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.2", status = Status.EXPERIMENTAL)
public final class FactoryMethodProperty implements Property {
	private final AnnotatedType annotatedType;
	private final Method factoryMethod;
	private final String parameterName;
	@Nullable
	private final Property fieldProperty;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public FactoryMethodProperty(
		AnnotatedType annotatedType,
		Method factoryMethod,
		String parameterName,
		@Nullable Property fieldProperty
	) {
		this.annotatedType = annotatedType;
		this.factoryMethod = factoryMethod;
		this.parameterName = parameterName;
		this.fieldProperty = fieldProperty;
		this.annotations = Arrays.asList(annotatedType.getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	public Method getFactoryMethod() {
		return factoryMethod;
	}

	@Nullable
	public Property getFieldProperty() {
		return fieldProperty;
	}

	@Override
	public String getName() {
		return this.parameterName;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		FactoryMethodProperty that = (FactoryMethodProperty)obj;
		return annotatedType.equals(that.annotatedType)
			&& factoryMethod.equals(that.factoryMethod)
			&& parameterName.equals(that.parameterName)
			&& Objects.equals(fieldProperty, that.fieldProperty)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotatedType, factoryMethod, parameterName, fieldProperty, annotations);
	}

	@Nullable
	@Override
	public Object getValue(Object obj) {
		if (fieldProperty != null) {
			return fieldProperty.getValue(obj);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "FactoryMethodProperty{"
			+ "annotatedType=" + annotatedType
			+ ", factoryMethod=" + factoryMethod
			+ ", parameterName='" + parameterName + '\''
			+ ", fieldProperty=" + fieldProperty
			+ '}';
	}
}
