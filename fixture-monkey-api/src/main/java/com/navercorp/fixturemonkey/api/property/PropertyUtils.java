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
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public abstract class PropertyUtils {
	public static Property toProperty(Class<?> type) {
		return toProperty(new JavaType(type));
	}

	public static Property toProperty(TypeReference<?> typeReference) {
		return toProperty(typeReference.getJvmType());
	}

	/**
	 * @deprecated Prefer {@link #toProperty(JvmType)}.
	 */
	@Deprecated
	public static Property toProperty(AnnotatedType annotatedType) {
		return toProperty(Types.toJvmType(annotatedType, Collections.emptyList()));
	}

	public static Property toProperty(JvmType jvmType) {
		return new Property() {
			@Override
			public JvmType getJvmType() {
				return jvmType;
			}

			@Nullable
			@Override
			public String getName() {
				return null;
			}

			@Override
			public List<Annotation> getAnnotations() {
				return jvmType.getAnnotations();
			}

			@Override
			public int hashCode() {
				return jvmType.getRawType().hashCode();
			}

			@Override
			public boolean equals(@Nullable Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null || getClass() == obj.getClass()) {
					return false;
				}

				Property that = (Property)obj;
				return jvmType.getRawType().equals(that.getJvmType().getRawType());
			}
		};
	}

	public static boolean isErasedProperty(Property property) {
		return property.getJvmType().getRawType() == Object.class;
	}
}
