package com.navercorp.objectfarm.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class JavaType implements JvmType {
	private final Class<?> rawType;
	private final List<JavaType> typeVariables;
	private final List<Annotation> annotations;
	@Nullable
	private final AnnotatedType annotatedType;

	public JavaType(Class<?> rawType) {
		this(rawType, Collections.emptyList(), Collections.emptyList(), null);
	}

	public JavaType(Class<?> rawType, List<JavaType> typeVariables, List<Annotation> annotations) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.annotatedType = null;
	}

	// for backward compatibility
	@Deprecated
	public JavaType(
		Class<?> rawType,
		List<JavaType> typeVariables,
		List<Annotation> annotations,
		AnnotatedType annotatedType
	) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.annotatedType = annotatedType;
	}

	public JavaType(TypeReference<?> typeReference) {
		this.rawType = Types.getActualType(typeReference.getAnnotatedType());
		this.typeVariables = Types.getGenericsTypes(typeReference.getAnnotatedType()).stream()
			.map(annotatedType -> new JavaType(Types.toTypeReference(annotatedType)))
			.collect(Collectors.toList());
		this.annotations = Arrays.stream(typeReference.getAnnotatedType().getAnnotations())
			.collect(Collectors.toList());
		this.annotatedType = typeReference.getAnnotatedType();
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@Override
	public List<JavaType> getTypeVariables() {
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
	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	@Override
	public boolean equals(Object obj) {
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
		return Objects.hash(rawType, typeVariables, annotations);
	}
}
