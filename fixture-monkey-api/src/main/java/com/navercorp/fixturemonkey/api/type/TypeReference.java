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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public abstract class TypeReference<T> {
	private final AnnotatedType annotatedType;

	protected TypeReference() {
		AnnotatedType annotatedType = getClass().getAnnotatedSuperclass();
		this.annotatedType = ((AnnotatedParameterizedType)annotatedType).getAnnotatedActualTypeArguments()[0];
	}

	protected TypeReference(Class<T> type) {
		this.annotatedType = new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Nullable
			@Override
			public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}
		};
	}

	public Type getType() {
		return this.annotatedType.getType();
	}

	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	public boolean isNotGeneric() {
		return Types.getGenericsTypes(annotatedType).isEmpty();
	}
}
