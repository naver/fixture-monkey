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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.12", status = Status.MAINTAINED)
public abstract class PropertyUtils {
	public static Property toProperty(Class<?> type) {
		return PropertyUtils.toProperty(Types.generateAnnotatedTypeWithoutAnnotation(type));
	}

	public static Property toProperty(TypeReference<?> typeReference) {
		return toProperty(typeReference.getAnnotatedType());
	}

	public static Property toProperty(AnnotatedType annotatedType) {
		return new Property() {
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
				return null;
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

			@Override
			public int hashCode() {
				return getType().hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null || getClass() == obj.getClass()) {
					return false;
				}

				Property that = (Property)obj;
				return getType().equals(that.getType());
			}
		};
	}

	public static boolean isErasedProperty(Property property) {
		return Types.getActualType(property.getType()) == Object.class
			|| Types.getActualType(property.getAnnotatedType()) == Object.class;
	}
}
