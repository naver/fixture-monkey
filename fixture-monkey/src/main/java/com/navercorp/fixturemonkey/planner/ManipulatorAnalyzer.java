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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.CustomizerDirective;
import com.navercorp.fixturemonkey.customizer.FilterDirective;
import com.navercorp.fixturemonkey.customizer.JustDirective;
import com.navercorp.fixturemonkey.customizer.LazyDirective;
import com.navercorp.fixturemonkey.customizer.NullityDirective;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.SetDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.decompose.DecomposedContainerDetector;
import com.navercorp.fixturemonkey.decompose.PropertyFieldExtractor;
import com.navercorp.fixturemonkey.tracing.ResolutionTrace;
import com.navercorp.fixturemonkey.tree.SeedPurpose;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.FieldExtractor;
import com.navercorp.objectfarm.api.input.InlinedValueResolver;
import com.navercorp.objectfarm.api.input.ValueAnalysisResult;
import com.navercorp.objectfarm.api.input.ValueAnalyzer;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.SeedSnapshot;
import com.navercorp.objectfarm.api.tree.PathContainerSizeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;

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
	 * Analyzes the root scope's directives and extracts topology-affecting information.
	 * <p>
	 * The {@code nameResolver} is used when decomposing {@code set("$", complexObject)} values so
	 * plugin-specific naming (e.g., Jackson {@code @JsonProperty}) is preserved on the produced
	 * child paths. Pass {@code null} to fall back to {@link Property#getName()}.
	 *
	 * @param rootScope               the root scope, with the directives the sampled builder declared in order
	 * @param nameResolver            per-property name resolver applied to decomposed child paths
	 * @param inlinedValueResolver    reconstructs value types a JVM language inlined into the
	 *                                decomposed object's fields
	 */
	public static AnalysisResult analyze(
		Scope rootScope,
		@Nullable Function<Property, String> nameResolver,
		InlinedValueResolver inlinedValueResolver
	) {
		AnalyzedScope declared = new AnalyzedScope(rootScope);
		PlanningValueStore values = new PlanningValueStore(nameResolver, inlinedValueResolver);
		boolean strictMode = false;
		for (PathDirective directive : rootScope.getDirectives()) {
			if (!strictMode && directive.strict()) {
				strictMode = true;
			}
			interpret(directive, declared, values);
		}
		return values.toResult(declared, strictMode);
	}

	/**
	 * Analyzes each defined scope, keeping their order.
	 *
	 * @param definedScopes the defined scopes, the one with the lowest precedence first
	 * @return the analyzed scopes, in the same order
	 */
	public static List<AnalyzedScope> analyze(List<Scope> definedScopes) {
		List<AnalyzedScope> analyzed = new ArrayList<>(definedScopes.size());
		for (Scope scope : definedScopes) {
			analyzed.add(analyze(scope));
		}
		return analyzed;
	}

	/**
	 * Analyzes the directives declared for a defined scope, with paths relative to the node the scope selects.
	 * Values stay as declared: unlike the root scope's, they are expanded during assembly, where the nodes the scope
	 * selects become known.
	 *
	 * @param scope the scope, with the directives declared for it in order
	 * @return the scope's directives by relative path
	 */
	public static AnalyzedScope analyze(Scope scope) {
		AnalyzedScope declared = new AnalyzedScope(scope);
		AssemblyValueStore values = new AssemblyValueStore(declared);
		for (PathDirective directive : scope.getDirectives()) {
			interpret(directive, declared, values);
		}
		return declared;
	}

	private static void interpret(PathDirective directive, AnalyzedScope declared, ValueStore values) {
		PathExpression path = directive.path();
		int limit = directive.limit();
		if (limit != PathDirective.UNLIMITED) {
			declared.putLimit(path, limit);
		}
		if (directive instanceof FilterDirective) {
			FilterDirective filter = (FilterDirective)directive;
			declared.addFilter(path, new PostConditionFilter(filter.type(), filter.filter()));
		} else if (directive instanceof CustomizerDirective) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer =
				(Function)((CustomizerDirective<?>)directive).customizer();
			declared.addCustomizer(path, new PropertyCustomizer(customizer));
		} else if (directive instanceof SetDirective) {
			declared.overrideCustomizersAt(path);
			values.set((SetDirective)directive);
		} else if (directive instanceof JustDirective) {
			declared.overrideCustomizersAt(path);
			values.just((JustDirective)directive);
		} else if (directive instanceof NullityDirective) {
			NullityDirective nullity = (NullityDirective)directive;
			if (nullity.toNull()) {
				declared.overrideCustomizersAt(path);
				values.setNull(nullity);
				declared.unmarkNotNull(path);
			} else {
				values.clearNull(path);
				declared.markNotNull(path);
			}
		} else if (directive instanceof LazyDirective) {
			declared.overrideCustomizersAt(path);
			values.lazy((LazyDirective)directive);
		} else if (directive instanceof SizeDirective) {
			values.size((SizeDirective)directive);
		}
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
		Collections.emptyMap(),
		Collections.emptyMap(),
		new AnalyzedScope(Scope.root(Collections.emptyList(), Collections.emptyMap())),
		Collections.emptyList(),
		false
	);

	private interface ValueStore {
		void set(SetDirective directive);

		void just(JustDirective directive);

		void setNull(NullityDirective directive);

		void clearNull(PathExpression path);

		void lazy(LazyDirective directive);

		void size(SizeDirective directive);
	}

	private static final class AssemblyValueStore implements ValueStore {
		private final AnalyzedScope declared;

		private AssemblyValueStore(AnalyzedScope declared) {
			this.declared = declared;
		}

		@Override
		public void set(SetDirective directive) {
			declared.putValue(directive.path(), directive.value());
		}

		@Override
		public void just(JustDirective directive) {
			declared.putValue(directive.path(), directive.value());
		}

		@Override
		public void setNull(NullityDirective directive) {
			declared.putValue(directive.path(), null);
		}

		@Override
		public void clearNull(PathExpression path) {
			declared.removeNullValue(path);
		}

		@Override
		public void lazy(LazyDirective directive) {
			boolean rootLevel = directive.path().isRoot();
			if (rootLevel) {
				declared.clearSupersededByRootLazy();
			}
			declared.putValue(
				directive.path(),
				new LazyValueHolder(directive.lazyArbitrary(), declared.getSelector(), rootLevel)
			);
		}

		@Override
		public void size(SizeDirective directive) {
			declared.putContainerSize(directive.path(), directive.containerInfo());
		}

	}

	private static final class PlanningValueStore implements ValueStore {
		private final @Nullable Function<Property, String> nameResolver;
		private final InlinedValueResolver inlinedValueResolver;
		private final List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		private final Map<PathExpression, Integer> containerSizeSequenceByPath = new HashMap<>();
		private final Map<PathExpression, SizeDirective> latestSizeDirectiveByPath = new HashMap<>();
		private final List<PathExpression> justPaths = new ArrayList<>();
		private final Map<PathExpression, @Nullable Object> valuesByPath = new HashMap<>();
		private final Map<PathExpression, Integer> valueOrderByPath = new HashMap<>();
		private final List<ResolutionTrace.NodeCollision> nodeCollisions = new ArrayList<>();

		private PlanningValueStore(
			@Nullable Function<Property, String> nameResolver,
			InlinedValueResolver inlinedValueResolver
		) {
			this.nameResolver = nameResolver;
			this.inlinedValueResolver = inlinedValueResolver;
		}

		@Override
		public void set(SetDirective directive) {
			if (yieldsToJust(directive.path(), directive.value(), directive.decomposedContainerValueFactory())) {
				return;
			}
			analyzeSetDirective(
				directive,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				nameResolver,
				inlinedValueResolver
			);
		}

		@Override
		public void just(JustDirective directive) {
			PathExpression pathExpression = directive.path();
			Object value = directive.value();
			int sequence = directive.sequence();
			if (value != null) {
				recordCollisionIfExists(
					pathExpression,
					sequence,
					value,
					valuesByPath,
					valueOrderByPath,
					nodeCollisions
				);
				valuesByPath.put(pathExpression, value);
				valueOrderByPath.put(pathExpression, sequence);
			}
			justPaths.add(pathExpression);
		}

		private boolean yieldsToJust(
			PathExpression path,
			@Nullable Object value,
			DecomposedContainerValueFactory decomposedContainerValueFactory
		) {
			if (value == null || !justPaths.contains(path)) {
				return false;
			}
			ContainerDetector containerDetector = new DecomposedContainerDetector(decomposedContainerValueFactory);
			if (containerDetector.isContainer(value)) {
				return true;
			}
			String ownPath = path.toExpression();
			ValueAnalysisResult result = new ValueAnalyzer(
				containerDetector,
				new PropertyFieldExtractor(nameResolver, inlinedValueResolver)
			).analyzeDecomposed(value, ownPath);
			for (String decomposedPath : result.getValuesByPath().keySet()) {
				if (!decomposedPath.equals(ownPath)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void setNull(NullityDirective directive) {
			PathExpression pathExpression = directive.path();
			int nullitySequence = directive.sequence();
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
		}

		@Override
		public void clearNull(PathExpression path) {
			if (valuesByPath.containsKey(path) && valuesByPath.get(path) == null) {
				valuesByPath.remove(path);
				valueOrderByPath.remove(path);
			}
		}

		@Override
		public void lazy(LazyDirective directive) {
			if (justPaths.contains(directive.path())) {
				Object value = directive.lazyArbitrary().getValue();
				if (!(value instanceof Values.Just)
					&& !(value instanceof Arbitrary)
					&& yieldsToJust(directive.path(), value, directive.decomposedContainerValueFactory())) {
					directive.lazyArbitrary().clear();
					return;
				}
			}
			analyzeLazyDirective(
				directive,
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				justPaths,
				valuesByPath,
				valueOrderByPath,
				nodeCollisions,
				nameResolver,
				inlinedValueResolver
			);
		}

		@Override
		public void size(SizeDirective directive) {
			analyzeSizeDirective(
				directive,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				latestSizeDirectiveByPath
			);
		}

		private AnalysisResult toResult(AnalyzedScope declared, boolean strictMode) {
			return new AnalysisResult(
				interfaceResolvers,
				genericTypeResolvers,
				containerSizeResolvers,
				containerSizeSequenceByPath,
				latestSizeDirectiveByPath,
				justPaths,
				valuesByPath,
				valueOrderByPath,
				declared,
				nodeCollisions,
				strictMode
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
		int minSize = containerInfo.getElementMinSize();
		int maxSize = containerInfo.getElementMaxSize();

		ContainerSizeResolver sizeResolver = new ContainerSizeResolver() {
			@Override
			public int resolveContainerSize(JvmType containerType) {
				return capToEnumConstants(containerType, containerInfo.getRandomSize());
			}

			@Override
			public int resolveContainerSize(JvmType containerType, SeedSnapshot scope) {
				if (minSize == maxSize) {
					return capToEnumConstants(containerType, minSize);
				}
				Random random = SeedPurpose.SIZE.randomFor(scope, containerType.hashCode());
				return capToEnumConstants(containerType, minSize + random.nextInt(maxSize - minSize + 1));
			}
		};

		return new PathContainerSizeResolver(directive.path(), sizeResolver);
	}

	private static int capToEnumConstants(@Nullable JvmType containerType, int size) {
		if (containerType == null) {
			return size;
		}
		Class<?> rawType = containerType.getRawType();
		List<? extends JvmType> typeVariables = containerType.getTypeVariables();
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
			return size;
		}
		Object[] constants = enumType.getEnumConstants();
		return constants != null ? Math.min(size, constants.length) : size;
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
		@Nullable Function<Property, String> nameResolver,
		InlinedValueResolver inlinedValueResolver
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
		ContainerDetector containerDetector = new DecomposedContainerDetector(factory);
		FieldExtractor fieldExtractor = new PropertyFieldExtractor(nameResolver, inlinedValueResolver);

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
		@Nullable Function<Property, String> nameResolver,
		InlinedValueResolver inlinedValueResolver
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
			value = ArbitraryUtils.sample((Arbitrary<?>)value);
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

		ContainerDetector containerDetector =
			new DecomposedContainerDetector(directive.decomposedContainerValueFactory());
		FieldExtractor fieldExtractor = new PropertyFieldExtractor(nameResolver, inlinedValueResolver);

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
