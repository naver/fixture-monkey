package com.navercorp.objectfarm.api.type;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;

public abstract class TypeReference<T> {
	private final AnnotatedType annotatedType;

	protected TypeReference() {
		AnnotatedType annotatedType = getClass().getAnnotatedSuperclass();
		this.annotatedType = ((AnnotatedParameterizedType)annotatedType).getAnnotatedActualTypeArguments()[0];
	}

	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}
}
