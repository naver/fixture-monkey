package com.navercorp.objectfarm.api.type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaType implements JvmType {
	private final Class<?> rawType;
	private final List<JavaType> typeVariables;

	public JavaType(Class<?> rawType) {
		this(rawType, Collections.emptyList());
	}

	public JavaType(Class<?> rawType, List<JavaType> typeVariables) {
		this.rawType = rawType;
		this.typeVariables = typeVariables;
	}

	public JavaType(TypeReference<?> typeReference) {
		this.rawType = Types.getActualType(typeReference.getAnnotatedType());
		this.typeVariables = Types.getGenericsTypes(typeReference.getAnnotatedType()).stream()
			.map(annotatedType -> new JavaType(Types.toTypeReference(annotatedType)))
			.collect(Collectors.toList());
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@Override
	public List<JavaType> getTypeVariables() {
		return typeVariables;
	}
}
