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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * Analyzes values to extract resolver information and decomposed field values.
 * <p>
 * ValueAnalyzer inspects an object graph and extracts:
 * <ul>
 *   <li>Interface resolvers for concrete types</li>
 *   <li>Generic type resolvers for parameterized types</li>
 *   <li>Container size resolvers for collections, maps, and arrays</li>
 *   <li>Values by path for object fields</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * ValueAnalyzer analyzer = new ValueAnalyzer();
 * ValueAnalysisResult result = analyzer.analyze(myObject, "$");
 *
 * // Use the resolvers for tree transformation
 * TransformResolverContext context = result.toResolverContext();
 *
 * // Access decomposed values
 * Map&lt;String, Object&gt; values = result.getValuesByPath();
 * </pre>
 */
public final class ValueAnalyzer {
	private final ContainerDetector containerDetector;
	private final FieldExtractor fieldExtractor;

	/**
	 * Creates a ValueAnalyzer with the standard container detector and reflection-based field extractor.
	 */
	public ValueAnalyzer() {
		this(ContainerDetector.standard(), FieldExtractor.reflection());
	}

	/**
	 * Creates a ValueAnalyzer with a custom container detector and reflection-based field extractor.
	 *
	 * @param containerDetector the container detector to use
	 */
	public ValueAnalyzer(ContainerDetector containerDetector) {
		this(containerDetector, FieldExtractor.reflection());
	}

	/**
	 * Creates a ValueAnalyzer with custom container detector and field extractor.
	 *
	 * @param containerDetector the container detector to use
	 * @param fieldExtractor    the field extractor to use
	 */
	public ValueAnalyzer(ContainerDetector containerDetector, FieldExtractor fieldExtractor) {
		this.containerDetector = containerDetector;
		this.fieldExtractor = fieldExtractor;
	}

	/**
	 * Analyzes a value and extracts resolver information.
	 *
	 * @param value          the value to analyze
	 * @param pathExpression the starting path expression (e.g., "$" or "$.fieldName")
	 * @return the analysis result containing resolvers and values by path
	 */
	public ValueAnalysisResult analyze(@Nullable Object value, String pathExpression) {
		return analyzeInternal(value, pathExpression, false);
	}

	/**
	 * Analyzes a decomposed value with deep recursive field traversal.
	 * Unlike {@link #analyze}, this method also stores container element values and
	 * recursively traverses object fields to register container size resolvers for nested containers
	 * (e.g., $.mallProduct.combinations when decomposing a root object).
	 *
	 * @param value          the value to analyze
	 * @param pathExpression the starting path expression (e.g., "$" or "$.fieldName")
	 * @return the analysis result containing resolvers and values by path
	 */
	public ValueAnalysisResult analyzeDecomposed(@Nullable Object value, String pathExpression) {
		return analyzeInternalDeep(value, pathExpression, true, 0);
	}

	private static final int MAX_DEEP_DEPTH = 5;

	private ValueAnalysisResult analyzeInternalDeep(
		@Nullable Object value,
		String pathExpression,
		boolean storeContainerElements,
		int depth
	) {
		if (value == null || depth > MAX_DEEP_DEPTH) {
			return ValueAnalysisResult.empty();
		}

		if (value instanceof Iterator || value instanceof Stream) {
			ValueAnalysisResult.Builder builder = ValueAnalysisResult.builder();
			builder.putValue(pathExpression, value);
			return builder.build();
		}

		List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		ValueAnalysisResult.Builder builder = ValueAnalysisResult.builder();

		builder.putValue(pathExpression, value);

		boolean isContainer = containerDetector.isContainer(value);

		extractContainerSizeResolverInternal(
			value,
			pathExpression,
			containerSizeResolvers,
			interfaceResolvers,
			genericTypeResolvers,
			builder,
			storeContainerElements
		);

		if (!isContainer) {
			extractFieldValues(value, pathExpression, builder);
			extractFieldContainerSizeResolversInternal(
				value,
				pathExpression,
				containerSizeResolvers,
				interfaceResolvers,
				genericTypeResolvers,
				builder,
				storeContainerElements
			);

			// Recursively analyze non-container object fields for nested container CSRs
			Map<String, @Nullable Object> fieldValues = fieldExtractor.extractFields(value, pathExpression);
			for (Map.Entry<String, @Nullable Object> entry : fieldValues.entrySet()) {
				String fieldPath = entry.getKey();
				Object fieldValue = entry.getValue();
				if (fieldValue != null && !containerDetector.isContainer(fieldValue)) {
					ValueAnalysisResult nestedResult = analyzeInternalDeep(
						fieldValue,
						fieldPath,
						storeContainerElements,
						depth + 1
					);
					containerSizeResolvers.addAll(nestedResult.getContainerSizeResolvers());
					interfaceResolvers.addAll(nestedResult.getInterfaceResolvers());
					genericTypeResolvers.addAll(nestedResult.getGenericTypeResolvers());
				}
			}
		}

		extractGenericTypeResolver(value, pathExpression, genericTypeResolvers);

		if (requiresInterfaceResolver(value)) {
			PathResolver<InterfaceResolver> interfaceResolver = InterfaceResolverConverter.fromValue(
				pathExpression,
				value
			);
			if (interfaceResolver != null) {
				interfaceResolvers.add(interfaceResolver);
			}
		}

		return builder
			.addAllInterfaceResolvers(interfaceResolvers)
			.addAllGenericTypeResolvers(genericTypeResolvers)
			.addAllContainerSizeResolvers(containerSizeResolvers)
			.build();
	}

	private ValueAnalysisResult analyzeInternal(
		@Nullable Object value,
		String pathExpression,
		boolean storeContainerElements
	) {
		if (value == null) {
			return ValueAnalysisResult.empty();
		}

		if (value instanceof Iterator || value instanceof Stream) {
			ValueAnalysisResult.Builder builder = ValueAnalysisResult.builder();
			builder.putValue(pathExpression, value);
			return builder.build();
		}

		List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		ValueAnalysisResult.Builder builder = ValueAnalysisResult.builder();

		builder.putValue(pathExpression, value);

		boolean isContainer = containerDetector.isContainer(value);

		extractContainerSizeResolverInternal(
			value,
			pathExpression,
			containerSizeResolvers,
			interfaceResolvers,
			genericTypeResolvers,
			builder,
			storeContainerElements
		);

		if (!isContainer) {
			extractFieldValues(value, pathExpression, builder);
			extractFieldContainerSizeResolversInternal(
				value,
				pathExpression,
				containerSizeResolvers,
				interfaceResolvers,
				genericTypeResolvers,
				builder,
				storeContainerElements
			);
		}

		extractGenericTypeResolver(value, pathExpression, genericTypeResolvers);

		if (requiresInterfaceResolver(value)) {
			PathResolver<InterfaceResolver> interfaceResolver = InterfaceResolverConverter.fromValue(
				pathExpression,
				value
			);
			if (interfaceResolver != null) {
				interfaceResolvers.add(interfaceResolver);
			}
		}

		return builder
			.addAllInterfaceResolvers(interfaceResolvers)
			.addAllGenericTypeResolvers(genericTypeResolvers)
			.addAllContainerSizeResolvers(containerSizeResolvers)
			.build();
	}

	private void extractContainerSizeResolver(
		Object value,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.Builder builder
	) {
		extractContainerSizeResolverInternal(
			value,
			pathExpression,
			containerSizeResolvers,
			interfaceResolvers,
			genericTypeResolvers,
			builder,
			false
		);
	}

	private void extractContainerSizeResolverInternal(
		Object value,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.Builder builder,
		boolean storeContainerElements
	) {
		OptionalInt containerSize = containerDetector.getContainerSize(value);
		if (!containerSize.isPresent()) {
			return;
		}

		int size = containerSize.getAsInt();
		PathExpression pattern = PathExpression.of(pathExpression);
		ContainerSizeResolver sizeResolver = new FixedContainerSizeResolver(size);
		containerSizeResolvers.add(new PathContainerSizeResolver(pattern, sizeResolver));

		// For nested containers, store values only if storeContainerElements is true
		// This allows decomposed values to preserve container elements while letting container info override sizes
		analyzeNestedContainers(
			value,
			pathExpression,
			containerSizeResolvers,
			interfaceResolvers,
			genericTypeResolvers,
			storeContainerElements ? builder : null
		);
	}

	private void analyzeNestedContainers(
		Object value,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.@Nullable Builder builder
	) {
		if (value instanceof Collection) {
			analyzeCollectionElements(
				(Collection<?>)value,
				pathExpression,
				containerSizeResolvers,
				interfaceResolvers,
				genericTypeResolvers,
				builder
			);
		} else if (value instanceof Map) {
			analyzeMapValues(
				(Map<?, ?>)value,
				pathExpression,
				containerSizeResolvers,
				interfaceResolvers,
				genericTypeResolvers,
				builder
			);
		} else if (value.getClass().isArray()) {
			analyzeArrayElements(
				value,
				pathExpression,
				containerSizeResolvers,
				interfaceResolvers,
				genericTypeResolvers,
				builder
			);
		}
	}

	private void analyzeCollectionElements(
		Collection<?> collection,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.@Nullable Builder builder
	) {
		int index = 0;
		for (Object element : collection) {
			if (element != null) {
				String elementPath = pathExpression + "[" + index + "]";

				// Store all elements (both container and non-container) if builder is provided
				if (builder != null) {
					builder.putValue(elementPath, element);
				}

				// Only create interface/generic resolvers for non-container elements
				// Container elements are handled by extractContainerSizeResolver recursively
				if (!containerDetector.isContainer(element)) {
					if (requiresInterfaceResolver(element)) {
						PathResolver<InterfaceResolver> interfaceResolver = InterfaceResolverConverter.fromValue(
							elementPath,
							element
						);
						if (interfaceResolver != null) {
							interfaceResolvers.add(interfaceResolver);
						}
					}

					extractGenericTypeResolver(element, elementPath, genericTypeResolvers);

					// Extract field values for non-container elements
					// This ensures that when a child path is set (e.g., "$[0].str"),
					// other fields (e.g., "$[0].integer") are preserved from the original object
					if (builder != null) {
						extractFieldValues(element, elementPath, builder);
					}

					// Extract container size resolvers from element's fields
					// This ensures nested container sizes are preserved during decomposition
					// (e.g., $.values[0].values size is captured when decomposing $.values)
					extractFieldContainerSizeResolversInternal(
						element,
						elementPath,
						containerSizeResolvers,
						interfaceResolvers,
						genericTypeResolvers,
						builder,
						builder != null
					);
				}

				extractContainerSizeResolver(
					element,
					elementPath,
					containerSizeResolvers,
					interfaceResolvers,
					genericTypeResolvers,
					builder
				);
			}
			index++;
		}
	}

	private void analyzeMapValues(
		Map<?, ?> map,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.@Nullable Builder builder
	) {
		int index = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object mapValue = entry.getValue();
			if (mapValue != null) {
				String valuePath = pathExpression + "[" + index + "]";

				// Only create interface/generic resolvers for non-container elements
				// Container elements are handled by extractContainerSizeResolver recursively
				if (!containerDetector.isContainer(mapValue)) {
					if (requiresInterfaceResolver(mapValue)) {
						PathResolver<InterfaceResolver> interfaceResolver = InterfaceResolverConverter.fromValue(
							valuePath,
							mapValue
						);
						if (interfaceResolver != null) {
							interfaceResolvers.add(interfaceResolver);
						}
					}

					extractGenericTypeResolver(mapValue, valuePath, genericTypeResolvers);
				}

				extractContainerSizeResolver(
					mapValue,
					valuePath,
					containerSizeResolvers,
					interfaceResolvers,
					genericTypeResolvers,
					builder
				);
			}
			index++;
		}
	}

	private void analyzeArrayElements(
		Object array,
		String pathExpression,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.@Nullable Builder builder
	) {
		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			if (element != null) {
				String elementPath = pathExpression + "[" + i + "]";

				// Store all elements (both container and non-container) if builder is provided
				if (builder != null) {
					builder.putValue(elementPath, element);
				}

				// Only create interface/generic resolvers for non-container elements
				// Container elements are handled by extractContainerSizeResolver recursively
				if (!containerDetector.isContainer(element)) {
					if (requiresInterfaceResolver(element)) {
						PathResolver<InterfaceResolver> interfaceResolver = InterfaceResolverConverter.fromValue(
							elementPath,
							element
						);
						if (interfaceResolver != null) {
							interfaceResolvers.add(interfaceResolver);
						}
					}

					extractGenericTypeResolver(element, elementPath, genericTypeResolvers);

					// Extract field values for non-container elements
					// This ensures that when a child path is set (e.g., "$[0].str"),
					// other fields (e.g., "$[0].integer") are preserved from the original object
					if (builder != null) {
						extractFieldValues(element, elementPath, builder);
					}
				}

				extractContainerSizeResolver(
					element,
					elementPath,
					containerSizeResolvers,
					interfaceResolvers,
					genericTypeResolvers,
					builder
				);
			}
		}
	}

	private void extractGenericTypeResolver(
		Object value,
		String pathExpression,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers
	) {
		Class<?> valueClass = value.getClass();

		// Terminal types don't need generic type resolution
		if (Types.isTerminalType(valueClass)) {
			return;
		}

		// Primitive wrappers implement Comparable<T> but this is not useful for generic resolution
		if (Types.wrapperToPrimitive(valueClass) != null) {
			return;
		}

		List<JvmType> typeArguments = GenericTypeResolverConverter.extractTypeArgumentsFromValue(value);
		if (typeArguments.isEmpty()) {
			return;
		}

		PathResolver<GenericTypeResolver> resolver = GenericTypeResolverConverter.createResolver(
			pathExpression,
			typeArguments
		);
		genericTypeResolvers.add(resolver);
	}

	private void extractFieldValues(Object value, String basePath, ValueAnalysisResult.Builder builder) {
		Map<String, @Nullable Object> fieldValues = fieldExtractor.extractFields(value, basePath);
		builder.putAllValues(fieldValues);
	}

	private void extractFieldContainerSizeResolvers(
		Object value,
		String basePath,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.Builder builder
	) {
		extractFieldContainerSizeResolversInternal(
			value,
			basePath,
			containerSizeResolvers,
			interfaceResolvers,
			genericTypeResolvers,
			builder,
			false
		);
	}

	private void extractFieldContainerSizeResolversInternal(
		Object value,
		String basePath,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ValueAnalysisResult.Builder builder,
		boolean storeContainerElements
	) {
		Map<String, @Nullable Object> fieldValues = fieldExtractor.extractFields(value, basePath);

		for (Map.Entry<String, @Nullable Object> entry : fieldValues.entrySet()) {
			String fieldPath = entry.getKey();
			Object fieldValue = entry.getValue();
			if (fieldValue != null) {
				extractContainerSizeResolverInternal(
					fieldValue,
					fieldPath,
					containerSizeResolvers,
					interfaceResolvers,
					genericTypeResolvers,
					builder,
					storeContainerElements
				);
			}
		}
	}

	private static boolean requiresInterfaceResolver(Object value) {
		Class<?> valueClass = value.getClass();

		// Terminal types don't need interface resolution
		if (Types.isTerminalType(valueClass)) {
			return false;
		}

		// Primitive wrappers (Long, Integer, Double, etc.) and other JDK value types
		// are leaf types that never get expanded into a node tree. Generating an
		// interface resolver for them is unnecessary and disables the
		// JvmNodeSubtreeContext (via hasPathSpecificResolvers), causing significant
		// performance degradation for simple set() calls.
		if (Types.isJdkValueType(valueClass)) {
			return false;
		}

		Class<?> superclass = valueClass.getSuperclass();
		if (superclass != null && superclass != Object.class) {
			return true;
		}

		return valueClass.getInterfaces().length > 0;
	}
}
