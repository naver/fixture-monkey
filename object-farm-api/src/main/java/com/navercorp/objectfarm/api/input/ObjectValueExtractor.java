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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Extracts all path→value pairs from an object graph recursively.
 * <p>
 * Combines {@link FieldExtractor} (for POJO field extraction) and
 * {@link ContainerDetector} (for container detection and sizing)
 * to produce a flat {@code Map<PathExpression, Object>} representing
 * every reachable value in the object graph.
 * <p>
 * Uses identity-based visited tracking to safely handle circular references
 * (e.g., JPA bidirectional entities).
 */
public final class ObjectValueExtractor {
	private final FieldExtractor fieldExtractor;
	private final ContainerDetector containerDetector;

	public ObjectValueExtractor() {
		this(FieldExtractor.reflection(), ContainerDetector.standard());
	}

	public ObjectValueExtractor(FieldExtractor fieldExtractor, ContainerDetector containerDetector) {
		this.fieldExtractor = fieldExtractor;
		this.containerDetector = containerDetector;
	}

	/**
	 * Recursively extracts all path→value pairs from the given object.
	 * <p>
	 * For POJOs, extracts fields and recurses into non-primitive field values.
	 * For Collections/Arrays, extracts elements by index and recurses into each element.
	 * For Maps, extracts key/value pairs by entry index.
	 * <p>
	 * Circular references are detected via identity comparison and silently skipped.
	 *
	 * @param value    the object to extract values from (null returns empty map)
	 * @param basePath the base path for this object (e.g., {@code PathExpression.root()} for "$")
	 * @return a flat map of path→value for every reachable node in the object graph
	 */
	public Map<PathExpression, Object> extract(@Nullable Object value, PathExpression basePath) {
		Map<PathExpression, Object> result = new LinkedHashMap<>();
		Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		extractInternal(value, basePath, result, visited);
		return result;
	}

	private void extractInternal(
		@Nullable Object value,
		PathExpression basePath,
		Map<PathExpression, Object> result,
		Set<Object> visited
	) {
		if (value == null) {
			return;
		}

		if (containerDetector.isContainer(value)) {
			extractContainer(value, basePath, result, visited);
		} else {
			extractObjectFields(value, basePath, result, visited);
		}
	}

	private void extractObjectFields(
		Object value,
		PathExpression basePath,
		Map<PathExpression, Object> result,
		Set<Object> visited
	) {
		if (!visited.add(value)) {
			return;
		}

		try {
			Map<String, ExtractedField> fields = fieldExtractor.extractFields(value, basePath.toExpression());

			for (Map.Entry<String, ExtractedField> entry : fields.entrySet()) {
				PathExpression fieldPath = PathExpression.of(entry.getKey());
				Object fieldValue = entry.getValue().getValue();
				result.put(fieldPath, fieldValue);

				if (fieldValue != null) {
					extractInternal(fieldValue, fieldPath, result, visited);
				}
			}
		} finally {
			visited.remove(value);
		}
	}

	private void extractContainer(
		Object container,
		PathExpression basePath,
		Map<PathExpression, Object> result,
		Set<Object> visited
	) {
		if (container instanceof Map) {
			extractMap((Map<?, ?>) container, basePath, result);
		} else if (container instanceof Collection) {
			extractCollection((Collection<?>) container, basePath, result, visited);
		} else if (container.getClass().isArray()) {
			extractArray(container, basePath, result, visited);
		}
	}

	private void extractCollection(
		Collection<?> collection,
		PathExpression basePath,
		Map<PathExpression, Object> result,
		Set<Object> visited
	) {
		int index = 0;
		for (Object element : collection) {
			if (element != null) {
				PathExpression elementPath = basePath.index(index);
				result.put(elementPath, element);
				extractInternal(element, elementPath, result, visited);
			}
			index++;
		}
	}

	private void extractArray(
		Object array,
		PathExpression basePath,
		Map<PathExpression, Object> result,
		Set<Object> visited
	) {
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			if (element != null) {
				PathExpression elementPath = basePath.index(i);
				result.put(elementPath, element);
				extractInternal(element, elementPath, result, visited);
			}
		}
	}

	private void extractMap(Map<?, ?> map, PathExpression basePath, Map<PathExpression, Object> result) {
		int index = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			PathExpression entryPath = basePath.index(index);
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (key != null) {
				result.put(entryPath.key(), key);
			}
			if (value != null) {
				result.put(entryPath.value(), value);
			}
			index++;
		}
	}
}
