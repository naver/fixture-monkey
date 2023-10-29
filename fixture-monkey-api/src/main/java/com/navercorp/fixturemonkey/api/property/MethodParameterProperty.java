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

package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public final class MethodParameterProperty implements Property {
	private final AnnotatedType annotatedType;
	@Nullable
	private final String parameterName;
	@Nullable
	private final Boolean nullable;

	public MethodParameterProperty(
		AnnotatedType annotatedType,
		@Nullable String parameterName,
		@Nullable Boolean nullable
	) {
		this.annotatedType = annotatedType;
		this.parameterName = parameterName;
		this.nullable = nullable;
	}

	@Override
	public Type getType() {
		return annotatedType.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	@Nullable
	@Override
	public String getName() {
		return parameterName;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Arrays.asList(annotatedType.getAnnotations());
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Boolean isNullable() {
		return nullable;
	}
}
