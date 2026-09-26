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

package com.navercorp.fixturemonkey.assembly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.ObjectValueExtractor;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Decomposes a value set at a path into child field values for assembly.
 * <p>
 * When a value is set at a path (e.g., via set() or thenApply), its fields are
 * decomposed and placed at child paths so that child-level overrides can take precedence.
 * The result is returned as a {@link DecomposeResult}; the caller applies it to the assembly state.
 */
final class ValueDecomposer {
	private final CandidateLookup candidates;
	private final LimitCounter limits;
	private final ObjectValueExtractor valueExtractor;
	private final ContainerDetector containerDetector;
	private final AssemblyTree assemblyTree;

	ValueDecomposer(
		CandidateLookup candidates,
		LimitCounter limits,
		ObjectValueExtractor valueExtractor,
		ContainerDetector containerDetector,
		AssemblyTree assemblyTree
	) {
		this.candidates = candidates;
		this.limits = limits;
		this.valueExtractor = valueExtractor;
		this.assemblyTree = assemblyTree;
		this.containerDetector = containerDetector;
	}

	/**
	 * Decomposes a value at the given path into child field values.
	 * <p>
	 * The value's fields are extracted and placed at child paths as a {@link DecomposeResult},
	 * allowing child-level overrides to take precedence.
	 *
	 * @param overriddenInside true when the value lies inside a defined scope's value decomposed for a directive inside
	 * @return a {@link DecomposeResult} describing the decomposition outcome
	 */
	DecomposeResult decompose(
		PathExpression currentPath,
		Class<?> currentRawType,
		boolean isCurrentTypeContainer,
		DirectivePrecedence parentPrecedence,
		boolean overriddenInside
	) {
		ValueCandidate candidate = candidates.at(currentPath);
		if (candidate == null) {
			return DecomposeResult.none();
		}
		Object baseValue = candidate.value;
		if (baseValue == null) {
			return DecomposeResult.none();
		}

		if (!isCurrentTypeContainer) {
			Map<PathExpression, ValueCandidate> valuesToPut = new HashMap<>();
			decomposeObjectFields(baseValue, currentPath, valuesToPut, parentPrecedence, overriddenInside);
			return DecomposeResult.of(valuesToPut, new HashSet<>(), null, -1);
		}

		// For container types: if the generated tree has a different size than the decomposed value,
		// handle the mismatch based on direction.
		JvmNode treeNode = assemblyTree.plannedNodeAt(currentPath);
		if (treeNode != null) {
			int generatedTreeChildCount = assemblyTree.childrenOf(treeNode).size();
			int containerSize = containerDetector.getContainerSize(baseValue).orElse(0);
			if (generatedTreeChildCount != containerSize) {
				if (generatedTreeChildCount > containerSize) {
					// Tree is larger than decomposed value (e.g., set({a=1,b=2}).size(4))
					// Decompose existing elements and let extra tree entries generate random values
					Map<PathExpression, ValueCandidate> valuesToPut = new HashMap<>();
					decomposeContainerElementFields(
						baseValue,
						currentPath,
						valuesToPut,
						parentPrecedence,
						overriddenInside
					);
					return DecomposeResult.of(valuesToPut, new HashSet<>(), null, -1);
				} else {
					// Tree is smaller (e.g., size() truncated) → truncate and decompose
					// Keep elements up to generatedTreeChildCount, remove excess
					Set<PathExpression> subtreesToRemove = new HashSet<>();
					for (int i = generatedTreeChildCount; i < containerSize; i++) {
						subtreesToRemove.add(currentPath.index(i));
					}
					Map<PathExpression, ValueCandidate> valuesToPut = new HashMap<>();
					decomposeContainerElementFields(
						truncateContainer(baseValue, generatedTreeChildCount),
						currentPath,
						valuesToPut,
						parentPrecedence,
						overriddenInside
					);
					return DecomposeResult.of(valuesToPut, subtreesToRemove, currentPath, generatedTreeChildCount);
				}
			}
		}

		// For container types, check if all child values are within the container's elements.
		// If so, return the value directly to preserve the container's size.
		if (!overriddenInside && allChildValuesWithinContainer(baseValue, currentPath)) {
			return DecomposeResult.earlyReturn(baseValue);
		}

		// Otherwise, decompose element fields for external overrides.
		// Also set the container's size as a limit to prevent extra elements from being generated.
		int containerSize = containerDetector.getContainerSize(baseValue).orElse(0);
		Map<PathExpression, ValueCandidate> valuesToPut = new HashMap<>();
		decomposeContainerElementFields(baseValue, currentPath, valuesToPut, parentPrecedence, overriddenInside);

		@Nullable
		PathExpression limitPath = !limits.hasChildLimit(currentPath) ? currentPath : null;
		int limitValue = limitPath != null ? containerSize : -1;

		return DecomposeResult.of(valuesToPut, new HashSet<>(), limitPath, limitValue);
	}

	int containerSize(@Nullable Object value) {
		return containerDetector.getContainerSize(value).orElse(-1);
	}

	/**
	 * Decomposes an object's fields using {@link ObjectValueExtractor} for 1-level extraction.
	 * When a field path already has a value, the decomposed value is added only if
	 * the parent's order is higher than the existing value's order (later set wins).
	 */
	private void decomposeObjectFields(
		Object value,
		PathExpression basePath,
		Map<PathExpression, ValueCandidate> valuesToPut,
		DirectivePrecedence parentPrecedence,
		boolean keepExpandedContainers
	) {
		Map<PathExpression, @Nullable Object> extracted = valueExtractor.extract(value, basePath);

		for (Map.Entry<PathExpression, @Nullable Object> entry : extracted.entrySet()) {
			PathExpression fieldPath = entry.getKey();
			Object fieldValue = entry.getValue();

			// Skip container fields if the tree already expanded them to a larger size.
			if (!keepExpandedContainers && fieldValue != null && containerDetector.isContainer(fieldValue)) {
				JvmNode treeNode = assemblyTree.plannedNodeAt(fieldPath);
				if (treeNode != null) {
					int generatedTreeChildCount = assemblyTree.childrenOf(treeNode).size();
					int decomposedSize = containerDetector.getContainerSize(fieldValue).orElse(0);
					if (generatedTreeChildCount > decomposedSize) {
						continue;
					}
				}
			}

			// Skip if a higher-order value already exists for this field path
			ValueCandidate existing = candidates.at(fieldPath);
			if (existing != null && existing.precedence.compareTo(parentPrecedence) > 0) {
				continue;
			}

			valuesToPut.put(fieldPath, new ValueCandidate(fieldValue, parentPrecedence));
		}
	}

	/**
	 * Checks if any container field of the given object has been expanded to a larger size
	 * in the tree than the object's actual field value.
	 */
	boolean hasContainerFieldExpandedInTree(Object value, PathExpression basePath) {
		Map<PathExpression, @Nullable Object> extracted = valueExtractor.extract(value, basePath);

		for (Map.Entry<PathExpression, @Nullable Object> entry : extracted.entrySet()) {
			Object fieldValue = entry.getValue();
			if (fieldValue != null && containerDetector.isContainer(fieldValue)) {
				JvmNode treeNode = assemblyTree.plannedNodeAt(entry.getKey());
				if (treeNode != null) {
					int generatedTreeChildCount = assemblyTree.childrenOf(treeNode).size();
					int actualSize = containerDetector.getContainerSize(fieldValue).orElse(0);
					if (generatedTreeChildCount > actualSize) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if all child values for a container path are ONLY direct element values
	 * (not nested paths like $[0].field) and within the container's element indices.
	 */
	private boolean allChildValuesWithinContainer(Object container, PathExpression containerPath) {
		int containerSize = containerDetector.getContainerSize(container).orElse(0);

		for (PathExpression path : candidates.getPaths()) {
			if (path.isChildOf(containerPath)) {
				if (path.depth() != containerPath.depth() + 1) {
					return false;
				}
				Segment lastSegment = path.getLastSegment();
				if (lastSegment == null || !(lastSegment.getFirstSelector() instanceof IndexSelector)) {
					return false;
				}
				int index = ((IndexSelector)lastSegment.getFirstSelector()).getIndex();
				if (index >= containerSize) {
					return false;
				}
				ValueCandidate childCandidate = candidates.at(path);
				if (childCandidate != null) {
					Object childValue = childCandidate.value;
					Object containerElement = getContainerElement(container, index);
					if (!java.util.Objects.equals(childValue, containerElement)) {
						return false;
					}
					if (
						childValue != null
							&& !containerDetector.isContainer(childValue)
							&& hasContainerFieldExpandedInTree(childValue, path)
					) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private @Nullable Object getContainerElement(Object container, int index) {
		if (container instanceof List) {
			List<?> list = (List<?>)container;
			if (index < list.size()) {
				return list.get(index);
			}
		} else if (container.getClass().isArray()) {
			int length = java.lang.reflect.Array.getLength(container);
			if (index < length) {
				return java.lang.reflect.Array.get(container, index);
			}
		}
		return null;
	}

	/**
	 * Truncates a container to the given size.
	 * Returns a new collection/array containing only the first {@code size} elements.
	 */
	private static Object truncateContainer(Object container, int size) {
		if (container instanceof List) {
			return new ArrayList<>(((List<?>)container).subList(0, size));
		} else if (container instanceof Collection) {
			List<@Nullable Object> truncated = new ArrayList<>(size);
			int count = 0;
			for (Object element : (Collection<?>)container) {
				if (count >= size) {
					break;
				}
				truncated.add(element);
				count++;
			}
			return truncated;
		} else if (container.getClass().isArray()) {
			return Types.truncateArray(container, size);
		}
		return container;
	}

	/**
	 * Decomposes container element fields using {@link ObjectValueExtractor}.
	 * Filters results through order comparison.
	 */
	private void decomposeContainerElementFields(
		Object container,
		PathExpression basePath,
		Map<PathExpression, ValueCandidate> valuesToPut,
		DirectivePrecedence parentPrecedence,
		boolean keepExpandedContainers
	) {
		if (container == null) {
			return;
		}

		Map<PathExpression, @Nullable Object> extracted = valueExtractor.extract(container, basePath);

		Set<PathExpression> skippedPrefixes = new HashSet<>();

		for (Map.Entry<PathExpression, @Nullable Object> entry : extracted.entrySet()) {
			PathExpression path = entry.getKey();
			Object value = entry.getValue();

			// Skip descendants of paths that were gated by order comparison
			if (isUnderAnyPrefix(path, skippedPrefixes)) {
				continue;
			}

			// For element-level paths ($[i]), check order gating
			if (isDirectChild(path, basePath)) {
				ValueCandidate existing = candidates.at(path);
				if (existing != null && existing.precedence.compareTo(parentPrecedence) > 0) {
					skippedPrefixes.add(path);
					continue;
				}
			} else {
				// For field-level paths ($[i].field): container tree-size skip + order comparison
				if (!keepExpandedContainers && value != null && containerDetector.isContainer(value)) {
					JvmNode treeNode = assemblyTree.plannedNodeAt(path);
					if (treeNode != null) {
						int generatedTreeChildCount = assemblyTree.childrenOf(treeNode).size();
						int decomposedSize = containerDetector.getContainerSize(value).orElse(0);
						if (generatedTreeChildCount > decomposedSize) {
							continue;
						}
					}
				}

				ValueCandidate existing = candidates.at(path);
				if (existing != null && existing.precedence.compareTo(parentPrecedence) > 0) {
					continue;
				}
			}

			valuesToPut.put(path, new ValueCandidate(value, parentPrecedence));
		}
	}

	private static boolean isDirectChild(PathExpression child, PathExpression parent) {
		return child.isChildOf(parent) && child.depth() == parent.depth() + 1;
	}

	private static boolean isUnderAnyPrefix(PathExpression path, Set<PathExpression> prefixes) {
		for (PathExpression prefix : prefixes) {
			if (path.isChildOf(prefix)) {
				return true;
			}
		}
		return false;
	}
}
