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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class PropertyDescriptorProperty implements Property {
	private final JvmType jvmType;
	private final PropertyDescriptor propertyDescriptor;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public PropertyDescriptorProperty(PropertyDescriptor propertyDescriptor) {
		this(TypeCache.getAnnotatedType(propertyDescriptor), propertyDescriptor);
	}

	/**
	 * In general, annotatedType uses the PropertyType of PropertyDescriptor.
	 * When the Type of PropertyDescriptor is defined as generics, the refied type is not known.
	 * Use this constructor when specifying a Type that provides a refied Generics type.
	 *
	 * @param annotatedType      annotatedType of the property
	 * @param propertyDescriptor propertyDescriptor of the property
	 * @see com.navercorp.fixturemonkey.api.type.Types
	 */
	public PropertyDescriptorProperty(AnnotatedType annotatedType, PropertyDescriptor propertyDescriptor) {
		List<Annotation> concatAnnotations = new ArrayList<>();
		if (propertyDescriptor.getWriteMethod() != null) {
			concatAnnotations.addAll(Arrays.asList(propertyDescriptor.getWriteMethod().getAnnotations()));
		}
		if (propertyDescriptor.getReadMethod() != null) {
			concatAnnotations.addAll(Arrays.asList(propertyDescriptor.getReadMethod().getAnnotations()));
		}
		this.jvmType = Types.toJvmType(annotatedType, concatAnnotations);
		this.propertyDescriptor = propertyDescriptor;
		this.annotationsMap = this.getAnnotations().stream()
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
		return this.jvmType.getAnnotatedType();
	}

	@Override
	public String getName() {
		return this.propertyDescriptor.getName();
	}

	@Override
	public List<Annotation> getAnnotations() {
		return this.jvmType.getAnnotations();
	}

	@Override
	public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
		return Optional.ofNullable(this.annotationsMap.get(annotationClass))
			.map(annotationClass::cast);
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		try {
			return this.propertyDescriptor.getReadMethod().invoke(instance);
		} catch (InvocationTargetException | IllegalAccessException ex) {
			throw new IllegalArgumentException(
				"Can not invoke value. obj: " + instance.toString() + ", propertyName: "
					+ this.propertyDescriptor.getName(),
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
		return jvmType.equals(that.jvmType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jvmType);
	}

	@Override
	public String toString() {
		return "PropertyDescriptorProperty{"
			+ "annotatedType=" + jvmType.getAnnotatedType()
			+ ", propertyDescriptor=" + propertyDescriptor + '}';
	}
}
