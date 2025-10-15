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
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * It is deprecated.
 * Use {@link TypeNameProperty} instead.
 */
@Deprecated
public final class MethodParameterProperty implements Property {
	private final JvmType jvmType;
	@Nullable
	private final String parameterName;
	@Nullable
	private final Boolean nullable;

	public MethodParameterProperty(
		AnnotatedType annotatedType,
		@Nullable String parameterName,
		@Nullable Boolean nullable
	) {
		this.jvmType = Types.toJvmType(annotatedType, Collections.emptyList());
		this.parameterName = parameterName;
		this.nullable = nullable;
	}

	@Override
	public Type getType() {
		return jvmType.getRawType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return jvmType.getAnnotatedType();
	}

	@Nullable
	@Override
	public String getName() {
		return parameterName;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return jvmType.getAnnotations();
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
