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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FieldProperty implements Property {
	private final JvmType jvmType;
	private final Field field;
	private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

	public FieldProperty(Field field) {
		this(TypeCache.getAnnotatedType(field), field);
	}

	/**
	 * In general, annotatedType uses the AnnotatedType of Field.
	 * When the Type of Field is defined as generics, the refied type is not known.
	 * Use this constructor when specifying a Type that provides a refied Generics type.
	 *
	 * @param annotatedType annotatedType of the property
	 * @param field         field of the property
	 * @see com.navercorp.fixturemonkey.api.type.Types
	 */
	public FieldProperty(AnnotatedType annotatedType, Field field) {
		this.jvmType = Types.toJvmType(
			annotatedType,
			Arrays.stream(field.getAnnotations()).collect(Collectors.toList())
		);
		this.field = field;
		this.annotationsMap = this.getAnnotations().stream()
			.collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (a1, a2) -> a1));
	}

	public Field getField() {
		return this.field;
	}

	@Override
	public Type getType() {
		return this.jvmType.getRawType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.jvmType.getAnnotatedType();
	}

	@Override
	public String getName() {
		return this.field.getName();
	}

	@Override
	public List<Annotation> getAnnotations() {
		return jvmType.getAnnotations();
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
		FieldProperty that = (FieldProperty)obj;

		return jvmType.equals(that.jvmType)
			&& getAnnotations().equals(that.getAnnotations());
	}

	@Override
	public int hashCode() {
		return Objects.hash(jvmType);
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		try {
			this.field.setAccessible(true);
			return this.field.get(instance);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException(
				"Can not extract value. obj: " + instance.toString() + ", fieldName: " + this.field.getName(),
				ex
			);
		}
	}

	@Override
	public String toString() {
		return "FieldProperty{"
			+ "annotatedType=" + getAnnotatedType()
			+ ", field=" + field + '}';
	}
}
