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

package com.navercorp.fixturemonkey.adapter.analysis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.adapter.projection.LazyValueHolder;
import com.navercorp.fixturemonkey.customizer.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.CompositeNodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeNullityManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetJustManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Extracts typed values from manipulators and converts them to path expressions.
 * <p>
 * This class handles the extraction of values set via {@code set()}, {@code setLazy()},
 * {@code setNull()} calls in registered builders, and their conversion into
 * TypeSelector-based PathExpression entries.
 */
public final class TypedValueExtractor {
	private TypedValueExtractor() {
	}

	/**
	 * Extracts typed values from an ArbitraryManipulator into the accumulated typedValues map.
	 * This extracts values set via set() calls in registered builders.
	 * <p>
	 * The accumulated map is mutated directly so that thenApply's {@code clear()} on "$" lazy
	 * correctly removes previously accumulated field-level entries for the same type.
	 *
	 * @param manipulator the manipulator to extract values from
	 * @param ownerType   the owner type for type-based matching
	 * @param typedValues the accumulated map to populate (mutated directly)
	 */
	public static void extract(
		ArbitraryManipulator manipulator,
		JvmType ownerType,
		Map<JvmType, Map<String, @Nullable Object>> typedValues
	) {
		NodeResolver nodeResolver = manipulator.getNodeResolver();
		NodeManipulator nodeManipulator = manipulator.getNodeManipulator();

		List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(nodeResolver);
		String pathExpression = PredicatePathConverter.toExpression(predicates);
		String fieldPath = pathExpression.startsWith("$.") ? pathExpression.substring(2) : pathExpression;

		populateFromNodeManipulator(nodeManipulator, fieldPath, ownerType, typedValues);
	}

	/**
	 * Recursively extracts typed values from a NodeManipulator.
	 * Uses put() so that later declarations override earlier ones (last-write-wins),
	 * consistent with the "declaration order" principle used by direct manipulators.
	 */
	private static void populateFromNodeManipulator(
		NodeManipulator nodeManipulator,
		String fieldPath,
		JvmType ownerType,
		Map<JvmType, Map<String, @Nullable Object>> typedValues
	) {
		if (nodeManipulator instanceof CompositeNodeManipulator) {
			CompositeNodeManipulator composite = (CompositeNodeManipulator)nodeManipulator;
			for (NodeManipulator inner : composite.getManipulators()) {
				populateFromNodeManipulator(inner, fieldPath, ownerType, typedValues);
			}
			return;
		}

		// Unwrap ApplyNodeCountManipulator to get the underlying manipulator
		if (nodeManipulator instanceof ApplyNodeCountManipulator) {
			ApplyNodeCountManipulator countManipulator = (ApplyNodeCountManipulator)nodeManipulator;
			populateFromNodeManipulator(countManipulator.getNodeManipulator(), fieldPath, ownerType, typedValues);
			return;
		}

		if (nodeManipulator instanceof NodeSetDecomposedValueManipulator) {
			NodeSetDecomposedValueManipulator<?> setManipulator = (NodeSetDecomposedValueManipulator<
				?
				>)nodeManipulator;
			Object value = setManipulator.getValue();

			Map<String, @Nullable Object> fieldValues = typedValues.computeIfAbsent(ownerType, k -> new HashMap<>());
			fieldValues.put(fieldPath, value);
		} else if (nodeManipulator instanceof NodeSetJustManipulator) {
			NodeSetJustManipulator setManipulator = (NodeSetJustManipulator)nodeManipulator;
			Object value = setManipulator.getValue();

			Map<String, @Nullable Object> fieldValues = typedValues.computeIfAbsent(ownerType, k -> new HashMap<>());
			fieldValues.put(fieldPath, value);
		} else if (nodeManipulator instanceof NodeNullityManipulator) {
			NodeNullityManipulator nullityManipulator = (NodeNullityManipulator)nodeManipulator;
			if (nullityManipulator.isToNull()) {
				Map<String, @Nullable Object> fieldValues =
					typedValues.computeIfAbsent(ownerType, k -> new HashMap<>());
				fieldValues.put(fieldPath, null);
			}
		} else if (nodeManipulator instanceof NodeSetLazyManipulator) {
			NodeSetLazyManipulator<?> lazyManipulator = (NodeSetLazyManipulator<?>)nodeManipulator;

			Map<String, @Nullable Object> fieldValues = typedValues.computeIfAbsent(ownerType, k -> new HashMap<>());
			// Use put() instead of putIfAbsent() for lazy manipulators.
			// thenApply chains produce multiple SetLazy on "$" where each subsequent lazy
			// captures the previous state (including earlier lazies) in its supplier.
			// The last lazy in the chain evaluates the entire chain, so it must override earlier ones.
			if ("$".equals(fieldPath)) {
				removeFieldsAlreadyIncludedInThenApplyLazy(fieldValues);
			}
			boolean isRootLevel = "$".equals(fieldPath);
			fieldValues.put(fieldPath,
				new LazyValueHolder(lazyManipulator.getLazyArbitrary(), ownerType.getRawType(), isRootLevel));
		}
	}

	/**
	 * thenApply's "$" lazy already contains the effects of all pre-thenApply manipulators
	 * (e.g., set("string", ...), setLazy("wrapperInteger", ...)) in its evaluation chain.
	 * Remove these field-level entries to avoid duplication — the "$" lazy will produce
	 * a complete object with all fields when evaluated.
	 */
	private static void removeFieldsAlreadyIncludedInThenApplyLazy(Map<String, @Nullable Object> fieldValues) {
		fieldValues.clear();
	}

	/**
	 * Converts type-based values (from registered builders) into TypeSelector-based PathExpression entries.
	 * <p>
	 * This unifies the type-based value system with the path-based value system by converting each
	 * {@code typedValues} entry into a PathExpression with a TypeSelector segment.
	 * <p>
	 * All entries are converted, including LazyValueHolder entries.
	 * LazyValueHolder values are unwrapped during assembly in assembleNode() when matched.
	 * <p>
	 * "$" root entries (from thenApply) are given lower order than field-level entries,
	 * so field-level values always take precedence over the whole-object lazy.
	 * <p>
	 * Order increases from 0 upward (consistent with direct manipulators).
	 * Root entries are processed first (lowest order), then field-level entries (higher order).
	 * The separation from user values is handled by the {@code ValueOrder} type system
	 * ({@code RegisterOrder} vs {@code UserOrder}), not by numeric offset.
	 * <p>
	 * For example:
	 * <ul>
	 *   <li>{@code typedValues[MyType]["field"] = "value"} → {@code $[type:MyType].field = "value"}</li>
	 *   <li>{@code typedValues[MyType]["a.b"] = 42} → {@code $[type:MyType].a.b = 42}</li>
	 *   <li>{@code typedValues[MyType]["$"] = lazyValue} → {@code $[type:MyType] = lazyValue} (lower order)</li>
	 *   <li>{@code typedValues[MyType]["field"] = lazyValue} → {@code $[type:MyType].field = lazyValue}</li>
	 * </ul>
	 *
	 * @param typedValues the type-based values from registered builders
	 * @return a map containing converted PathExpression entries and their order numbers
	 */
	public static ConversionResult convertToPathExpressions(
		Map<JvmType, Map<String, @Nullable Object>> typedValues
	) {
		Map<PathExpression, @Nullable Object> convertedValues = new HashMap<>();
		Map<PathExpression, Integer> convertedOrders = new HashMap<>();
		int order = 0;

		// Collect "$" root entries and field-level entries separately
		List<Map.Entry<Class<?>, @Nullable Object>> rootEntries = new ArrayList<>();
		List<Map.Entry<PathExpression, @Nullable Object>> fieldEntries = new ArrayList<>();

		for (Map.Entry<JvmType, Map<String, @Nullable Object>> typeEntry : typedValues.entrySet()) {
			JvmType ownerType = typeEntry.getKey();
			Class<?> ownerClass = ownerType.getRawType();
			Map<String, @Nullable Object> fieldValues = typeEntry.getValue();

			for (Map.Entry<String, @Nullable Object> fieldEntry : fieldValues.entrySet()) {
				String fieldPath = fieldEntry.getKey();
				Object value = fieldEntry.getValue();

				if ("$".equals(fieldPath)) {
					rootEntries.add(new AbstractMap.SimpleEntry<>(ownerClass, value));
					continue;
				}

				PathExpression pathExpr = buildTypedPathExpression(ownerClass, fieldPath);
				fieldEntries.add(new AbstractMap.SimpleEntry<>(pathExpr, value));
			}
		}

		// Process root entries first (lowest order)
		for (Map.Entry<Class<?>, @Nullable Object> rootEntry : rootEntries) {
			PathExpression pathExpr = buildTypedPathExpression(rootEntry.getKey(), "$");
			convertedValues.put(pathExpr, rootEntry.getValue());
			convertedOrders.put(pathExpr, order);
			order++;
		}

		// Process field-level entries after root (higher order)
		for (Map.Entry<PathExpression, @Nullable Object> fieldEntry : fieldEntries) {
			convertedValues.put(fieldEntry.getKey(), fieldEntry.getValue());
			convertedOrders.put(fieldEntry.getKey(), order);
			order++;
		}

		return new ConversionResult(convertedValues, convertedOrders);
	}

	/**
	 * Builds a PathExpression with TypeSelector for a given owner type and field path.
	 * <p>
	 * Uses PathExpression.of() to properly parse complex field paths like "values[0]",
	 * "nested.list[*]", etc., then prepends the TypeSelector segment.
	 */
	private static PathExpression buildTypedPathExpression(Class<?> ownerClass, String fieldPath) {
		PathExpression typePath = PathExpression.root().type(ownerClass);

		if ("$".equals(fieldPath)) {
			return typePath;
		}

		// Parse the field path using PathExpression.of() which handles
		// complex paths like "values[0]", "nested.list[*]", etc.
		PathExpression parsed = PathExpression.of("$." + fieldPath);
		List<Segment> parsedSegments = parsed.getSegments();

		// Append all parsed segments to the type path.
		// Note: segments do NOT include root ($) - root is implicit in PathExpression.
		// So we start at index 0, not 1.
		PathExpression current = typePath;
		for (int i = 0; i < parsedSegments.size(); i++) {
			current = current.appendSegment(parsedSegments.get(i));
		}
		return current;
	}

	/**
	 * Result of converting typed values to path expressions.
	 */
	public static final class ConversionResult {

		public final Map<PathExpression, @Nullable Object> values;
		public final Map<PathExpression, Integer> orders;

		ConversionResult(Map<PathExpression, @Nullable Object> values, Map<PathExpression, Integer> orders) {
			this.values = values;
			this.orders = orders;
		}
	}
}
