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
	private final AnnotatedType annotatedType;
	private int cachedHashCode;

	public JavaType(Class<?> rawType) {
		this(rawType, Collections.emptyList(), Collections.emptyList(), null);
	}

	public JavaType(Class<?> rawType, List<? extends JvmType> typeVariables, List<Annotation> annotations) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.annotatedType = null;
	}

	// for backward compatibility
	@Deprecated
	public JavaType(
		Class<?> rawType,
		List<JvmType> typeVariables,
		List<Annotation> annotations,
		@Nullable AnnotatedType annotatedType
	) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.annotatedType = annotatedType;
	}

	public JavaType(ObjectTypeReference<?> typeReference) {
		AnnotatedType originalType = typeReference.getAnnotatedType();
		// Resolve wildcard types to their upper bound
		AnnotatedType resolvedType = Types.resolveWildcardType(originalType);
		this.rawType = resolveRawType(resolvedType);
		this.typeVariables = Types.getGenericsTypes(resolvedType).stream()
			.map(annotatedType -> new JavaType(Types.toTypeReference(annotatedType)))
			.collect(Collectors.toList());
		this.annotations = Arrays.stream(resolvedType.getAnnotations())
			.collect(Collectors.toList());
		// Don't store the annotatedType if it contains wildcards in type arguments,
		// so that JvmNodePropertyAdapter will build a new AnnotatedType from resolved components
		this.annotatedType = Types.containsWildcardTypeArguments(resolvedType) ? null : resolvedType;
	}

	private static Class<?> resolveRawType(AnnotatedType annotatedType) {
		Type type = annotatedType.getType();
		if (type instanceof GenericArrayType) {
			// For GenericArrayType, we need the array class, not the component class
			GenericArrayType genericArrayType = (GenericArrayType)type;
			Class<?> componentClass = Types.getActualType(genericArrayType.getGenericComponentType());
			return java.lang.reflect.Array.newInstance(componentClass, 0).getClass();
		}
		return Types.getActualType(annotatedType);
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

	/**
	 * It is for backward compatibility. Recommend to use the {@link #getRawType()} or {@link #getTypeVariables()}
	 */
	@Deprecated
	@Override
	@Nullable
	@SuppressWarnings("override.return")
	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	@Override
	@Nullable
	public JvmType getComponentType() {
		if (!rawType.isArray()) {
			return null;
		}
		Class<?> componentClass = rawType.getComponentType();
		// For array types, typeVariables holds the component type's type variables
		return new JavaType(componentClass, typeVariables, Collections.emptyList());
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		JavaType javaType = (JavaType)obj;
		return Objects.equals(rawType, javaType.rawType)
			&& Objects.equals(typeVariables, javaType.typeVariables)
			&& Objects.equals(annotations, javaType.annotations);
	}

	@Override
	public int hashCode() {
		int hash = cachedHashCode;
		if (hash == 0) {
			hash = Objects.hash(rawType, typeVariables, annotations);
			if (hash == 0) {
				hash = 1;
			}
			cachedHashCode = hash;
		}
		return hash;
	}
}
