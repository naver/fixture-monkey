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

package com.navercorp.fixturemonkey.jackson3.type;

import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@SuppressWarnings("unused")
@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public abstract class Jackson3TypeReference<T> extends tools.jackson.core.type.TypeReference<T> {
	private final AnnotatedType annotatedType;

	protected Jackson3TypeReference() {
		AnnotatedType annotatedType = getClass().getAnnotatedSuperclass();
		this.annotatedType = ((AnnotatedParameterizedType)annotatedType).getAnnotatedActualTypeArguments()[0];
	}

	protected Jackson3TypeReference(Class<T> type) {
		this.annotatedType = generateAnnotatedTypeWithoutAnnotation(type);
	}

	public Type getType() {
		return this.annotatedType.getType();
	}

	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	public boolean isGenericType() {
		return !Types.getGenericsTypes(annotatedType).isEmpty();
	}
}
