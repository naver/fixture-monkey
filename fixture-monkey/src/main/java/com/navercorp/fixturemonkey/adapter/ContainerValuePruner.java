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

package com.navercorp.fixturemonkey.adapter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.input.ContainerDetector;

/**
 * Prunes values that exceed container size constraints.
 *
 * <p>Extracted from {@link DefaultNodeTreeAdapter} to separate container value
 * pruning concerns from the main adapt orchestration logic.
 */
final class ContainerValuePruner {
	private final ContainerDetector containerDetector;

	ContainerValuePruner(ContainerDetector containerDetector) {
		this.containerDetector = containerDetector;
	}

	Map<PathExpression, @Nullable Object> pruneValuesExceedingContainerSize(
		Map<PathExpression, @Nullable Object> valuesByPath,
		List<ContainerInfoManipulator> containerManipulators,
		Map<PathExpression, Integer> valueOrderByPath,
		Map<ContainerInfoManipulator, PathExpression> containerPathCache
	) {
		if (valuesByPath.isEmpty() || containerManipulators.isEmpty()) {
			return new HashMap<>(valuesByPath);
		}

		Map<PathExpression, ContainerSizeConstraint> sizeConstraintByPath = collectContainerSizeConstraints(
			containerManipulators,
			containerPathCache
		);
		if (sizeConstraintByPath.isEmpty()) {
			return new HashMap<>(valuesByPath);
		}

		List<Map.Entry<PathExpression, ContainerSizeConstraint>> wildcardSizeEntries = new ArrayList<>();
		for (Map.Entry<PathExpression, ContainerSizeConstraint> entry : sizeConstraintByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcardSizeEntries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
			}
		}

		Map<PathExpression, @Nullable Object> result = new HashMap<>(valuesByPath);
		List<PathExpression> keysToRemove = new ArrayList<>();

		for (PathExpression path : valuesByPath.keySet()) {
			@Nullable Object value = valuesByPath.get(path);
			ContainerSizeConstraint sizeConstraint = sizeConstraintByPath.get(path);

			// Case 1: Exact path match with size constraint
			if (sizeConstraint != null && containerDetector.isContainer(value)) {
				Integer setOrder = valueOrderByPath.get(path);
				if (setOrder != null && sizeConstraint.sequence > setOrder) {
					if (applyContainerSizeConstraint(
						result, path, value, sizeConstraint, valueOrderByPath, setOrder)) {
						keysToRemove.add(path);
					}
				}

				if (!keysToRemove.contains(path)) {
					int effectiveOrder = setOrder != null ? setOrder : Integer.MIN_VALUE;
					if (hasChildManipulatorWithHigherSequence(path, effectiveOrder, sizeConstraintByPath)) {
						keysToRemove.add(path);
					}
				}
				continue;
			}

			// Case 1b: Null value at a path that has a size constraint.
			// setNull → size: size is later (sizeConstraint.sequence > setOrder) → remove null, size wins
			// size → setNull: setNull is later → keep null, it will be returned as-is during assembly
			if (sizeConstraint != null && value == null) {
				Integer setOrder = valueOrderByPath.get(path);
				if (setOrder != null && sizeConstraint.sequence > setOrder) {
					keysToRemove.add(path);
				}
				continue;
			}

			// Case 2: Wildcard match for container values without exact match
			if (sizeConstraint == null && containerDetector.isContainer(value) && !wildcardSizeEntries.isEmpty()) {
				Integer setOrder = valueOrderByPath.get(path);
				if (setOrder != null) {
					ContainerSizeConstraint wildcardSizeConstraint =
						findWildcardSizeConstraint(path, setOrder, wildcardSizeEntries);
					if (wildcardSizeConstraint != null) {
						if (applyContainerSizeConstraint(
							result, path, value, wildcardSizeConstraint, valueOrderByPath, setOrder)) {
							keysToRemove.add(path);
						}
					}
				}
				if (keysToRemove.contains(path)) {
					continue;
				}
			}

			// Case 3: Container without exact/wildcard match — check child manipulators
			if (containerDetector.isContainer(value) && !keysToRemove.contains(path)) {
				int effectiveOrder = valueOrderByPath.getOrDefault(path, Integer.MIN_VALUE);
				if (hasChildManipulatorWithHigherSequence(path, effectiveOrder, sizeConstraintByPath)) {
					keysToRemove.add(path);
					continue;
				}
			}

			// Case 4: Indexed element exceeding container size
			if (isIndexedElementOutOfRange(path, sizeConstraintByPath, valueOrderByPath)) {
				keysToRemove.add(path);
			}
		}

		keysToRemove.forEach(result::remove);
		return result;
	}

	/**
	 * Removes child values that fall under a just path.
	 * When Values.just() is set at a path, all child path values are unconditionally ignored
	 * because Values.just() means "use this exact object without any modifications".
	 */
	static void pruneChildrenOfJustPaths(
		Map<PathExpression, @Nullable Object> valuesByPath,
		List<PathExpression> justPaths
	) {
		if (justPaths.isEmpty()) {
			return;
		}

		for (PathExpression justPath : justPaths) {
			if (!valuesByPath.containsKey(justPath)) {
				continue;
			}

			valuesByPath.keySet().removeIf(path -> !path.equals(justPath) && path.startsWith(justPath));
		}
	}

	/**
	 * Applies a container size constraint to the value.
	 * If the target size is larger, expands to individual elements. If smaller, truncates.
	 *
	 * @return true if the container was expanded to elements (the original entry should be removed)
	 */
	private boolean applyContainerSizeConstraint(
		Map<PathExpression, @Nullable Object> result,
		PathExpression path,
		@Nullable Object value,
		ContainerSizeConstraint sizeConstraint,
		Map<PathExpression, Integer> valueOrderByPath,
		int containerOrder
	) {
		if (value == null) {
			return false;
		}
		int currentSize = containerDetector.getContainerSize(value).orElse(0);
		if (sizeConstraint.size > currentSize) {
			expandContainerToElements(result, path, value, valueOrderByPath, containerOrder);
			return true;
		} else if (sizeConstraint.size < currentSize) {
			result.put(path, resizeContainer(value, sizeConstraint.size));
		}
		return false;
	}

	private static @Nullable ContainerSizeConstraint findWildcardSizeConstraint(
		PathExpression path,
		int setOrder,
		List<Map.Entry<PathExpression, ContainerSizeConstraint>> wildcardSizeEntries
	) {
		for (Map.Entry<PathExpression, ContainerSizeConstraint> wildcardEntry : wildcardSizeEntries) {
			if (wildcardEntry.getKey().matches(path) && wildcardEntry.getValue().sequence > setOrder) {
				return wildcardEntry.getValue();
			}
		}
		return null;
	}

	private static boolean isIndexedElementOutOfRange(
		PathExpression path,
		Map<PathExpression, ContainerSizeConstraint> sizeConstraintByPath,
		Map<PathExpression, Integer> valueOrderByPath
	) {
		List<Segment> segments = path.getSegments();
		if (segments.isEmpty()) {
			return false;
		}

		Segment lastSegment = segments.get(segments.size() - 1);
		if (lastSegment.isSingleSelector() && lastSegment.getFirstSelector() instanceof IndexSelector) {
			int index = ((IndexSelector)lastSegment.getFirstSelector()).getIndex();
			PathExpression containerPath = path.getParent();
			ContainerSizeConstraint containerSizeConstraint = sizeConstraintByPath.get(containerPath);
			if (containerSizeConstraint != null && index >= containerSizeConstraint.size) {
				// Only prune if the size constraint has higher sequence than this element's value
				Integer elementOrder = valueOrderByPath.get(path);
				return elementOrder == null || containerSizeConstraint.sequence > elementOrder;
			}
		}
		return false;
	}

	private static boolean hasChildManipulatorWithHigherSequence(
		PathExpression containerPath,
		int setOrder,
		Map<PathExpression, ContainerSizeConstraint> sizeConstraintByPath
	) {
		for (Map.Entry<PathExpression, ContainerSizeConstraint> entry : sizeConstraintByPath.entrySet()) {
			if (entry.getKey().isChildOf(containerPath) && entry.getValue().sequence > setOrder) {
				return true;
			}
		}
		return false;
	}

	private static void expandContainerToElements(
		Map<PathExpression, @Nullable Object> result,
		PathExpression containerPath,
		Object container,
		Map<PathExpression, Integer> valueOrderByPath,
		int containerOrder
	) {
		if (container == null) {
			return;
		}

		if (container instanceof Map) {
			Map<?, ?> map = (Map<?, ?>)container;
			int index = 0;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				PathExpression entryPath = containerPath.index(index);
				putIfHigherOrder(result, entryPath.key(), entry.getKey(), valueOrderByPath, containerOrder);
				putIfHigherOrder(result, entryPath.value(), entry.getValue(), valueOrderByPath, containerOrder);
				index++;
			}
		} else if (container instanceof List) {
			List<?> list = (List<?>)container;
			for (int i = 0; i < list.size(); i++) {
				putIfHigherOrder(result, containerPath.index(i), list.get(i), valueOrderByPath, containerOrder);
			}
		} else if (container instanceof java.util.Collection) {
			Collection<?> collection = (Collection<?>)container;
			int index = 0;
			for (Object elem : collection) {
				putIfHigherOrder(result, containerPath.index(index), elem, valueOrderByPath, containerOrder);
				index++;
			}
		} else if (container.getClass().isArray()) {
			int length = java.lang.reflect.Array.getLength(container);
			for (int i = 0; i < length; i++) {
				putIfHigherOrder(
					result, containerPath.index(i), java.lang.reflect.Array.get(container, i),
					valueOrderByPath, containerOrder
				);
			}
		}
	}

	private static void putIfHigherOrder(
		Map<PathExpression, @Nullable Object> result,
		PathExpression elementPath,
		@Nullable Object elementValue,
		Map<PathExpression, Integer> valueOrderByPath,
		int containerOrder
	) {
		Integer existingOrder = valueOrderByPath.get(elementPath);
		if (existingOrder != null && existingOrder > containerOrder) {
			return;
		}
		result.put(elementPath, elementValue);
	}

	@SuppressWarnings({"unchecked", "rawtypes", "return"})
	private static Object resizeContainer(Object container, int targetSize) {
		if (container == null) {
			return null;
		}

		if (container instanceof List) {
			List list = (List)container;
			if (list.size() <= targetSize) {
				return container;
			}
			return new ArrayList<>(list.subList(0, targetSize));
		}

		if (container instanceof java.util.Set) {
			java.util.Set set = (java.util.Set)container;
			if (set.size() <= targetSize) {
				return container;
			}
			java.util.Set truncated = new java.util.LinkedHashSet<>();
			int count = 0;
			for (Object elem : set) {
				if (count >= targetSize) {
					break;
				}
				truncated.add(elem);
				count++;
			}
			return truncated;
		}

		if (container.getClass().isArray()) {
			return Types.truncateArray(container, targetSize);
		}

		return container;
	}

	private static Map<PathExpression, ContainerSizeConstraint> collectContainerSizeConstraints(
		List<ContainerInfoManipulator> containerManipulators,
		Map<ContainerInfoManipulator, PathExpression> containerPathCache
	) {
		Map<PathExpression, ContainerSizeConstraint> sizeConstraintByPath = new HashMap<>();

		List<ContainerInfoManipulator> sorted = new ArrayList<>(containerManipulators);
		sorted.sort(Comparator.comparingInt(ContainerInfoManipulator::getManipulatingSequence));

		for (ContainerInfoManipulator manipulator : sorted) {
			// Use maxSize for pruning indexed elements — elements within the max range should be preserved.
			// Using fixed() (random between min and max) would incorrectly prune elements
			// when maxSize > fixedSize (e.g., maxSize(2) + listElement(1, value) should keep $[1]).
			int size = manipulator.getContainerInfo().getElementMaxSize();
			int sequence = manipulator.getManipulatingSequence();

			PathExpression path = containerPathCache.get(manipulator);
			if (path != null) {
				sizeConstraintByPath.put(path, new ContainerSizeConstraint(size, sequence));
			}
		}

		return sizeConstraintByPath;
	}

	static final class ContainerSizeConstraint {
		final int size;
		final int sequence;

		ContainerSizeConstraint(int size, int sequence) {
			this.size = size;
			this.sequence = sequence;
		}
	}
}
