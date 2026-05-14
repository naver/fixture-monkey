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

import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public abstract class TypeReference<T> {
	private final AnnotatedType annotatedType;
	// Lazy because Types.toTypeReference produces anonymous subclasses that override getAnnotatedType()
	// to return a value different from the field captured by the parent constructor. Computing jvmType
	// eagerly in the constructor would cache the field-derived (wrong) value, bypassing the override.
	// Deferring to first access also avoids invoking an overridable method during super construction,
	// which is unsafe before subclass initialization completes.
	private volatile @Nullable JvmType jvmType;
	private final ReentrantLock jvmTypeLock = new ReentrantLock();

	protected TypeReference() {
		AnnotatedType annotatedType = getClass().getAnnotatedSuperclass();
		this.annotatedType = ((AnnotatedParameterizedType)annotatedType).getAnnotatedActualTypeArguments()[0];
	}

	protected TypeReference(Class<T> type) {
		this.annotatedType = generateAnnotatedTypeWithoutAnnotation(type);
	}

	public Type getType() {
		return this.annotatedType.getType();
	}

	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	public JvmType getJvmType() {
		JvmType cached = this.jvmType;
		if (cached != null) {
			return cached;
		}
		jvmTypeLock.lock();
		try {
			cached = this.jvmType;
			if (cached == null) {
				cached = Types.toJvmType(getAnnotatedType(), Collections.emptyList());
				this.jvmType = cached;
			}
			return cached;
		} finally {
			jvmTypeLock.unlock();
		}
	}

	public boolean isGenericType() {
		return Types.getActualType(annotatedType.getType()).getTypeParameters().length != 0;
	}
}
