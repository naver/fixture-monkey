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

package com.navercorp.fixturemonkey.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.6.10", status = Status.INTERNAL)
public abstract class AnnotatedTypes {
	public static AnnotatedArrayType newAnnotatedArrayType(
		AnnotatedType annotatedGenericComponentType,
		Type type,
		Annotation[] annotations,
		Annotation[] declaredAnnotations,
		AnnotatedType annotatedOwnerType
	) {
		return new AnnotatedArrayType() {
			@Override
			public AnnotatedType getAnnotatedGenericComponentType() {
				return annotatedGenericComponentType;
			}

			@Override
			public Type getType() {
				return type;
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return Arrays.stream(annotations)
					.filter(it -> it.getClass() == annotationClass)
					.findAny()
					.map(annotationClass::cast)
					.orElse(null);
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return declaredAnnotations;
			}
		};
	}

	public static AnnotatedParameterizedType newAnnotatedParameterizedType(
		AnnotatedType[] annotatedActualTypeArguments,
		Type type,
		Annotation[] annotations,
		Annotation[] declaredAnnotations,
		AnnotatedType annotatedOwnerType
	) {
		return new AnnotatedParameterizedType() {
			@Override
			public AnnotatedType[] getAnnotatedActualTypeArguments() {
				return annotatedActualTypeArguments;
			}

			@Override
			public Type getType() {
				return type;
			}

			@SuppressWarnings("unchecked")
			@Override
			@Nullable
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return (T)Arrays.stream(annotations)
					.filter(it -> it.getClass() == annotationClass)
					.findAny()
					.orElse(null);
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return declaredAnnotations;
			}
		};
	}

	public static AnnotatedTypeVariable newAnnotatedTypeVariable(
		AnnotatedType currentAnnotatedType,
		List<AnnotatedType> genericsAnnotatedTypes
	) {
		return new AnnotatedTypeVariable() {
			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				AnnotatedType[] annotatedTypes = new AnnotatedType[1];
				annotatedTypes[0] = genericsAnnotatedTypes.get(0);
				return annotatedTypes;
			}

			@Override
			public Type getType() {
				return genericsAnnotatedTypes.get(0).getType();
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return currentAnnotatedType.getAnnotation(annotationClass);
			}

			@Override
			public Annotation[] getAnnotations() {
				return currentAnnotatedType.getAnnotations();
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return currentAnnotatedType.getDeclaredAnnotations();
			}
		};
	}
}
