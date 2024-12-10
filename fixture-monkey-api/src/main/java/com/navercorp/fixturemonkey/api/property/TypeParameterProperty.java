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
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is a property for a type parameter.
 * It is used to represent a type parameter of a generic class or method.
 * It does not support {@link #getValue(Object)}
 * because there is no way to get the value of the property from the object.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class TypeParameterProperty implements Property {
	private final AnnotatedType annotatedType;

	public TypeParameterProperty(AnnotatedType typeParameterAnnotatedType) {
		this.annotatedType = typeParameterAnnotatedType;
	}

	@Override
	public Type getType() {
		return this.annotatedType.getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	/**
	 * The type parameter has no name.
	 *
	 * @return null
	 */
	@Nullable
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Arrays.asList(this.annotatedType.getAnnotations());
	}

	@Nullable
	@Override
	public Object getValue(Object instance) {
		throw new UnsupportedOperationException("type parameter property does not support getValue");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TypeParameterProperty that = (TypeParameterProperty)obj;
		return Objects.equals(annotatedType.getType(), that.annotatedType.getType());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(annotatedType.getType());
	}
}
