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

package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.BaseStream;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

public class ArbitraryType<T> {
	private final Class<T> type;
	private final AnnotatedType annotatedType;
	private final List<Annotation> annotations;

	public ArbitraryType(Class<T> type, AnnotatedType annotatedType, List<Annotation> annotations) {
		this.type = type;
		this.annotatedType = annotatedType;
		this.annotations = Collections.unmodifiableList(annotations);
	}

	@SuppressWarnings("unchecked")
	public ArbitraryType(TypeReference<T> typeReference) {
		this.type = (Class<T>)Types.getActualType(typeReference.getType());
		this.annotatedType = typeReference.getAnnotatedType();
		this.annotations = Arrays.stream(annotatedType.getAnnotations()).collect(toList());

	}

	public ArbitraryType(Class<T> type) {
		this.type = type;
		this.annotatedType = null;
		this.annotations = Collections.emptyList();
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <U extends Annotation> U getAnnotation(Class<U> annotationType) {
		if (annotationType == null) {
			throw new IllegalArgumentException("annotationType not exists");
		}

		return (U)annotations.stream()
			.filter(it -> it.annotationType() == annotationType)
			.findAny()
			.orElse(null);
	}

	@SuppressWarnings("unchecked")
	public <U> ArbitraryType<U> getGenericArbitraryType(int index) {
		Class<U> childClazz = (Class<U>)this.findGenericType(index)
			.orElseThrow(() -> new IllegalArgumentException(index + "th childClazz not exists"));
		AnnotatedType childAnnotatedType = this.findGenericAnnotatedType(index)
			.orElseThrow(() -> new IllegalArgumentException(index + "th childAnnotatedType not exists"));
		return new ArbitraryType<>(
			childClazz,
			childAnnotatedType,
			Arrays.asList(childAnnotatedType.getAnnotations())
		);
	}

	@SuppressWarnings("unchecked")
	public <U> ArbitraryType<U> getArrayArbitraryType() {
		if (!this.isArray()) {
			throw new IllegalStateException("FixtureType is not array but getArrayFixtureType is called.");
		}

		AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType)this.getAnnotatedType();
		Class<?> arrayType = (Class<?>)annotatedArrayType.getType();

		Class<U> genericClazz = (Class<U>)arrayType.getComponentType();
		AnnotatedType genericAnnotatedType = annotatedArrayType.getAnnotatedGenericComponentType();

		return new ArbitraryType<>(genericClazz, genericAnnotatedType,
			Arrays.asList(genericAnnotatedType.getAnnotations()));
	}

	public boolean isContainer() {
		return isGenericType()
			&& (isCollection()
			|| isMap()
			|| isMapEntry()
			|| isArray()
			|| isStream()
			|| isOptional()
			|| isIterator()
			|| isIterable());
	}

	private boolean isCollection() {
		return Collection.class.isAssignableFrom(type);
	}

	private boolean isIterable() {
		return Iterable.class.isAssignableFrom(type);
	}

	private boolean isIterator() {
		return Iterator.class.isAssignableFrom(type);
	}

	public boolean isArray() {
		return annotatedType instanceof AnnotatedArrayType
			&& ((Class<?>)annotatedType.getType()).isArray();
	}

	public boolean isMapEntry() {
		return Map.Entry.class.isAssignableFrom(type);
	}

	public boolean isMap() {
		return Map.class.isAssignableFrom(type);
	}

	public boolean isGenericType() {
		return annotatedType instanceof AnnotatedArrayType
			|| annotatedType instanceof AnnotatedParameterizedType;
	}

	public boolean isOptional() {
		return Optional.class.isAssignableFrom(type);
	}

	public boolean isStream() {
		return BaseStream.class.isAssignableFrom(type);
	}

	public boolean isPrimitive() {
		return type.isPrimitive();
	}

	public boolean isEnum() {
		return type.isEnum();
	}

	public boolean isInterface() {
		return type.isInterface();
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(type.getModifiers());
	}

	public Class<?> getType() {
		return type;
	}

	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	private Optional<AnnotatedType> findGenericAnnotatedType(int index) {
		if (annotatedType == null) {
			return Optional.empty();
		}

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)annotatedType;
		AnnotatedType[] annotatedActualTypeArguments = parameterizedType.getAnnotatedActualTypeArguments();

		if (annotatedActualTypeArguments.length <= index) {
			return Optional.empty();
		}

		return Optional.of(annotatedActualTypeArguments[index]);
	}

	private Optional<Class<?>> findGenericType(int index) {
		return findGenericAnnotatedType(index).map(this::findGenericType);
	}

	private Class<?> findGenericType(AnnotatedType annotatedType) {
		return Types.getActualType(annotatedType.getType());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryType<?> that = (ArbitraryType<?>)obj;
		return type.equals(that.type)
			&& Objects.equals(annotatedType, that.annotatedType)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, annotatedType, annotations);
	}
}
