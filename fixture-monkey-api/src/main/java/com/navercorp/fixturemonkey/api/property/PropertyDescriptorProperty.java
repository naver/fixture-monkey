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

import static java.util.stream.Collectors.toMap;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.TypeCache;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class PropertyDescriptorProperty implements Property {
	private final AnnotatedType annotatedType;
	private final PropertyDescriptor propertyDescriptor;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public PropertyDescriptorProperty(PropertyDescriptor propertyDescriptor) {
		this(TypeCache.getAnnotatedType(propertyDescriptor), propertyDescriptor);
	}

	/**
	 * In general, annotatedType uses the PropertyType of PropertyDescriptor.
	 * When the Type of PropertyDescriptor is defined as generics, the refied type is not known.
	 * Use this constructor when specifying a Type that provides a refied Generics type.
	 *
	 * @see com.navercorp.fixturemonkey.api.type.Types
	 * @param annotatedType
	 * @param propertyDescriptor
	 */
	public PropertyDescriptorProperty(AnnotatedType annotatedType, PropertyDescriptor propertyDescriptor) {
		this.annotatedType = annotatedType;
		this.propertyDescriptor = propertyDescriptor;
		this.annotations = Arrays.asList(propertyDescriptor.getReadMethod().getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return this.propertyDescriptor;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	@Override
	public String getName() {
		return this.propertyDescriptor.getName();
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
		try {
			return this.propertyDescriptor.getReadMethod().invoke(obj);
		} catch (InvocationTargetException | IllegalAccessException ex) {
			throw new IllegalArgumentException(
				"Can not invoke value. obj: " + obj.toString() + ", propertyName: " + this.propertyDescriptor.getName(),
				ex
			);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PropertyDescriptorProperty that = (PropertyDescriptorProperty)obj;
		return annotatedType.getType().equals(that.annotatedType.getType())
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotatedType.getType(), annotations);
	}

	@Override
	public String toString() {
		return "PropertyDescriptorProperty{"
			+ "annotatedType=" + annotatedType
			+ ", propertyDescriptor=" + propertyDescriptor + '}';
	}
}
