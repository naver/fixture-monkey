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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class PropertyDescriptorProperty implements Property {
	private final PropertyDescriptor propertyDescriptor;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public PropertyDescriptorProperty(PropertyDescriptor propertyDescriptor) {
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
		return this.propertyDescriptor.getPropertyType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.propertyDescriptor.getReadMethod().getAnnotatedReturnType();
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
		return Objects.equals(this.propertyDescriptor, that.propertyDescriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.propertyDescriptor);
	}

	@Override
	public String toString() {
		return "PropertyDescriptorProperty{propertyDescriptor='" + this.propertyDescriptor + '}';
	}
}
