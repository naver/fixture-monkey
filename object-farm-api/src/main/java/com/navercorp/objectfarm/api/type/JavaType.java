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

package com.navercorp.objectfarm.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

public final class JavaType implements JvmType {
	private final Class<?> rawType;
	private final List<? extends JvmType> typeVariables;
	private final List<Annotation> annotations;
	@Nullable
	private final JvmType componentType;
	@Nullable
	private final Boolean nullable;
	private int cachedHashCode;

	public JavaType(Class<?> rawType) {
		this(rawType, Collections.emptyList(), Collections.emptyList());
	}

	public JavaType(Class<?> rawType, List<? extends JvmType> typeVariables, List<Annotation> annotations) {
		this(rawType, typeVariables, annotations, deriveComponentType(rawType, typeVariables), null);
	}

	public JavaType(
		Class<?> rawType,
		List<? extends JvmType> typeVariables,
		List<Annotation> annotations,
		@Nullable Boolean nullable
	) {
		this(rawType, typeVariables, annotations, deriveComponentType(rawType, typeVariables), nullable);
	}

	public JavaType(
		Class<?> rawType,
		List<? extends JvmType> typeVariables,
		List<Annotation> annotations,
		@Nullable JvmType componentType,
		@Nullable Boolean nullable
	) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.componentType = componentType;
		this.nullable = nullable;
	}

	public JavaType(ObjectTypeReference<?> typeReference) {
		AnnotatedType originalType = typeReference.getAnnotatedType();
		// Resolve wildcard types to their upper bound
		AnnotatedType resolvedType = Types.resolveWildcardType(originalType);
		this.rawType = resolveRawType(resolvedType);
		this.typeVariables = Types.getGenericsTypes(resolvedType).stream()
			.map(annotatedType -> new JavaType(Types.toTypeReference(annotatedType)))
			.collect(Collectors.toList());
		this.componentType = resolveComponentType(resolvedType, this.rawType);
		this.annotations = Arrays.stream(resolvedType.getAnnotations())
			.collect(Collectors.toList());
		this.nullable = null;
	}

	private static Class<?> resolveRawType(AnnotatedType annotatedType) {
		Type type = annotatedType.getType();
		if (type instanceof GenericArrayType) {
			// For GenericArrayType, we need the array class, not the component class
			GenericArrayType genericArrayType = (GenericArrayType)type;
			Class<?> componentClass = Types.getActualType(genericArrayType.getGenericComponentType());
			return Array.newInstance(componentClass, 0).getClass();
		}
		return Types.getActualType(annotatedType);
	}

	@Nullable
	private static JvmType resolveComponentType(AnnotatedType annotatedType, Class<?> rawType) {
		Type type = annotatedType.getType();
		if (type instanceof GenericArrayType) {
			Type genericComponentType = ((GenericArrayType)type).getGenericComponentType();
			return new JavaType(Types.toTypeReference(Types.generateAnnotatedTypeWithoutAnnotation(genericComponentType)));
		}
		if (rawType.isArray()) {
			return new JavaType(rawType.getComponentType());
		}
		return null;
	}

	@Nullable
	private static JvmType deriveComponentType(Class<?> rawType, List<? extends JvmType> typeVariables) {
		if (!rawType.isArray()) {
			return null;
		}
		Class<?> componentClass = rawType.getComponentType();
		// Preserve the legacy convention: when constructing an array type with typeVariables,
		// those typeVariables represent the component type's generics.
		return new JavaType(componentClass, typeVariables, Collections.emptyList());
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@Override
	public List<? extends JvmType> getTypeVariables() {
		return typeVariables;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	@Override
	@Nullable
	public Boolean getNullable() {
		return nullable;
	}

	@Override
	@Nullable
	public JvmType getComponentType() {
		return componentType;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JavaType javaType = (JavaType)obj;
		return Objects.equals(rawType, javaType.rawType)
			&& Objects.equals(typeVariables, javaType.typeVariables)
			&& Objects.equals(annotations, javaType.annotations)
			&& Objects.equals(componentType, javaType.componentType);
	}

	@Override
	public int hashCode() {
		int hash = cachedHashCode;
		if (hash == 0) {
			hash = Objects.hash(rawType, typeVariables, annotations, componentType);
			if (hash == 0) {
				hash = 1;
			}
			cachedHashCode = hash;
		}
		return hash;
	}
}
