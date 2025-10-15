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

package com.navercorp.fixturemonkey.api.expression;

import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyDescriptorProperty;
import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector;
import com.navercorp.fixturemonkey.api.type.TypeCache;

abstract class JavaGetterPropertySelectors {
	private static final JavaGetterPropertyFieldNameResolver PROPERTY_FIELD_NAME_RESOLVER =
		new JavaGetterPropertyFieldNameResolver();

	/**
	 * It is for internal use only. It can be changed or removed at any time.
	 **/
	@SuppressWarnings("unchecked")
	static <T, R> JavaGetterMethodPropertySelector<T, R> resolvePropertySelector(
		JavaGetterMethodReference<T, R> methodRef
	) {
		try {
			Class<?> methodRefClass = methodRef.getClass();
			Method replaceMethod = methodRefClass.getDeclaredMethod("writeReplace");
			replaceMethod.setAccessible(true);
			SerializedLambda lambda = (SerializedLambda)replaceMethod.invoke(methodRef);
			String className = lambda.getImplClass().replace('/', '.');
			ClassLoader classLoader;
			if (methodRefClass.getClassLoader() != null) {
				classLoader = methodRefClass.getClassLoader();
			} else {
				classLoader = JavaGetterPropertySelectors.class.getClassLoader();
			}

			Class<R> targetClass = (Class<R>)Class.forName(className, true, classLoader);
			if (KotlinTypeDetector.isKotlinType(targetClass)) {
				throw new IllegalArgumentException("Kotlin type could not resolve property name. type: " + targetClass);
			}

			String fieldName = PROPERTY_FIELD_NAME_RESOLVER.resolveFieldName(targetClass, lambda.getImplMethodName());
			Property fieldProperty = resolveFieldProperty(targetClass, fieldName);
			Property propertyDescriptorProperty = resolvePropertyDescriptorProperty(targetClass, fieldName);

			Property resolvedProperty;
			if (fieldProperty != null && propertyDescriptorProperty != null) {
				resolvedProperty = new CompositeProperty(fieldProperty, propertyDescriptorProperty);
			} else if (fieldProperty != null) {
				resolvedProperty = fieldProperty;
			} else if (propertyDescriptorProperty != null) {
				resolvedProperty = propertyDescriptorProperty;
			} else {
				throw new IllegalArgumentException(
					"Could not resolve a field or a JavaBeans getter by given lambda. type: " + targetClass
				);
			}

			return new JavaGetterMethodPropertySelector<>(targetClass, resolvedProperty);
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				"Could not resolve a field or a JavaBeans getter by given lambda. lambda: " + methodRef,
				ex
			);
		}

	}

	@Nullable
	private static Property resolveFieldProperty(Class<?> targetClass, String fieldName) {
		Map<String, Field> fieldsByName = TypeCache.getFieldsByName(targetClass);
		if (!fieldsByName.containsKey(fieldName)) {
			return null;
		}
		return new FieldProperty(fieldsByName.get(fieldName));
	}

	@Nullable
	private static Property resolvePropertyDescriptorProperty(Class<?> targetClass, String fieldName) {
		Map<String, PropertyDescriptor> propertyDescriptorsByPropertyName =
			TypeCache.getPropertyDescriptorsByPropertyName(targetClass);

		if (!propertyDescriptorsByPropertyName.containsKey(fieldName)) {
			return null;
		}
		return new PropertyDescriptorProperty(propertyDescriptorsByPropertyName.get(fieldName));
	}
}
