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
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;

@API(since = "0.4.0", status = Status.INTERNAL)
public final class LazyAnnotatedType<T> implements AnnotatedType {
	private final Supplier<T> supplier;

	public LazyAnnotatedType(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public Type getType() {
		T value = supplier.get();
		if (value == null) {
			return UnidentifiableType.class;
		}

		return Types.getActualType(value.getClass());
	}

	@Override
	@Nullable
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
}
