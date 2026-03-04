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

import static com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult.LazyManipulatorDescriptor;
import static com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult.PostConditionFilter;
import static com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult.PropertyCustomizer;
import static com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.FIELD_PROPERTY_GENERATOR;
import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;
import static com.navercorp.fixturemonkey.api.type.Types.isBoxedPrimitive;
import static com.navercorp.fixturemonkey.api.type.Types.isJavaType;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.adapter.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.customizer.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.CompositeNodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeCustomizerManipulator;
import com.navercorp.fixturemonkey.customizer.NodeFilterManipulator;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeNullityManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetJustManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;
import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodePredicateResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.StaticNodeResolver;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.FieldExtractor;
import com.navercorp.objectfarm.api.input.ValueAnalysisResult;
import com.navercorp.objectfarm.api.input.ValueAnalyzer;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;

/**
 * Analyzes ArbitraryManipulator instances to extract topology-affecting information.
 * <p>
 * This analyzer processes ArbitraryManipulators and extracts:
 * <ul>
 *   <li>Interface resolution information from value setters</li>
 *   <li>Generic type hints from value types</li>
 *   <li>Paths that should be excluded (null paths or just-set paths)</li>
 * </ul>
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class ManipulatorAnalyzer {
	private ManipulatorAnalyzer() {
	}

	/**
	 * Analyzes a list of ArbitraryManipulators and extracts topology-affecting information.
	 * Uses Java field names for decomposed path expressions.
	 *
	 * @param manipulators the list of ArbitraryManipulators to analyze
	 * @return the analysis result containing resolvers and just paths
	 */
	public static AnalysisResult analyze(List<ArbitraryManipulator> manipulators) {
		return analyze(manipulators, Property::getName);
	}

	/**
	 * Analyzes a list of ArbitraryManipulators and extracts topology-affecting information.
	 *
	 * @param manipulators the list of ArbitraryManipulators to analyze
	 * @param nameResolver resolves property names for decomposed path expressions
	 * @return the analysis result containing resolvers and just paths
	 */
	public static AnalysisResult analyze(List<ArbitraryManipulator> manipulators, DecomposeNameResolver nameResolver) {
		List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		Map<PathExpression, Integer> containerSizeSequenceByPath = new HashMap<>();
		List<PathExpression> justPaths = new ArrayList<>();
		Set<PathExpression> notNullPaths = new HashSet<>();
		Map<PathExpression, @Nullable Object> valuesByPath = new HashMap<>();
		List<LazyManipulatorDescriptor> lazyManipulators = new ArrayList<>();
		Map<PathExpression, List<PostConditionFilter>> filtersByPath = new HashMap<>();
		Map<PathExpression, Integer> limitsByPath = new HashMap<>();
		Map<PathExpression, Integer> valueOrderByPath = new HashMap<>();
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath = new HashMap<>();
		List<ResolutionTrace.NodeCollision> nodeCollisions = new ArrayList<>();
		boolean strictMode = false;

		for (int order = 0; order < manipulators.size(); order++) {
			ArbitraryManipulator manipulator = manipulators.get(order);

			// Skip registered manipulators (those using StaticNodeResolver)
			// They are handled via type-based resolution, not path-based resolution
			if (hasStaticNodeResolver(manipulator.getNodeResolver())) {
				continue;
			}

			// Check if strict mode is enabled (any manipulator with ApplyStrictModeResolver)
			if (!strictMode && hasStrictModeResolver(manipulator.getNodeResolver())) {
				strictMode = true;
			}

			analyzeManipulator(
				manipulator,
				order,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				justPaths,
				notNullPaths,
				valuesByPath,
				lazyManipulators,
				filtersByPath,
				limitsByPath,
				valueOrderByPath,
				customizersByPath,
				nodeCollisions,
				nameResolver
			);
		}

		return new AnalysisResult(
			interfaceResolvers,
			genericTypeResolvers,
			containerSizeResolvers,
			containerSizeSequenceByPath,
			justPaths,
			notNullPaths,
			valuesByPath,
			lazyManipulators,
			filtersByPath,
			limitsByPath,
			valueOrderByPath,
			customizersByPath,
			nodeCollisions,
			strictMode
		);
	}

	/**
	 * Returns an empty AnalysisResult for cases where there are no manipulators.
	 * This is a performance optimization to avoid creating empty collections repeatedly.
	 */
	public static AnalysisResult emptyResult() {
		return EMPTY_RESULT;
	}

	private static final PathExpression ROOT_PATH = PathExpression.root();

	private static final AnalysisResult EMPTY_RESULT = new AnalysisResult(
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyMap(),
		Collections.emptyList(),
		Collections.emptySet(),
		Collections.emptyMap(),
		Collections.emptyList(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyList(),
		false
	);

	private static void analyzeManipulator(
		ArbitraryManipulator manipulator,
		int order,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		List<PathExpression> justPaths,
		Set<PathExpression> notNullPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		List<LazyManipulatorDescriptor> lazyManipulators,
		Map<PathExpression, List<PostConditionFilter>> filtersByPath,
		Map<PathExpression, Integer> limitsByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		DecomposeNameResolver nameResolver
	) {
		NodeResolver nodeResolver = manipulator.getNodeResolver();
		NodeManipulator nodeManipulator = manipulator.getNodeManipulator();

		List<NextNodePredicate> predicates = extractPredicates(nodeResolver);
		PathExpression pathExpression = PredicatePathConverter.convert(predicates, nameResolver);

		analyzeNodeManipulator(
			nodeManipulator,
			pathExpression,
			order,
			interfaceResolvers,
			genericTypeResolvers,
			containerSizeResolvers,
			containerSizeSequenceByPath,
			justPaths,
			notNullPaths,
			valuesByPath,
			lazyManipulators,
			filtersByPath,
			limitsByPath,
			null,
			valueOrderByPath,
			customizersByPath,
			nodeCollisions,
			nameResolver
		);
	}

	private static void analyzeNodeManipulator(
		NodeManipulator nodeManipulator,
		PathExpression pathExpression,
		int order,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		List<PathExpression> justPaths,
		Set<PathExpression> notNullPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		List<LazyManipulatorDescriptor> lazyManipulators,
		Map<PathExpression, List<PostConditionFilter>> filtersByPath,
		Map<PathExpression, Integer> limitsByPath,
		@Nullable Integer limit,
		Map<PathExpression, Integer> valueOrderByPath,
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		DecomposeNameResolver nameResolver
	) {
		if (nodeManipulator instanceof CompositeNodeManipulator) {
			CompositeNodeManipulator composite = (CompositeNodeManipulator)nodeManipulator;
			for (NodeManipulator inner : composite.getManipulators()) {
				analyzeNodeManipulator(
					inner,
					pathExpression,
					order,
					interfaceResolvers,
					genericTypeResolvers,
					containerSizeResolvers,
					containerSizeSequenceByPath,
					justPaths,
					notNullPaths,
					valuesByPath,
					lazyManipulators,
					filtersByPath,
					limitsByPath,
					limit,
					valueOrderByPath,
					customizersByPath,
					nodeCollisions,
					nameResolver
				);
			}
			return;
		}

		if (nodeManipulator instanceof ApplyNodeCountManipulator) {
			ApplyNodeCountManipulator countManipulator = (ApplyNodeCountManipulator)nodeManipulator;
			int extractedLimit = countManipulator.getCount();
			if (extractedLimit > 0) {
				limitsByPath.put(pathExpression, extractedLimit);
			}
			analyzeNodeManipulator(
				countManipulator.getNodeManipulator(),
				pathExpression,
				order,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				justPaths,
				notNullPaths,
				valuesByPath,
				lazyManipulators,
				filtersByPath,
				limitsByPath,
				extractedLimit,
				valueOrderByPath,
				customizersByPath,
				nodeCollisions,
				nameResolver
			);
			return;
		}

		if (nodeManipulator instanceof NodeFilterManipulator) {
			NodeFilterManipulator filterManipulator = (NodeFilterManipulator)nodeManipulator;
			PostConditionFilter filter = extractPostConditionFilter(filterManipulator);
			if (filter != null) {
				filtersByPath.computeIfAbsent(pathExpression, k -> new ArrayList<>()).add(filter);
			}
			return;
		}

		if (nodeManipulator instanceof NodeCustomizerManipulator) {
			@SuppressWarnings("unchecked")
			NodeCustomizerManipulator<?> customizerManipulator = (NodeCustomizerManipulator<?>)nodeManipulator;
			@SuppressWarnings("unchecked")
			Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer = (Function<
				CombinableArbitrary<?>,
				CombinableArbitrary<?>
				>)(Function<?, ?>)customizerManipulator.getArbitraryCustomizer();
			boolean afterSet = valuesByPath.containsKey(pathExpression);
			customizersByPath
				.computeIfAbsent(pathExpression, k -> new ArrayList<>())
				.add(new PropertyCustomizer(customizer, order, afterSet));
			return;
		}

		if (nodeManipulator instanceof NodeSetDecomposedValueManipulator) {
			analyzeDecomposedValueManipulator(
				(NodeSetDecomposedValueManipulator<?>)nodeManipulator,
				pathExpression,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				nameResolver
			);
		} else if (nodeManipulator instanceof NodeSetJustManipulator) {
			// Values.just() creates truly immutable values - child values should NOT be applied.
			// This is intentional: when a user sets Values.just(object), they want that exact object
			// without any modifications. Subsequent set("child", value) calls are ignored.
			// This is the ONLY case where child path values are ignored.
			NodeSetJustManipulator justManipulator = (NodeSetJustManipulator)nodeManipulator;
			Object value = justManipulator.getValue();
			if (value != null) {
				recordCollisionIfExists(pathExpression, order, value, valuesByPath, valueOrderByPath, nodeCollisions);
				valuesByPath.put(pathExpression, value);
				valueOrderByPath.put(pathExpression, order);
			}
			justPaths.add(pathExpression);
		} else if (nodeManipulator instanceof NodeNullityManipulator) {
			// setNull() just sets the value to null - child values CAN override this.
			// Following the "more specific path wins" rule: setNull("object") + set("object.str", "x")
			// results in object.str = "x" (the more specific path wins).
			// setNotNull() removes any null value that was previously set for this path
			// and also sets null injection to 0 for this path.
			NodeNullityManipulator nullityManipulator = (NodeNullityManipulator)nodeManipulator;
			int nullitySequence = nullityManipulator.getSequence();
			if (nullityManipulator.isToNull()) {
				recordCollisionIfExists(
					pathExpression,
					nullitySequence,
					null,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, null);
				valueOrderByPath.put(pathExpression, nullitySequence);
				notNullPaths.remove(pathExpression);
			} else {
				// setNotNull: remove any previously set null value for this path
				if (valuesByPath.containsKey(pathExpression) && valuesByPath.get(pathExpression) == null) {
					valuesByPath.remove(pathExpression);
					valueOrderByPath.remove(pathExpression);
				}
				// Track this path as requiring non-null (null injection = 0)
				notNullPaths.add(pathExpression);
			}
		} else if (nodeManipulator instanceof NodeSetLazyManipulator) {
			NodeSetLazyManipulator<?> lazyManipulator = (NodeSetLazyManipulator<?>)nodeManipulator;
			if (limit != null) {
				limitsByPath.put(pathExpression, limit);
			}
			// Evaluate lazy immediately and decompose - same flow as other values
			analyzeLazyManipulator(
				lazyManipulator,
				pathExpression,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				justPaths,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				nameResolver
			);
		}
	}

	private static void analyzeDecomposedValueManipulator(
		NodeSetDecomposedValueManipulator<?> manipulator,
		PathExpression pathExpression,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		DecomposeNameResolver nameResolver
	) {
		Object value = manipulator.getValue();
		if (value == null) {
			return;
		}

		// Use the manipulator's factory sequence for priority comparison
		int factorySequence = manipulator.getSequence();

		// Remove existing indexed child paths under this path before adding new values
		// This handles the case where a container is set multiple times
		// e.g., set("values", ["test"]) then set("values", [])
		// Only remove indexed children (e.g., $.values[0]) not field children (e.g., $.values.field)
		// Only remove child paths with lower sequence (older values)
		String indexPrefix = pathExpression.toExpression() + "[";
		valuesByPath
			.keySet()
			.removeIf(
				key ->
					key.toExpression().startsWith(indexPrefix)
						&& valueOrderByPath.getOrDefault(key, Integer.MIN_VALUE) < factorySequence
			);
		valueOrderByPath
			.keySet()
			.removeIf(key -> key.toExpression().startsWith(indexPrefix) && !valuesByPath.containsKey(key));

		DecomposedContainerValueFactory factory = manipulator.getDecomposedContainerValueFactory();
		ContainerDetector containerDetector = createContainerDetector(factory);
		FieldExtractor fieldExtractor = createFieldExtractor(nameResolver);

		ValueAnalyzer analyzer = new ValueAnalyzer(containerDetector, fieldExtractor);
		ValueAnalysisResult result = analyzer.analyzeDecomposed(value, pathExpression.toExpression());

		interfaceResolvers.addAll(result.getInterfaceResolvers());
		genericTypeResolvers.addAll(result.getGenericTypeResolvers());

		// Add container size resolvers with sequence-based priority
		// Only add if this sequence is higher than the existing one for the same path
		for (PathResolver<ContainerSizeResolver> csr : result.getContainerSizeResolvers()) {
			PathExpression resolverPath = getResolverPath(csr);
			if (resolverPath != null) {
				Integer existingSeq = containerSizeSequenceByPath.get(resolverPath);
				if (existingSeq != null && existingSeq > factorySequence) {
					continue;
				}
				// Remove old CSR for the same path
				if (existingSeq != null) {
					containerSizeResolvers.removeIf(existing -> resolverPath.equals(getResolverPath(existing)));
				}
				containerSizeSequenceByPath.put(resolverPath, factorySequence);
			}
			containerSizeResolvers.add(csr);
		}

		// Only add values if they have higher sequence than existing values
		for (Map.Entry<String, @Nullable Object> entry : result.getValuesByPath().entrySet()) {
			PathExpression path = PathExpression.of(entry.getKey());
			Integer existingSequence = valueOrderByPath.get(path);
			if (existingSequence == null || existingSequence < factorySequence) {
				if (existingSequence != null) {
					recordCollisionIfExists(
						path,
						factorySequence,
						entry.getValue(),
						valuesByPath,
						valueOrderByPath,
						nodeCollisions
					);
				}
				valuesByPath.put(path, entry.getValue());
				valueOrderByPath.put(path, factorySequence);
			}
		}

		// For root "$" decomposed value, remove the whole object only if there are
		// decomposed child values. This allows container info to override container sizes.
		// For terminal types (String, Integer, etc.), keep the "$" value since there
		// are no child values to decompose into.
		if (pathExpression.isRoot()) {
			boolean hasChildValues = result
				.getValuesByPath()
				.keySet()
				.stream()
				.map(PathExpression::of)
				.anyMatch(path -> !path.isRoot() && path.startsWith(ROOT_PATH));
			if (hasChildValues) {
				valuesByPath.remove(ROOT_PATH);
			}
		}
	}

	/**
	 * Analyzes a lazy manipulator by evaluating it immediately and decomposing the result.
	 * This follows the same flow as decomposed value manipulators, ensuring consistent
	 * value ordering and priority handling.
	 */
	private static void analyzeLazyManipulator(
		NodeSetLazyManipulator<?> manipulator,
		PathExpression pathExpression,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		List<PathExpression> justPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		DecomposeNameResolver nameResolver
	) {
		LazyArbitrary<?> lazyArbitrary = manipulator.getLazyArbitrary();
		Object value = lazyArbitrary.getValue();

		if (value == null) {
			// Store null value explicitly so that null validation can occur at assembly time
			// (e.g., Map key null check: "Map key cannot be null.")
			recordCollisionIfExists(
				pathExpression,
				manipulator.getSequence(),
				null,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions
			);
			valuesByPath.put(pathExpression, null);
			valueOrderByPath.put(pathExpression, manipulator.getSequence());
			lazyArbitrary.clear();
			return;
		}

		// Unwrap Arbitrary - sample it to get the actual value
		if (value instanceof Arbitrary) {
			value = ((Arbitrary<?>)value).sample();
			if (value == null) {
				recordCollisionIfExists(
					pathExpression,
					manipulator.getSequence(),
					null,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, null);
				valueOrderByPath.put(pathExpression, manipulator.getSequence());
				lazyArbitrary.clear();
				return;
			}
		}

		// Unwrap Values.Just - treat as immutable just value
		if (value instanceof Values.Just) {
			Object justValue = ((Values.Just)value).getValue();
			if (justValue != null) {
				recordCollisionIfExists(
					pathExpression,
					manipulator.getSequence(),
					justValue,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, justValue);
				valueOrderByPath.put(pathExpression, manipulator.getSequence());
			}
			justPaths.add(pathExpression);
			lazyArbitrary.clear();
			return;
		}

		// Use the manipulator's factory sequence for priority comparison
		int factorySequence = manipulator.getSequence();

		// Remove existing child paths under this path before adding new values
		// Only remove child paths with lower sequence (older values)
		valuesByPath
			.keySet()
			.removeIf(
				key ->
					key.isChildOf(pathExpression)
						&& valueOrderByPath.getOrDefault(key, Integer.MIN_VALUE) < factorySequence
			);
		valueOrderByPath.keySet().removeIf(key -> key.isChildOf(pathExpression) && !valuesByPath.containsKey(key));

		// Use standard container detector and name-resolving field extractor for lazy values
		ContainerDetector containerDetector = ContainerDetector.standard();
		FieldExtractor fieldExtractor = createFieldExtractor(nameResolver);

		ValueAnalyzer analyzer = new ValueAnalyzer(containerDetector, fieldExtractor);
		ValueAnalysisResult result = analyzer.analyzeDecomposed(value, pathExpression.toExpression());

		interfaceResolvers.addAll(result.getInterfaceResolvers());
		genericTypeResolvers.addAll(result.getGenericTypeResolvers());

		// Add container size resolvers with sequence-based priority
		for (PathResolver<ContainerSizeResolver> csr : result.getContainerSizeResolvers()) {
			PathExpression resolverPath = getResolverPath(csr);
			if (resolverPath != null) {
				Integer existingSeq = containerSizeSequenceByPath.get(resolverPath);
				if (existingSeq != null && existingSeq > factorySequence) {
					continue;
				}
				if (existingSeq != null) {
					containerSizeResolvers.removeIf(existing -> resolverPath.equals(getResolverPath(existing)));
				}
				containerSizeSequenceByPath.put(resolverPath, factorySequence);
			}
			containerSizeResolvers.add(csr);
		}

		// Only add values if they have higher sequence than existing values
		for (Map.Entry<String, @Nullable Object> entry : result.getValuesByPath().entrySet()) {
			PathExpression path = PathExpression.of(entry.getKey());
			Integer existingSequence = valueOrderByPath.get(path);
			if (existingSequence == null || existingSequence < factorySequence) {
				if (existingSequence != null) {
					recordCollisionIfExists(
						path,
						factorySequence,
						entry.getValue(),
						valuesByPath,
						valueOrderByPath,
						nodeCollisions
					);
				}
				valuesByPath.put(path, entry.getValue());
				valueOrderByPath.put(path, factorySequence);
			}
		}

		// For root "$" lazy value, remove the whole object only if there are
		// decomposed child values. For terminal types, keep the "$" value.
		if (pathExpression.isRoot()) {
			boolean hasChildValues = result
				.getValuesByPath()
				.keySet()
				.stream()
				.map(PathExpression::of)
				.anyMatch(path -> !path.isRoot() && path.startsWith(ROOT_PATH));
			if (hasChildValues) {
				valuesByPath.remove(ROOT_PATH);
			}
		}

		// Clear the lazy after evaluation to prevent re-execution
		lazyArbitrary.clear();
	}

	private static void recordCollisionIfExists(
		PathExpression path,
		int newOrder,
		@Nullable Object newValue,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions
	) {
		if (valuesByPath.containsKey(path)) {
			Object previousValue = valuesByPath.get(path);
			int previousOrder = valueOrderByPath.getOrDefault(path, -1);
			nodeCollisions.add(
				new ResolutionTrace.NodeCollision(path.toExpression(), previousOrder, previousValue, newOrder, newValue)
			);
		}
	}

	/**
	 * Creates a ContainerDetector that uses DecomposedContainerValueFactory.
	 */
	private static ContainerDetector createContainerDetector(DecomposedContainerValueFactory factory) {
		return value -> {
			if (value == null) {
				return OptionalInt.empty();
			}
			try {
				DecomposableJavaContainer decomposed = factory.from(value);
				if (decomposed != null) {
					return OptionalInt.of(decomposed.getSize());
				}
				return OptionalInt.empty();
			} catch (IllegalArgumentException e) {
				return OptionalInt.empty();
			}
		};
	}

	/**
	 * Creates a FieldExtractor that uses Property-based field extraction.
	 *
	 * @param nameResolver resolves property names for path expressions
	 */
	private static FieldExtractor createFieldExtractor(DecomposeNameResolver nameResolver) {
		return (value, basePath) -> {
			Map<String, @Nullable Object> result = new HashMap<>();

			if (value == null) {
				return result;
			}

			Class<?> clazz = value.getClass();

			if (clazz.isPrimitive() || clazz == String.class || clazz.isEnum() || clazz.isArray()) {
				return result;
			}

			if (isBoxedPrimitive(clazz)) {
				return result;
			}

			if (
				value instanceof Collection
					|| value instanceof Map
					|| value instanceof Iterator
					|| value instanceof Stream
			) {
				return result;
			}

			if (isJavaType(clazz)) {
				return result;
			}

			AnnotatedType annotatedType =
				generateAnnotatedTypeWithoutAnnotation(clazz);
			Property parentProperty = new TypeParameterProperty(annotatedType);
			List<Property> childProperties = FIELD_PROPERTY_GENERATOR.generateChildProperties(parentProperty);

			for (Property childProperty : childProperties) {
				String childPath = basePath + "." + nameResolver.resolve(childProperty);
				result.put(childPath, childProperty.getValue(value));
			}

			return result;
		};
	}

	/**
	 * Extracts the path expression from a ContainerSizeResolver if available.
	 */
	private static @Nullable PathExpression getResolverPath(PathResolver<ContainerSizeResolver> resolver) {
		if (resolver instanceof PathContainerSizeResolver) {
			PathContainerSizeResolver sizeResolver = (PathContainerSizeResolver)resolver;
			return sizeResolver.getPattern();
		}
		return null;
	}

	private static PostConditionFilter extractPostConditionFilter(NodeFilterManipulator filterManipulator) {
		Class<?> type = filterManipulator.getType();
		Predicate<?> filter = filterManipulator.getFilter();
		return new PostConditionFilter(type, filter);
	}

	/**
	 * Checks if any manipulator in the list contains a StaticNodeResolver,
	 * indicating the presence of registered manipulators.
	 *
	 * @param manipulators the list of manipulators to check
	 * @return true if any manipulator uses StaticNodeResolver
	 */
	public static boolean hasRegisteredManipulators(List<ArbitraryManipulator> manipulators) {
		for (ArbitraryManipulator manipulator : manipulators) {
			if (hasStaticNodeResolver(manipulator.getNodeResolver())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the NodeResolver contains a StaticNodeResolver, indicating this is
	 * a registered manipulator (from registerBuilder).
	 *
	 * @param nodeResolver the node resolver to check
	 * @return true if it contains a StaticNodeResolver
	 */
	public static boolean hasStaticNodeResolver(NodeResolver nodeResolver) {
		if (nodeResolver instanceof StaticNodeResolver) {
			return true;
		} else if (nodeResolver instanceof CompositeNodeResolver) {
			CompositeNodeResolver composite = (CompositeNodeResolver)nodeResolver;
			for (NodeResolver childResolver : composite.getNodeResolvers()) {
				if (hasStaticNodeResolver(childResolver)) {
					return true;
				}
			}
		} else if (nodeResolver instanceof ApplyStrictModeResolver) {
			ApplyStrictModeResolver strict = (ApplyStrictModeResolver)nodeResolver;
			return hasStaticNodeResolver(strict.getNodeResolver());
		}
		return false;
	}

	/**
	 * Checks if the NodeResolver contains an ApplyStrictModeResolver, indicating this
	 * manipulator should throw an exception if the path doesn't exist.
	 */
	private static boolean hasStrictModeResolver(NodeResolver nodeResolver) {
		if (nodeResolver instanceof ApplyStrictModeResolver) {
			return true;
		} else if (nodeResolver instanceof CompositeNodeResolver) {
			CompositeNodeResolver composite = (CompositeNodeResolver)nodeResolver;
			for (NodeResolver childResolver : composite.getNodeResolvers()) {
				if (hasStrictModeResolver(childResolver)) {
					return true;
				}
			}
		}
		return false;
	}

	private static List<NextNodePredicate> extractPredicates(NodeResolver nodeResolver) {
		List<NextNodePredicate> predicates = new ArrayList<>();
		extractPredicatesRecursive(nodeResolver, predicates);
		return predicates;
	}

	private static void extractPredicatesRecursive(NodeResolver nodeResolver, List<NextNodePredicate> predicates) {
		if (nodeResolver instanceof ApplyStrictModeResolver) {
			// Unwrap strict mode resolver and continue with the inner resolver
			ApplyStrictModeResolver strictResolver = (ApplyStrictModeResolver)nodeResolver;
			extractPredicatesRecursive(strictResolver.getNodeResolver(), predicates);
		} else if (nodeResolver instanceof CompositeNodeResolver) {
			CompositeNodeResolver composite = (CompositeNodeResolver)nodeResolver;
			for (NodeResolver childResolver : composite.getNodeResolvers()) {
				extractPredicatesRecursive(childResolver, predicates);
			}
		} else if (nodeResolver instanceof NodePredicateResolver) {
			NextNodePredicate predicate = ((NodePredicateResolver)nodeResolver).getNextNodePredicate();
			if (predicate != null) {
				predicates.add(predicate);
			}
		}
	}

	/**
	 * Resolves the name of a property for use in decomposed path expressions.
	 * <p>
	 * This allows plugins (e.g., Jackson) to map Java field names to their
	 * serialized names (e.g., {@code @JsonProperty} values) during decompose,
	 * ensuring decomposed paths match the tree node paths built by the assembler.
	 */
	@FunctionalInterface
	public interface DecomposeNameResolver {
		@Nullable String resolve(Property property);
	}
}
