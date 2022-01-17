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
import java.lang.reflect.Field;
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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class FieldProperty implements Property {
	private final Field field;
	private final List<Annotation> annotations;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public FieldProperty(Field field) {
		this.field = field;
		this.annotations = Arrays.asList(field.getAnnotations());
		this.annotationsMap = this.annotations.stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	public Field getField() {
		return this.field;
	}

	@Override
	public Class<?> getType() {
		return this.field.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.field.getAnnotatedType();
	}

	@Override
	public String getName() {
		return this.field.getName();
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
			this.field.setAccessible(true);
			return this.field.get(obj);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException(
				"Can not extract value. obj: " + obj.toString() + ", fieldName: " + this.field.getName(),
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
		FieldProperty that = (FieldProperty)obj;
		return Objects.equals(this.field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.field);
	}

	@Override
	public String toString() {
		return "FieldProperty{field='" + this.field + '}';
	}
}
