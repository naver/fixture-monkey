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
 * It is a property has a type name.
 * For example, It can be used to represent a type name of a class or method.
 */
@API(since = "1.1.6", status = Status.EXPERIMENTAL)
public final class TypeNameProperty implements Property {
	private final AnnotatedType annotatedType;
	private final String typeName;
	@Nullable
	private final Boolean nullable;

	public TypeNameProperty(
		AnnotatedType annotatedType,
		String typeName,
		@Nullable Boolean nullable
	) {
		this.annotatedType = annotatedType;
		this.typeName = typeName;
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
		return typeName;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TypeNameProperty that = (TypeNameProperty)obj;
		return Objects.equals(annotatedType, that.annotatedType)
			&& Objects.equals(typeName, that.typeName)
			&& Objects.equals(nullable, that.nullable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(annotatedType, typeName, nullable);
	}
}
