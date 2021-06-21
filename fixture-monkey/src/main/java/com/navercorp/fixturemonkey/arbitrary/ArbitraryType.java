package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
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

public final class ArbitraryType<T> {
	private final Class<T> type;
	private final AnnotatedType annotatedType;
	private final List<Annotation> annotations;

	public ArbitraryType(Class<T> type, AnnotatedType annotatedType, List<Annotation> annotations) {
		this.type = type;
		this.annotatedType = annotatedType;
		this.annotations = Collections.unmodifiableList(annotations);
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
	public <U> ArbitraryType<U> getGenericFixtureType(int index) {
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
	public <U> ArbitraryType<U> getArrayFixtureType() {
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
		return isCollection()
			|| isMap()
			|| isMapEntry()
			|| isArray()
			|| isStream()
			|| isOptional()
			|| isIterator()
			|| isIterable();
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

	public boolean isNoGenericContainer() {
		return this.isContainer()
			&& !this.isArray()
			&& !(annotatedType instanceof AnnotatedParameterizedType);
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

	public Class<?> getType() {
		return type;
	}

	public AnnotatedType getAnnotatedType() {
		return annotatedType;
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
		if (annotatedType instanceof AnnotatedParameterizedType) {
			ParameterizedType parameterType = (ParameterizedType)annotatedType.getType();
			return (Class<?>)parameterType.getRawType();
		}

		return (Class<?>)annotatedType.getType();
	}
}
