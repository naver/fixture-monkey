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

package com.navercorp.objectfarm.api.input;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.Types;

/**
 * Extracts field information from an object.
 * <p>
 * This interface abstracts field extraction logic, allowing different implementations
 * for different contexts (e.g., reflection-based vs. Property-based extraction).
 */
public interface FieldExtractor {
	/**
	 * Extracts fields from an object, returning both runtime values and declared types.
	 *
	 * @param value    the object to extract fields from
	 * @param basePath the base path for field paths
	 * @return a map of field paths to their extracted field information
	 */
	Map<String, ExtractedField> extractFields(Object value, String basePath);

	/**
	 * Returns a field extractor that uses reflection to extract fields.
	 *
	 * @return the reflection-based field extractor
	 */
	static FieldExtractor reflection() {
		return ReflectionFieldExtractor.INSTANCE;
	}

	/**
	 * Returns a no-op extractor that doesn't extract any fields.
	 *
	 * @return a no-op field extractor
	 */
	static FieldExtractor none() {
		return (value, basePath) -> new HashMap<>();
	}

	/**
	 * Reflection-based field extractor.
	 */
	final class ReflectionFieldExtractor implements FieldExtractor {

		static final ReflectionFieldExtractor INSTANCE = new ReflectionFieldExtractor();

		/**
		 * Cache for declared fields by class to avoid repeated reflection calls.
		 * Uses ConcurrentHashMap for thread-safety.
		 */
		private static final Map<Class<?>, Field[]> DECLARED_FIELDS_CACHE = new ConcurrentHashMap<>();

		private ReflectionFieldExtractor() {
		}

		@Override
		public Map<String, ExtractedField> extractFields(Object value, String basePath) {
			Map<String, ExtractedField> result = new HashMap<>();

			if (value == null) {
				return result;
			}

			Class<?> clazz = value.getClass();

			if (isSkippable(clazz, value)) {
				return result;
			}

			extractFieldsRecursively(clazz, value, basePath, result);
			return result;
		}

		private static boolean isSkippable(Class<?> clazz, Object value) {
			if (clazz == String.class || clazz.isArray() || Types.isJdkValueType(clazz)
				|| Number.class.isAssignableFrom(clazz)) {
				return true;
			}

			if (
				value instanceof Collection || value instanceof Map || value instanceof Iterator
					|| value instanceof Stream
			) {
				return true;
			}

			return Types.isJavaType(clazz);
		}

		private void extractFieldsRecursively(@Nullable Class<?> clazz, Object value, String basePath,
			Map<String, ExtractedField> result) {
			if (clazz == null || clazz == Object.class) {
				return;
			}

			Field[] fields = getDeclaredFields(clazz);

			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				String childPath = basePath + "." + field.getName();

				try {
					Object fieldValue = field.get(value);
					result.put(childPath, new ExtractedField(fieldValue, field.getType()));
				} catch (IllegalAccessException | SecurityException e) {
					// Skip fields we can't access
				}
			}

			extractFieldsRecursively(clazz.getSuperclass(), value, basePath, result);
		}

		private static Field[] getDeclaredFields(Class<?> clazz) {
			return DECLARED_FIELDS_CACHE.computeIfAbsent(clazz, c -> {
				Field[] declared = c.getDeclaredFields();
				for (Field f : declared) {
					if (!Modifier.isStatic(f.getModifiers())) {
						try {
							f.setAccessible(true);
						} catch (SecurityException ignored) {
							// Will handle access issues when reading
						}
					}
				}
				return declared;
			});
		}
	}
}
