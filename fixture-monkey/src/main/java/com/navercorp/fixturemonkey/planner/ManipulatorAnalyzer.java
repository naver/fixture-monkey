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

package com.navercorp.fixturemonkey.planner;

import static com.navercorp.fixturemonkey.planner.AnalysisResult.PostConditionFilter;
import static com.navercorp.fixturemonkey.planner.AnalysisResult.PropertyCustomizer;
import static com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.FIELD_PROPERTY_GENERATOR;
import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;
import static com.navercorp.fixturemonkey.api.type.Types.isBoxedPrimitive;
import static com.navercorp.fixturemonkey.api.type.Types.isJavaType;
import static com.navercorp.fixturemonkey.api.type.Types.toJvmType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
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
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.customizer.CustomizerDirective;
import com.navercorp.fixturemonkey.customizer.FilterDirective;
import com.navercorp.fixturemonkey.customizer.JustDirective;
import com.navercorp.fixturemonkey.customizer.LazyDirective;
import com.navercorp.fixturemonkey.customizer.NullityDirective;
import com.navercorp.fixturemonkey.customizer.SetDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.ExtractedField;
import com.navercorp.objectfarm.api.input.FieldExtractor;
import com.navercorp.objectfarm.api.input.ValueAnalysisResult;
import com.navercorp.objectfarm.api.input.ValueAnalyzer;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;

/**
 * Analyzes {@link PathDirective} instances to extract topology-affecting information.
 * <p>
 * This analyzer walks the directive list in order and extracts:
 * <ul>
 *   <li>Interface resolution information from value setters</li>
 *   <li>Generic type hints from value types</li>
 *   <li>Container size resolvers (from explicit {@code size()} and decomposed values)</li>
 *   <li>Paths that should be excluded (null paths or just-set paths)</li>
 * </ul>
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class ManipulatorAnalyzer {
	private ManipulatorAnalyzer() {
	}

	/**
	 * Analyzes a list of {@link PathDirective}s and extracts topology-affecting information.
	 * <p>
	 * The {@code nameResolver} is used when decomposing {@code set("$", complexObject)} values so
	 * plugin-specific naming (e.g., Jackson {@code @JsonProperty}) is preserved on the produced
	 * child paths. Pass {@code null} to fall back to {@link Property#getName()}.
	 *
	 * @param directives   the list of directives to analyze
	 * @param nameResolver per-property name resolver applied to decomposed child paths
	 */
	public static AnalysisResult analyze(
		List<PathDirective> directives,
		@Nullable Function<Property, String> nameResolver
	) {
		List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		Map<PathExpression, Integer> containerSizeSequenceByPath = new HashMap<>();
		Map<PathExpression, SizeDirective> latestSizeDirectiveByPath = new HashMap<>();
		List<PathExpression> justPaths = new ArrayList<>();
		Set<PathExpression> notNullPaths = new HashSet<>();
		Map<PathExpression, @Nullable Object> valuesByPath = new HashMap<>();
		Map<PathExpression, List<PostConditionFilter>> filtersByPath = new HashMap<>();
		Map<PathExpression, Integer> limitsByPath = new HashMap<>();
		Map<PathExpression, Integer> valueOrderByPath = new HashMap<>();
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath = new HashMap<>();
		List<ResolutionTrace.NodeCollision> nodeCollisions = new ArrayList<>();
		boolean strictMode = false;

		for (int order = 0; order < directives.size(); order++) {
			PathDirective directive = directives.get(order);

			// Registered directives are handled via type-based resolution, not path-based resolution
			if (directive.registered()) {
				continue;
			}

			if (!strictMode && directive.strict()) {
				strictMode = true;
			}

			analyzeDirective(
				directive,
				order,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				latestSizeDirectiveByPath,
				justPaths,
				notNullPaths,
				valuesByPath,
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
			latestSizeDirectiveByPath,
			justPaths,
			notNullPaths,
			valuesByPath,
			filtersByPath,
			limitsByPath,
			valueOrderByPath,
			customizersByPath,
			nodeCollisions,
			strictMode
		);
	}

	/**
	 * Returns an empty AnalysisResult for cases where there are no directives.
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
		Collections.emptyMap(),
		Collections.emptyList(),
		Collections.emptySet(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyMap(),
		Collections.emptyList(),
		false
	);

	private static void analyzeDirective(
		PathDirective directive,
		int order,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		Map<PathExpression, SizeDirective> latestSizeDirectiveByPath,
		List<PathExpression> justPaths,
		Set<PathExpression> notNullPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, List<PostConditionFilter>> filtersByPath,
		Map<PathExpression, Integer> limitsByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		Map<PathExpression, List<PropertyCustomizer>> customizersByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		@Nullable Function<Property, String> nameResolver
	) {
		PathExpression pathExpression = directive.path();
		int limit = directive.limit();
		if (limit > 0) {
			limitsByPath.put(pathExpression, limit);
		}

		if (directive instanceof FilterDirective) {
			FilterDirective filterDirective = (FilterDirective)directive;
			filtersByPath
				.computeIfAbsent(pathExpression, k -> new ArrayList<>())
				.add(new PostConditionFilter(filterDirective.type(), filterDirective.filter()));
			return;
		}

		if (directive instanceof CustomizerDirective) {
			CustomizerDirective<?> customizerDirective = (CustomizerDirective<?>)directive;
			@SuppressWarnings({"unchecked", "rawtypes"})
			Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer =
				(Function)customizerDirective.customizer();
			boolean afterSet = valuesByPath.containsKey(pathExpression);
			customizersByPath
				.computeIfAbsent(pathExpression, k -> new ArrayList<>())
				.add(new PropertyCustomizer(customizer, order, afterSet));
			return;
		}

		if (directive instanceof SetDirective) {
			analyzeSetDirective(
				(SetDirective)directive,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				nameResolver
			);
			return;
		}

		if (directive instanceof JustDirective) {
			JustDirective justDirective = (JustDirective)directive;
			Object value = justDirective.value();
			if (value != null) {
				recordCollisionIfExists(pathExpression, order, value, valuesByPath, valueOrderByPath, nodeCollisions);
				valuesByPath.put(pathExpression, value);
				valueOrderByPath.put(pathExpression, order);
			}
			justPaths.add(pathExpression);
			return;
		}

		if (directive instanceof NullityDirective) {
			NullityDirective nullityDirective = (NullityDirective)directive;
			int nullitySequence = nullityDirective.sequence();
			if (nullityDirective.toNull()) {
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
				if (valuesByPath.containsKey(pathExpression) && valuesByPath.get(pathExpression) == null) {
					valuesByPath.remove(pathExpression);
					valueOrderByPath.remove(pathExpression);
				}
				notNullPaths.add(pathExpression);
			}
			return;
		}

		if (directive instanceof LazyDirective) {
			analyzeLazyDirective(
				(LazyDirective)directive,
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
			return;
		}

		if (directive instanceof SizeDirective) {
			analyzeSizeDirective(
				(SizeDirective)directive,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				latestSizeDirectiveByPath
			);
		}
	}

	private static void analyzeSizeDirective(
		SizeDirective directive,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		Map<PathExpression, SizeDirective> latestSizeDirectiveByPath
	) {
		PathExpression path = directive.path();
		int sequence = directive.sequence();

		Integer existingSequence = containerSizeSequenceByPath.get(path);
		if (existingSequence != null && existingSequence > sequence) {
			return;
		}
		if (existingSequence != null) {
			containerSizeResolvers.removeIf(existing -> path.equals(getResolverPath(existing)));
		}
		containerSizeSequenceByPath.put(path, sequence);
		latestSizeDirectiveByPath.put(path, directive);
		containerSizeResolvers.add(buildExplicitSizeResolver(directive));
	}

	private static PathResolver<ContainerSizeResolver> buildExplicitSizeResolver(SizeDirective directive) {
		ArbitraryContainerInfo containerInfo = directive.containerInfo();
		int fixedSize = containerInfo.getRandomSize();

		ContainerSizeResolver sizeResolver = containerType -> {
			if (containerType == null) {
				return fixedSize;
			}
			Class<?> rawType = containerType.getRawType();
			List<? extends com.navercorp.objectfarm.api.type.JvmType> typeVariables = containerType.getTypeVariables();
			Class<?> enumType = null;
			if (!typeVariables.isEmpty()) {
				if (java.util.Set.class.isAssignableFrom(rawType)) {
					Class<?> elementType = typeVariables.get(0).getRawType();
					if (elementType.isEnum()) {
						enumType = elementType;
					}
				} else if (Map.class.isAssignableFrom(rawType)) {
					Class<?> keyType = typeVariables.get(0).getRawType();
					if (keyType.isEnum()) {
						enumType = keyType;
					}
				}
			}
			if (enumType == null) {
				return fixedSize;
			}
			Object[] constants = enumType.getEnumConstants();
			return constants != null ? Math.min(fixedSize, constants.length) : fixedSize;
		};

		return new PathContainerSizeResolver(directive.path(), sizeResolver);
	}

	private static void analyzeSetDirective(
		SetDirective directive,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		@Nullable Function<Property, String> nameResolver
	) {
		PathExpression pathExpression = directive.path();
		Object value = directive.value();
		if (value == null) {
			return;
		}

		int factorySequence = directive.sequence();

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

		DecomposedContainerValueFactory factory = directive.decomposedContainerValueFactory();
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
	 * Analyzes a lazy directive by evaluating it immediately and decomposing the result.
	 * This follows the same flow as decomposed value directives, ensuring consistent
	 * value ordering and priority handling.
	 */
	private static void analyzeLazyDirective(
		LazyDirective directive,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		Map<PathExpression, Integer> containerSizeSequenceByPath,
		List<PathExpression> justPaths,
		Map<PathExpression, @Nullable Object> valuesByPath,
		Map<PathExpression, Integer> valueOrderByPath,
		List<ResolutionTrace.NodeCollision> nodeCollisions,
		@Nullable Function<Property, String> nameResolver
	) {
		PathExpression pathExpression = directive.path();
		LazyArbitrary<?> lazyArbitrary = directive.lazyArbitrary();
		Object value = lazyArbitrary.getValue();

		if (value == null) {
			// Store null value explicitly so that null validation can occur at assembly time
			// (e.g., Map key null check: "Map key cannot be null.")
			recordCollisionIfExists(
				pathExpression,
				directive.sequence(),
				null,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions
			);
			valuesByPath.put(pathExpression, null);
			valueOrderByPath.put(pathExpression, directive.sequence());
			lazyArbitrary.clear();
			return;
		}

		// Unwrap Arbitrary - sample it to get the actual value
		if (value instanceof Arbitrary) {
			value = ((Arbitrary<?>)value).sample();
			if (value == null) {
				recordCollisionIfExists(
					pathExpression,
					directive.sequence(),
					null,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, null);
				valueOrderByPath.put(pathExpression, directive.sequence());
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
					directive.sequence(),
					justValue,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, justValue);
				valueOrderByPath.put(pathExpression, directive.sequence());
			}
			justPaths.add(pathExpression);
			lazyArbitrary.clear();
			return;
		}

		int factorySequence = directive.sequence();

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

		// Use standard container detector and property-aware field extractor for lazy values
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
	 * Creates a FieldExtractor for value decomposition.
	 * <p>
	 * Uses {@link FieldPropertyGenerator}/{@link com.navercorp.fixturemonkey.api.property.FieldProperty}
	 * to enumerate child properties so plugin-specific {@link PropertyNameResolver}s (e.g., Jackson
	 * {@code @JsonProperty}) take effect, but reads each value via direct {@link Field#get(Object)}
	 * — never through {@code Property.getValue}.
	 */
	@SuppressWarnings({"argument", "type.argument", "methodref.return", "return"})
	private static FieldExtractor createFieldExtractor(@Nullable Function<Property, String> nameResolver) {
		Function<Property, String> resolver = nameResolver != null ? nameResolver : Property::getName;
		return new FieldExtractor() {
			@Override
			public Map<String, ExtractedField> extractFields(@Nullable Object value, String basePath) {
				List<Property> childProperties = getChildProperties(value);
				if (childProperties == null) {
					return new HashMap<>();
				}

				Map<String, ExtractedField> result = new HashMap<>();
				for (Property childProperty : childProperties) {
					String name = resolver.apply(childProperty);
					if (name == null) {
						continue;
					}
					String childPath = basePath + "." + name;
					Class<?> declaredType = childProperty.getJvmType().getRawType();
					Object fieldValue = readPropertyValue(childProperty, value);
					result.put(childPath, new ExtractedField(fieldValue, declaredType));
				}
				return result;
			}

			private @Nullable List<Property> getChildProperties(@Nullable Object value) {
				if (value == null) {
					return null;
				}
				Class<?> clazz = value.getClass();
				if (clazz.isPrimitive() || clazz == String.class || clazz.isEnum() || clazz.isArray()) {
					return null;
				}
				if (isBoxedPrimitive(clazz)) {
					return null;
				}
				if (
					value instanceof Collection
						|| value instanceof Map
						|| value instanceof Iterator
						|| value instanceof Stream
				) {
					return null;
				}
				if (isJavaType(clazz)) {
					return null;
				}
				AnnotatedType annotatedType = generateAnnotatedTypeWithoutAnnotation(clazz);
				Property parentProperty = new TypeParameterProperty(toJvmType(annotatedType, Collections.emptyList()));
				return FIELD_PROPERTY_GENERATOR.generateChildProperties(parentProperty);
			}

			private @Nullable Object readPropertyValue(Property property, @Nullable Object instance) {
				if (instance == null || !(property instanceof FieldProperty)) {
					return null;
				}
				Field field = ((FieldProperty)property).getField();
				try {
					field.setAccessible(true);
					return field.get(instance);
				} catch (IllegalAccessException ex) {
					return null;
				}
			}
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

}
