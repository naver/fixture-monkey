package com.navercorp.fixturemonkey.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Optional;

import javax.annotation.Nullable;

public final class AnnotationSource {
	@Nullable
	private final AnnotatedType annotatedType;

	public AnnotationSource(@Nullable AnnotatedType annotatedType) {
		this.annotatedType = annotatedType;
	}

	public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
		if (annotatedType == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(annotatedType.getAnnotation(annotationClass));
	}
}
