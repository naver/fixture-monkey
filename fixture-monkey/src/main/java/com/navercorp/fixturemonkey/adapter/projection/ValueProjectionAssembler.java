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

package com.navercorp.fixturemonkey.adapter.projection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.BaseStream;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.NodeTreeAdapter;
import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContext;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.TraceableCombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.InterfaceSelectionStrategy;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeDefinition;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapKeyElementProperty;
import com.navercorp.fixturemonkey.api.property.MapValueElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.KeySelector;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;
import com.navercorp.objectfarm.api.expression.TypeSelector;
import com.navercorp.objectfarm.api.expression.ValueSelector;
import com.navercorp.objectfarm.api.expression.WildcardSelector;
import com.navercorp.objectfarm.api.input.ObjectValueExtractor;
import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmMapEntryNode;
import com.navercorp.objectfarm.api.node.JvmMapNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.ConstructorParamCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.FieldAccessCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.MethodInvocationCreationMethod;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

final class ValueProjectionAssembler {
	private final JvmNodeTree structure;
	private final Map<PathExpression, @Nullable Object> valuesByPath;
	private final AssembleContext context;

	ValueProjectionAssembler(
		JvmNodeTree structure,
		Map<PathExpression, @Nullable Object> valuesByPath,
		AssembleContext context
	) {
		this.structure = structure;
		this.valuesByPath = valuesByPath;
		this.context = context;
	}

	CombinableArbitrary<?> assemble() {
		JvmNode rootNode = structure.getRootNode();

		Map<PathExpression, ValueCandidate> mergedCandidates = new HashMap<>();
		Map<PathExpression, Integer> orderMap = context.getValueOrderByPath();
		for (Map.Entry<PathExpression, @Nullable Object> entry : valuesByPath.entrySet()) {
			PathExpression path = entry.getKey();
			ValueOrder order = ValueOrder.UserOrder.of(orderMap.getOrDefault(path, 0));
			mergedCandidates.put(path, new ValueCandidate(entry.getValue(), order));
		}

		// "typed" = values from register() keyed by TypeSelector paths (e.g., $[type:T]).
		// putIfAbsent ensures user-set values (above) take priority over register values.
		Map<PathExpression, @Nullable Object> typedPathValues = context.getTypedPathValues();
		Map<PathExpression, Integer> typedPathOrders = context.getTypedPathOrders();

		for (Map.Entry<PathExpression, @Nullable Object> entry : typedPathValues.entrySet()) {
			PathExpression typedPath = entry.getKey();
			mergedCandidates.putIfAbsent(
				typedPath,
				new ValueCandidate(
					entry.getValue(),
					ValueOrder.RegisterOrder.of(typedPathOrders.getOrDefault(typedPath, 0))
				)
			);
		}

		AssemblyState state = new AssemblyState(
			structure,
			mergedCandidates,
			context.getRootProperty(),
			context.getOptions(),
			context.getGeneratorContext(),
			context.getLoggingContext(),
			context.getJustPaths(),
			context.getNotNullPaths(),
			context.getFiltersByPath(),
			context.getLimitsByPath(),
			context.getInterfaceSelectionStrategy(),
			context.getCustomizersByPath(),
			context.getTraceContext(),
			context.getIntrospectorsByType(),
			context.getNodeTreeAdapter(),
			context.getUserContainerSizePaths()
		);

		if (state.traceContext.isEnabled()) {
			Map<String, @Nullable Object> traceValues = new LinkedHashMap<>();
			Map<String, Integer> traceOrders = new LinkedHashMap<>();
			Map<String, String> traceSources = new LinkedHashMap<>();
			for (Map.Entry<PathExpression, ValueCandidate> entry : mergedCandidates.entrySet()) {
				String pathStr = entry.getKey().toExpression();
				ValueCandidate candidate = entry.getValue();
				traceValues.put(pathStr, candidate.value);
				traceOrders.put(pathStr, candidate.order.sequence());
				traceSources.put(pathStr, candidate.sourceLabel());
			}
			state.traceContext.recordMergedCandidates(traceValues, traceOrders, traceSources);
		}

		return assembleNode(rootNode, state, null, null, PathExpression.root(), new HashSet<>());
	}

	@SuppressWarnings("dereference.of.nullable")
	private CombinableArbitrary<?> assembleNode(
		JvmNode node,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		FixtureMonkeyOptions options = state.options;
		JvmType currentType = node.getConcreteType();

		state.nodeByPath.put(currentPath.toExpression(), node);

		boolean isCurrentTypeContainer = state.containerTypeCache.computeIfAbsent(currentType, type ->
			computeIsContainerType(type, options)
		);

		Class<?> currentRawType = currentType.getRawType();
		boolean isCircular = !isCurrentTypeContainer && visitedTypes.contains(currentRawType);

		boolean addedToVisited = false;
		if (!isCurrentTypeContainer && visitedTypes.add(currentRawType)) {
			addedToVisited = true;
		}

		try {
			if (isUnderExcludedPath(currentPath, state.justPaths)) {
				return assembleNodeDefault(node, state, parentContext, parentPath, currentPath, visitedTypes);
			}

			boolean isValueSet = state.candidatesByPath.containsKey(currentPath);
			boolean hasChildValues = hasChildPathValues(currentPath, state.pathIndex);

			// set("field", null) vs child values priority:
			// If null was set AFTER all child values, null wins. Otherwise children win.
			if (isValueSet && hasChildValues) {
				ValueCandidate nullCandidate = state.candidatesByPath.get(currentPath);
				if (nullCandidate != null && nullCandidate.value == null) {
					boolean hasActualChildCandidates = hasChildCandidateValues(currentPath, state.candidatesByPath);
					if (!hasActualChildCandidates) {
						return wrapValueWithFiltersAndCustomizers(null, currentPath, currentRawType, state);
					}
					if (isNullSetAfterAllChildren(nullCandidate.order, currentPath, state.candidatesByPath)) {
						return wrapValueWithFiltersAndCustomizers(null, currentPath, currentRawType, state);
					}
				}
			}

			if (!hasChildValues) {
				PathExpression bestPath = findBestMatchingPath(state.candidatesByPath, currentPath, state);
				if (bestPath != null) {
					if (!bestPath.equals(currentPath)) {
						Integer remainingLimit = state.limitsByPath.get(bestPath);
						if (remainingLimit != null) {
							if (remainingLimit <= 0) {
								return assembleNodeDefault(
									node,
									state,
									parentContext,
									parentPath,
									currentPath,
									visitedTypes
								);
							}
							state.limitsByPath.put(bestPath, remainingLimit - 1);
						}
					}

					ValueCandidate bestCandidate = state.candidatesByPath.get(bestPath);
					Object rawValue = bestCandidate.value;
					boolean wasLazy = rawValue instanceof LazyValueHolder;
					Object setValue = resolveLazyValue(
						rawValue,
						bestCandidate.order instanceof ValueOrder.RegisterOrder,
						state
					);

					// LazyArbitrary detected self-referential evaluation; fall back to default generation
					if (setValue == LazyValueHolder.RECURSION_BLOCKED) {
						return assembleNodeDefault(node, state, parentContext, parentPath, currentPath, visitedTypes);
					}

					// User's notNull overrides register's null — ignore register value and generate default
					if (setValue == null
						&& bestCandidate.order instanceof ValueOrder.RegisterOrder
						&& state.notNullPaths.contains(currentPath)
					) {
						return assembleNodeDefault(node, state, parentContext, parentPath, currentPath, visitedTypes);
					}

					// Root TypeSelector matched but field-level siblings exist → decompose and fall through
					if (isRootTypeSelector(bestPath) && hasFieldLevelTypeSelectorSiblings(bestPath, state)) {
						state.candidatesByPath.put(currentPath, bestCandidate.withValue(setValue));
						isValueSet = true;
					} else {
						String source = bestCandidate.sourceLabel();
						return traceAndReturnValue(
							setValue,
							source,
							currentPath,
							currentRawType,
							isCurrentTypeContainer,
							parentContext,
							currentType,
							state
						);
					}
				}
			}

			if (isValueSet) {
				ValueCandidate currentCandidate = state.candidatesByPath.get(currentPath);
				ValueOrder parentOrder = currentCandidate != null ? currentCandidate.order : ValueOrder.UserOrder.of(0);
				DecomposeResult decomposeResult = state.valueDecomposer.decompose(
					currentPath,
					currentRawType,
					isCurrentTypeContainer,
					parentOrder
				);
				applyDecomposeResult(decomposeResult, state);
				if (decomposeResult.hasEarlyReturn()) {
					return wrapValueWithFiltersAndCustomizers(
						decomposeResult.getEarlyReturnValue(),
						currentPath,
						currentRawType,
						state
					);
				}
			}

			// thenApply fallback: walks ancestors to find $[type:T] lazy, evaluates it,
			// and navigates the relative path to extract field values.
			if (!isValueSet && !hasChildValues) {
				Object typedValue = resolveThenApplyAncestorValue(node, currentPath, state);
				if (typedValue != null) {
					return traceAndReturnValue(
						typedValue,
						"REGISTER",
						currentPath,
						currentRawType,
						isCurrentTypeContainer,
						parentContext,
						currentType,
						state
					);
				}
			}

			if (node instanceof JvmMapNode) {
				return assembleMapNode((JvmMapNode)node, state, parentContext, parentPath, currentPath, visitedTypes);
			}

			if (node instanceof JvmMapEntryNode) {
				return assembleMapEntryNode(
					(JvmMapEntryNode)node,
					state,
					parentContext,
					parentPath,
					currentPath,
					visitedTypes
				);
			}

			// Container interfaces (List, Map, etc.) skip this — handled by ContainerIntrospector.
			// Primitives also skip (abstract modifier but not truly abstract).
			JvmType nodeType = currentType;
			Class<?> rawType = nodeType.getRawType();
			boolean isInterfaceOrAbstract = !rawType.isPrimitive()
				&& (Modifier.isInterface(rawType.getModifiers()) || Modifier.isAbstract(rawType.getModifiers()));
			boolean needsImplementationSelection =
				isInterfaceOrAbstract || hasCandidateConcretePropertyResolvers(node, state);
			if (needsImplementationSelection && !isCurrentTypeContainer) {
				if (isValueSet) {
					Object setValue = state.candidatesByPath.get(currentPath).value;
					if (state.traceContext.isEnabled()) {
						traceAssemblyStep(
							state,
							currentPath,
							state.candidatesByPath.get(currentPath).sourceLabel(),
							setValue,
							0.0,
							isCurrentTypeContainer,
							parentContext != null ? parentContext.getArbitraryProperty().isContainer() : null,
							currentType.getRawType().getSimpleName()
						);
					}
					return wrapValueWithFiltersAndCustomizers(setValue, currentPath, currentRawType, state);
				}
				CombinableArbitrary<?> interfaceResult = assembleInterfaceNode(
					node,
					state,
					parentContext,
					parentPath,
					currentPath,
					visitedTypes,
					currentPath
				);
				if (interfaceResult != CombinableArbitrary.NOT_GENERATED) {
					return interfaceResult;
				}
				if (state.nodeTreeAdapter != null) {
					JvmNodeTree anonymousTree = state.nodeTreeAdapter.createAnonymousNodeTree(nodeType, options);
					if (anonymousTree != null) {
						registerConcreteTree(anonymousTree, state);
						return assembleNodeDefault(
							anonymousTree.getRootNode(),
							state,
							parentContext,
							parentPath,
							currentPath,
							visitedTypes
						);
					}
				}
				return generateWithDefaultArbitrary(
					node,
					state,
					parentContext,
					parentPath,
					currentPath,
					visitedTypes,
					currentPath
				);
			}

			Property nodeProperty = state.propertyByNode.computeIfAbsent(node, state.nodePropertyFactory);
			writeBackTypeMetadata(node, nodeProperty, state);

			PropertyNameResolver nameResolver = resolveNameResolver(node, nodeProperty, state);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, node.getIndex());

			Property propertyForNullInject = parentPath == null ? new RootProperty(nodeProperty) : nodeProperty;
			ArbitraryProperty ownerProperty = parentContext != null ? parentContext.getArbitraryProperty() : null;
			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				propertyForNullInject,
				node.getIndex(),
				ownerProperty,
				isCurrentTypeContainer,
				nameResolver
			);
			double nullInject = resolveNullInjectGenerator(node, nodeProperty, state).generate(nullInjectContext);

			if (state.notNullPaths.contains(currentPath)) {
				nullInject = 0.0;
			}

			// Suppress null injection for types targeted by register() to ensure registered values take effect
			if (nullInject > 0 && hasMatchingTypeSelector(node.getConcreteType(), state.pathIndex)) {
				nullInject = 0.0;
			}

			if (nullInject > 0 && (isValueSet || hasChildValues)) {
				nullInject = 0.0;
			}

			List<ConcreteTypeDefinition> typeDefinitions = Collections.singletonList(
				new ConcreteTypeDefinition(nodeProperty, Collections.emptyList())
			);

			ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
				objectProperty,
				isCurrentTypeContainer,
				nullInject,
				typeDefinitions
			);

			int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
			Property propertyPathProperty = state.propertyPathPropertyByNode.getOrDefault(node, nodeProperty);
			PropertyPath propertyPath = new PropertyPath(propertyPathProperty, parentPath, depth);

			List<JvmNode> allChildren = deduplicateChildren(getChildrenForNode(node, state));

			List<JvmNode> children;
			if (isCircular) {
				children = new ArrayList<>();
				for (JvmNode child : allChildren) {
					Class<?> childType = child.getConcreteType().getRawType();
					if (!visitedTypes.contains(childType)) {
						children.add(child);
					} else {
						PathExpression childPath = buildChildPath(currentPath, child, node, state);
						if (
							hasChildPathValues(childPath, state.pathIndex)
								|| state.candidatesByPath.containsKey(childPath)
						) {
							children.add(child);
						}
					}
				}
			} else {
				children = allChildren;
			}

			if (isCurrentTypeContainer && state.limitsByPath.containsKey(currentPath)) {
				int sizeLimit = state.limitsByPath.get(currentPath);
				if (children.size() > sizeLimit) {
					children = children.subList(0, sizeLimit);
				}
			}

			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			for (JvmNode childNode : children) {
				PathExpression childPath = buildChildPath(currentPath, childNode, node, state);

				Property childProperty = state.propertyByNode.computeIfAbsent(childNode, state.nodePropertyFactory);
				writeBackTypeMetadata(childNode, childProperty, state);
				PropertyNameResolver childNameResolver = resolveNameResolver(childNode, childProperty, state);

				ObjectProperty childObjectProperty = new ObjectProperty(
					childProperty,
					childNameResolver,
					childNode.getIndex()
				);

				boolean childIsContainer = state.containerTypeCache.computeIfAbsent(childNode.getConcreteType(), type ->
					computeIsContainerType(type, options)
				);

				ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
					childProperty,
					childNode.getIndex(),
					arbitraryProperty, // now we can reference the parent's arbitraryProperty
					childIsContainer,
					childNameResolver
				);
				double childNullInject = resolveNullInjectGenerator(childNode, childProperty, state).generate(
					childNullInjectContext
				);

				if (childNullInject > 0) {
					if (
						state.candidatesByPath.containsKey(childPath)
							|| hasChildPathValues(childPath, state.pathIndex)
							|| state.notNullPaths.contains(childPath)
							|| state.customizersByPath.containsKey(childPath)
					) {
						childNullInject = 0.0;
					}
				}

				List<ConcreteTypeDefinition> childTypeDefinitions = Collections.singletonList(
					new ConcreteTypeDefinition(childProperty, Collections.emptyList())
				);

				ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
					childObjectProperty,
					childIsContainer,
					childNullInject,
					childTypeDefinitions
				);

				childArbitraryProperties.add(childArbitraryProperty);
				nodeByArbitraryProperty.put(childArbitraryProperty, childNode);
				pathByArbitraryProperty.put(childArbitraryProperty, childPath);
			}

			LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> propertyPath);

			ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
				nodeProperty,
				arbitraryProperty,
				childArbitraryProperties,
				parentContext,
				(currentContext, childProp) -> {
					JvmNode childNode = nodeByArbitraryProperty.get(childProp);
					if (childNode == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					PathExpression childPath = pathByArbitraryProperty.get(childProp);
					if (childPath == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
				},
				lazyPropertyPath,
				state.monkeyGeneratorContext,
				options.getGenerateUniqueMaxTries(),
				nullInject,
				state.loggingContext
			);

			Class<?> actualType = Types.getActualType(nodeProperty.getType());
			ArbitraryIntrospector typeSpecificIntrospector = state.introspectorsByType.get(actualType);

			// Do NOT call .injectNull() here — the generator already handles null injection
			// and wraps with TraceableCombinableArbitrary.
			CombinableArbitrary<?> result;
			if (typeSpecificIntrospector != null) {
				ArbitraryIntrospectorResult introspectorResult = typeSpecificIntrospector.introspect(context);
				result = new TraceableCombinableArbitrary<>(
					introspectorResult.getValue().injectNull(nullInject),
					propertyPath
				);
			} else {
				result = options.getDefaultArbitraryGenerator().generate(context);
			}

			result = applyFilters(result, currentPath, currentRawType, state);
			result = applyCustomizers(result, currentPath, state);

			if (state.traceContext.isEnabled()) {
				String introspectorName =
					typeSpecificIntrospector != null ? typeSpecificIntrospector.getClass().getSimpleName() : null;
				CreationMethod nodeCreationMethod = node.getCreationMethod();
				ValueCandidate traceCandidate = isValueSet ? state.candidatesByPath.get(currentPath) : null;
				String assemblySource;
				if (traceCandidate == null) {
					assemblySource = "GENERATED";
				} else if (state.traceContext.isDecomposedPath(currentPath.toExpression())) {
					assemblySource = "DECOMPOSED";
				} else {
					assemblySource = traceCandidate.sourceLabel();
				}
				traceAssemblyStep(
					state,
					currentPath,
					assemblySource,
					traceCandidate != null ? traceCandidate.value : null,
					nullInject,
					isCurrentTypeContainer,
					parentContext != null ? parentContext.getArbitraryProperty().isContainer() : null,
					currentType.getRawType().getSimpleName(),
					formatCreationMethodType(nodeCreationMethod),
					formatCreationDetail(nodeCreationMethod),
					introspectorName,
					currentType.getRawType().getSimpleName(),
					null // actualType
				);
			}

			return result;
		} finally {
			if (addedToVisited) {
				visitedTypes.remove(currentRawType);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean hasCandidateConcretePropertyResolvers(JvmNode node, AssemblyState state) {
		if (state.typeMetadataCache != null) {
			CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
			if (cached != null) {
				return cached.hasCandidateConcretePropertyResolvers;
			}
		}
		Property property = JvmNodePropertyFactory.fromType(node.getConcreteType());
		return state.options.getCandidateConcretePropertyResolver(property) != null;
	}

	private CombinableArbitrary<?> assembleInterfaceNode(
		JvmNode node,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes,
		PathExpression normalizedPath
	) {
		FixtureMonkeyOptions options = state.options;

		Property interfaceProperty = state.nodePropertyFactory.apply(node);

		@SuppressWarnings("deprecation")
		CandidateConcretePropertyResolver resolver = options.getCandidateConcretePropertyResolver(interfaceProperty);
		List<Property> candidates = resolver != null ? resolver.resolve(interfaceProperty) : Collections.emptyList();

		if (candidates == null || candidates.isEmpty()) {
			return CombinableArbitrary.NOT_GENERATED;
		}

		InterfaceSelectionStrategy strategy = state.interfaceSelectionStrategy;
		long seed = state.assemblySeed;
		int sampleIndex = state.interfaceSelectionCounter.getAndIncrement();

		int selectedIndex = strategy.selectIndex(candidates.size(), seed, sampleIndex);
		Property selectedProperty = candidates.get(selectedIndex);

		JvmType concreteType = Types.toJvmType(selectedProperty.getAnnotatedType(), selectedProperty.getAnnotations());

		// Skip concrete tree for self-recursive types to avoid infinite recursion
		boolean isSelfRecursive = visitedTypes.contains(concreteType.getRawType());
		JvmNodeTree concreteTree =
			state.nodeTreeAdapter != null && !isSelfRecursive
				? state.nodeTreeAdapter.createConcreteNodeTree(concreteType, options)
				: null;

		CombinableArbitrary<?> result;
		if (concreteTree != null) {
			JvmNode concreteRootNode = concreteTree.getRootNode();

			registerConcreteTree(concreteTree, state);
			state.propertyByNode.put(concreteRootNode, selectedProperty);

			result = assembleNode(concreteRootNode, state, parentContext, parentPath, currentPath, visitedTypes);
		} else {
			result = generateWithConcreteProperty(
				selectedProperty,
				node,
				state,
				parentContext,
				parentPath,
				currentPath,
				visitedTypes,
				normalizedPath
			);
		}

		// Preserve combined()/rawValue() delegation — critical for Jackson serialization semantics
		@SuppressWarnings("unchecked")
		CombinableArbitrary<Object> typedResult = (CombinableArbitrary<Object>)result;
		return new CombinableArbitrary<Object>() {
			@Override
			public Object combined() {
				return typedResult.combined();
			}

			@Override
			public Object rawValue() {
				return typedResult.rawValue();
			}

			@Override
			public void clear() {
				typedResult.clear();
			}

			@Override
			public boolean fixed() {
				return typedResult.fixed();
			}
		};
	}

	private CombinableArbitrary<?> generateWithDefaultArbitrary(
		JvmNode node,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes,
		PathExpression normalizedPath
	) {
		Property nodeProperty = state.nodePropertyFactory.apply(node);
		return generateWithConcreteProperty(
			nodeProperty,
			node,
			state,
			parentContext,
			parentPath,
			currentPath,
			visitedTypes,
			normalizedPath
		);
	}

	private CombinableArbitrary<?> generateWithConcreteProperty(
		Property concreteProperty,
		JvmNode originalNode,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes,
		PathExpression normalizedPath
	) {
		FixtureMonkeyOptions options = state.options;
		PropertyNameResolver nameResolver = options.getPropertyNameResolver(concreteProperty);

		ObjectProperty objectProperty = new ObjectProperty(concreteProperty, nameResolver, originalNode.getIndex());

		Class<?> actualType = Types.getActualType(concreteProperty.getType());
		ArbitraryIntrospector typeSpecificIntrospector = state.introspectorsByType.get(actualType);

		PropertyGenerator propertyGenerator = options
			.getDefaultArbitraryGenerator()
			.getRequiredPropertyGenerator(concreteProperty);

		List<Property> childProperties =
			propertyGenerator != null
				? propertyGenerator.generateChildProperties(concreteProperty)
				: Collections.emptyList();

		if (childProperties.isEmpty()) {
			PropertyGenerator optionsPropertyGenerator = options.getOptionalPropertyGenerator(concreteProperty);
			if (optionsPropertyGenerator != null) {
				childProperties = optionsPropertyGenerator.generateChildProperties(concreteProperty);
			}
		}

		JvmType originalType = originalNode.getConcreteType();
		boolean isContainer = state.containerTypeCache.computeIfAbsent(originalType, type ->
			computeIsContainerType(type, options)
		);

		Property propertyForNullInject = parentPath == null ? new RootProperty(concreteProperty) : concreteProperty;
		ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
			propertyForNullInject,
			originalNode.getIndex(),
			null,
			isContainer,
			nameResolver
		);
		double nullInject = options.getNullInjectGenerator(concreteProperty).generate(nullInjectContext);

		if (state.notNullPaths.contains(normalizedPath)) {
			nullInject = 0.0;
		}

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
			objectProperty,
			isContainer,
			nullInject,
			Collections.singletonList(new ConcreteTypeDefinition(concreteProperty, childProperties))
		);

		int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
		PropertyPath propertyPath = new PropertyPath(concreteProperty, parentPath, depth);

		boolean addedToVisited = false;
		if (visitedTypes.add(actualType)) {
			addedToVisited = true;
		}

		JvmType concreteJvmType = Types.toJvmType(
			concreteProperty.getAnnotatedType(),
			concreteProperty.getAnnotations()
		);
		Map<String, JvmNode> concreteChildrenByName = buildConcreteChildrenMap(concreteJvmType, state);

		try {
			if (!addedToVisited) {
				List<Property> filteredChildren = new ArrayList<>();
				for (Property childProp : childProperties) {
					Class<?> childRawType = Types.getActualType(childProp.getType());
					boolean isRecursiveChild =
						visitedTypes.contains(childRawType) || childRawType.isAssignableFrom(actualType);
					if (!isRecursiveChild) {
						filteredChildren.add(childProp);
					} else {
						PropertyNameResolver childNr = options.getPropertyNameResolver(childProp);
						String childName = childNr.resolve(childProp);
						PathExpression childPath = currentPath.child(childName);
						if (
							hasChildPathValues(childPath, state.pathIndex)
								|| state.candidatesByPath.containsKey(childPath)
						) {
							filteredChildren.add(childProp);
						}
					}
				}
				childProperties = filteredChildren;
			}

			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, Property> propertyByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			for (Property childProperty : childProperties) {
				PropertyNameResolver childNameResolver = options.getPropertyNameResolver(childProperty);

				ObjectProperty childObjectProperty = new ObjectProperty(
					childProperty,
					childNameResolver,
					0
				);

				JvmType childJvmType = Types.toJvmType(
					childProperty.getAnnotatedType(),
					childProperty.getAnnotations()
				);
				boolean childIsContainer = state.containerTypeCache.computeIfAbsent(childJvmType, type ->
					computeIsContainerType(type, options)
				);

				ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
					childProperty,
					0,
					arbitraryProperty,
					childIsContainer,
					childNameResolver
				);
				double childNullInject = options.getNullInjectGenerator(childProperty).generate(childNullInjectContext);

				List<Property> grandChildProperties =
					propertyGenerator != null
						? propertyGenerator.generateChildProperties(childProperty)
						: Collections.emptyList();

				ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
					childObjectProperty,
					childIsContainer,
					childNullInject,
					Collections.singletonList(new ConcreteTypeDefinition(childProperty, grandChildProperties))
				);

				childArbitraryProperties.add(childArbitraryProperty);
				propertyByArbitraryProperty.put(childArbitraryProperty, childProperty);

				String childName = childNameResolver.resolve(childProperty);
				PathExpression childPath = currentPath.child(childName);
				pathByArbitraryProperty.put(childArbitraryProperty, childPath);
			}

			LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> propertyPath);

			ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
				concreteProperty,
				arbitraryProperty,
				childArbitraryProperties,
				parentContext,
				(currentContext, childProp) -> {
					Property childProperty = propertyByArbitraryProperty.get(childProp);
					if (childProperty == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					PathExpression childPath = pathByArbitraryProperty.get(childProp);
					if (childPath == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}

					String childName = childProperty.getName();
					JvmNode childNode = childName != null ? concreteChildrenByName.get(childName) : null;
					if (childNode == null) {
						JvmType childType = Types.toJvmType(
							childProperty.getAnnotatedType(),
							childProperty.getAnnotations()
						);
						childNode = new JavaNode(childType, childName != null ? childName : "");
					}

					return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
				},
				lazyPropertyPath,
				state.monkeyGeneratorContext,
				options.getGenerateUniqueMaxTries(),
				nullInject,
				state.loggingContext
			);

			CombinableArbitrary<?> result;
			if (typeSpecificIntrospector != null) {
				ArbitraryIntrospectorResult introspectorResult = typeSpecificIntrospector.introspect(context);
				result = introspectorResult.getValue();
			} else {
				result = options.getDefaultArbitraryGenerator().generate(context);
			}

			Class<?> concreteRawType = Types.getActualType(concreteProperty.getType());
			if (!concreteRawType.isPrimitive()) {
				result = result.injectNull(nullInject);
			}

			CombinableArbitrary<?> filtered = applyFilters(result, normalizedPath, concreteRawType, state);
			return applyCustomizers(filtered, normalizedPath, state);
		} finally {
			if (addedToVisited) {
				visitedTypes.remove(actualType);
			}
		}
	}

	private CombinableArbitrary<?> assembleMapNode(
		JvmMapNode mapNode,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		return assembleMapLikeNode(
			mapNode,
			mapNode.getKeyNode(),
			mapNode.getValueNode(),
			state,
			parentContext,
			parentPath,
			currentPath,
			visitedTypes
		);
	}

	private CombinableArbitrary<?> assembleMapEntryNode(
		JvmMapEntryNode mapEntryNode,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		FixtureMonkeyOptions options = state.options;

		JvmType nodeType = mapEntryNode.getConcreteType();
		boolean isNodeContainer = state.containerTypeCache.computeIfAbsent(nodeType, type ->
			computeIsContainerType(type, options)
		);

		Class<?> nodeRawType = nodeType.getRawType();
		boolean addedToVisited = false;
		if (!isNodeContainer && visitedTypes.add(nodeRawType)) {
			addedToVisited = true;
		}

		try {
			List<JvmNode> treeChildren = state.nodeTree.getChildren(mapEntryNode);
			JvmNode keyNode = !treeChildren.isEmpty() ? treeChildren.get(0) : mapEntryNode.getKeyNode();
			JvmNode valueNode = treeChildren.size() > 1 ? treeChildren.get(1) : mapEntryNode.getValueNode();

			Property keyProperty = state.propertyByNode.computeIfAbsent(keyNode, state.nodePropertyFactory);
			Property valueProperty = state.propertyByNode.computeIfAbsent(valueNode, state.nodePropertyFactory);

			Property nodeProperty = state.propertyByNode.computeIfAbsent(mapEntryNode, state.nodePropertyFactory);
			PropertyNameResolver nameResolver = resolveNameResolver(mapEntryNode, nodeProperty, state);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, mapEntryNode.getIndex());

			boolean isContainer = true;

			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				nodeProperty,
				mapEntryNode.getIndex(),
				null,
				isContainer,
				nameResolver
			);
			double nullInject = resolveNullInjectGenerator(mapEntryNode, nodeProperty, state).generate(
				nullInjectContext
			);

			MapEntryElementProperty mapEntryElementProperty = new MapEntryElementProperty(
				nodeProperty,
				keyProperty,
				valueProperty
			);

			List<Property> childProperties = Collections.singletonList(mapEntryElementProperty);
			List<ConcreteTypeDefinition> typeDefinitions = Collections.singletonList(
				new ConcreteTypeDefinition(nodeProperty, childProperties)
			);

			ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
				objectProperty,
				isContainer,
				nullInject,
				typeDefinitions
			);

			int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
			PropertyPath propertyPath = new PropertyPath(nodeProperty, parentPath, depth);

			PropertyNameResolver childNameResolver = options.getPropertyNameResolver(mapEntryElementProperty);
			ObjectProperty childObjectProperty = new ObjectProperty(mapEntryElementProperty, childNameResolver, null);

			ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
				mapEntryElementProperty,
				null,
				arbitraryProperty,
				true,
				childNameResolver
			);
			double childNullInject = options
				.getNullInjectGenerator(mapEntryElementProperty)
				.generate(childNullInjectContext);

			List<Property> grandChildProperties = java.util.Arrays.asList(keyProperty, valueProperty);
			List<ConcreteTypeDefinition> childTypeDefinitions = Collections.singletonList(
				new ConcreteTypeDefinition(mapEntryElementProperty, grandChildProperties)
			);

			ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
				childObjectProperty,
				true,
				childNullInject,
				childTypeDefinitions
			);

			List<ArbitraryProperty> childArbitraryProperties = Collections.singletonList(childArbitraryProperty);

			return buildContextAndGenerate(
				nodeProperty,
				arbitraryProperty,
				childArbitraryProperties,
				parentContext,
				(currentContext, childProp) -> {
					return assembleMapEntryElement(
						mapEntryElementProperty,
						keyNode,
						valueNode,
						state,
						currentContext,
						propertyPath,
						currentPath,
						visitedTypes
					);
				},
				propertyPath,
				nullInject,
				state
			);
		} finally {
			if (addedToVisited) {
				visitedTypes.remove(nodeRawType);
			}
		}
	}

	@SuppressWarnings("unboxing.of.nullable")
	private CombinableArbitrary<?> assembleMapLikeNode(
		JvmNode mapLikeNode,
		JvmNode fallbackKeyNode,
		JvmNode fallbackValueNode,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		FixtureMonkeyOptions options = state.options;

		JvmType nodeType = mapLikeNode.getConcreteType();
		boolean isNodeContainer = state.containerTypeCache.computeIfAbsent(nodeType, type ->
			computeIsContainerType(type, options)
		);

		Class<?> rawType = nodeType.getRawType();
		boolean addedToVisited = false;
		if (!isNodeContainer && visitedTypes.add(rawType)) {
			addedToVisited = true;
		}

		try {
			List<JvmNode> treeChildren = state.nodeTree.getChildren(mapLikeNode);
			JvmNode keyNode = treeChildren.size() > 0 ? treeChildren.get(0) : fallbackKeyNode;
			JvmNode valueNode = treeChildren.size() > 1 ? treeChildren.get(1) : fallbackValueNode;

			Property keyProperty = state.propertyByNode.computeIfAbsent(keyNode, state.nodePropertyFactory);
			Property valueProperty = state.propertyByNode.computeIfAbsent(valueNode, state.nodePropertyFactory);

			Property mapEntryProperty = state.propertyByNode.computeIfAbsent(mapLikeNode, state.nodePropertyFactory);

			int entrySequence = mapLikeNode.getIndex() != null ? mapLikeNode.getIndex() : 0;
			state.propertyPathPropertyByNode.put(
				keyNode,
				new MapKeyElementProperty(mapEntryProperty, keyProperty, entrySequence)
			);
			state.propertyPathPropertyByNode.put(
				valueNode,
				new MapValueElementProperty(mapEntryProperty, valueProperty, entrySequence)
			);

			MapEntryElementProperty nodeProperty = new MapEntryElementProperty(
				mapEntryProperty,
				keyProperty,
				valueProperty
			);

			PropertyNameResolver nameResolver = options.getPropertyNameResolver(nodeProperty);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, mapLikeNode.getIndex());

			boolean isContainer = true;

			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				nodeProperty,
				mapLikeNode.getIndex(),
				null,
				isContainer,
				nameResolver
			);
			double nullInject = options.getNullInjectGenerator(nodeProperty).generate(nullInjectContext);

			List<Property> childProperties = java.util.Arrays.asList(keyProperty, valueProperty);
			List<ConcreteTypeDefinition> typeDefinitions = Collections.singletonList(
				new ConcreteTypeDefinition(nodeProperty, childProperties)
			);

			ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
				objectProperty,
				isContainer,
				nullInject,
				typeDefinitions
			);

			int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
			PropertyPath propertyPath = new PropertyPath(nodeProperty, parentPath, depth);

			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			ArbitraryProperty keyArbitraryProperty = buildChildArbitraryPropertyCached(
				keyNode,
				keyProperty,
				arbitraryProperty,
				state
			);
			keyArbitraryProperty = new ArbitraryProperty(
				keyArbitraryProperty.getObjectProperty(),
				keyArbitraryProperty.isContainer(),
				0.0,
				keyArbitraryProperty.getConcreteTypeDefinitions()
			);
			childArbitraryProperties.add(keyArbitraryProperty);
			nodeByArbitraryProperty.put(keyArbitraryProperty, keyNode);
			pathByArbitraryProperty.put(keyArbitraryProperty, currentPath.key());

			ArbitraryProperty valueArbitraryProperty = buildChildArbitraryPropertyCached(
				valueNode,
				valueProperty,
				arbitraryProperty,
				state
			);
			childArbitraryProperties.add(valueArbitraryProperty);
			nodeByArbitraryProperty.put(valueArbitraryProperty, valueNode);
			pathByArbitraryProperty.put(valueArbitraryProperty, currentPath.value());

			CombinableArbitrary<?> result = buildContextAndGenerate(
				nodeProperty,
				arbitraryProperty,
				childArbitraryProperties,
				parentContext,
				(currentContext, childProp) -> {
					JvmNode childNode = nodeByArbitraryProperty.get(childProp);
					if (childNode == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					PathExpression childPath = pathByArbitraryProperty.get(childProp);
					if (childPath == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
				},
				propertyPath,
				nullInject,
				state
			);

			if (state.traceContext.isEnabled()) {
				traceAssemblyStep(
					state,
					currentPath,
					"GENERATED",
					null,
					nullInject,
					true, // isContainer - map entry is a container (contains key and value)
					parentContext != null ? parentContext.getArbitraryProperty().isContainer() : null,
					"MapEntry"
				);
			}

			return result;
		} finally {
			if (addedToVisited) {
				visitedTypes.remove(rawType);
			}
		}
	}

	private CombinableArbitrary<?> assembleMapEntryElement(
		MapEntryElementProperty mapEntryElementProperty,
		JvmNode keyNode,
		JvmNode valueNode,
		AssemblyState state,
		ArbitraryGeneratorContext parentContext,
		PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		FixtureMonkeyOptions options = state.options;

		Property keyProperty = state.propertyByNode.computeIfAbsent(keyNode, state.nodePropertyFactory);
		Property valueProperty = state.propertyByNode.computeIfAbsent(valueNode, state.nodePropertyFactory);

		Property mapProperty = mapEntryElementProperty.getMapEntryProperty();
		state.propertyPathPropertyByNode.put(keyNode, new MapKeyElementProperty(mapProperty, keyProperty, 0));
		state.propertyPathPropertyByNode.put(valueNode, new MapValueElementProperty(mapProperty, valueProperty, 0));

		PropertyNameResolver nameResolver = options.getPropertyNameResolver(mapEntryElementProperty);

		ObjectProperty objectProperty = new ObjectProperty(mapEntryElementProperty, nameResolver, null);

		boolean isContainer = true;

		ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
			mapEntryElementProperty,
			null,
			null,
			isContainer,
			nameResolver
		);
		double nullInject = options.getNullInjectGenerator(mapEntryElementProperty).generate(nullInjectContext);

		List<Property> childProperties = java.util.Arrays.asList(keyProperty, valueProperty);
		List<ConcreteTypeDefinition> typeDefinitions = Collections.singletonList(
			new ConcreteTypeDefinition(mapEntryElementProperty, childProperties)
		);

		ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
			objectProperty,
			isContainer,
			nullInject,
			typeDefinitions
		);

		int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
		PropertyPath propertyPath = new PropertyPath(mapEntryElementProperty, parentPath, depth);

		List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
		Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
		Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

		ArbitraryProperty keyArbitraryProperty = buildChildArbitraryPropertyCached(
			keyNode,
			keyProperty,
			arbitraryProperty,
			state
		);
		keyArbitraryProperty = new ArbitraryProperty(
			keyArbitraryProperty.getObjectProperty(),
			keyArbitraryProperty.isContainer(),
			0.0, // Force nullInject to 0 for map keys
			keyArbitraryProperty.getConcreteTypeDefinitions()
		);
		childArbitraryProperties.add(keyArbitraryProperty);
		nodeByArbitraryProperty.put(keyArbitraryProperty, keyNode);
		pathByArbitraryProperty.put(keyArbitraryProperty, currentPath.key());

		ArbitraryProperty valueArbitraryProperty = buildChildArbitraryPropertyCached(
			valueNode,
			valueProperty,
			arbitraryProperty,
			state
		);
		childArbitraryProperties.add(valueArbitraryProperty);
		nodeByArbitraryProperty.put(valueArbitraryProperty, valueNode);
		pathByArbitraryProperty.put(valueArbitraryProperty, currentPath.value());

		return buildContextAndGenerate(
			mapEntryElementProperty,
			arbitraryProperty,
			childArbitraryProperties,
			parentContext,
			(currentContext, childProp) -> {
				JvmNode childNode = nodeByArbitraryProperty.get(childProp);
				if (childNode == null) {
					return CombinableArbitrary.NOT_GENERATED;
				}
				PathExpression childPath = pathByArbitraryProperty.get(childProp);
				if (childPath == null) {
					return CombinableArbitrary.NOT_GENERATED;
				}
				return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
			},
			propertyPath,
			nullInject,
			state
		);
	}

	private CombinableArbitrary<?> assembleNodeDefault(
		JvmNode node,
		AssemblyState state,
		@Nullable ArbitraryGeneratorContext parentContext,
		@Nullable PropertyPath parentPath,
		PathExpression currentPath,
		Set<Class<?>> visitedTypes
	) {
		FixtureMonkeyOptions options = state.options;

		JvmType currentType = node.getConcreteType();
		boolean isContainer = state.containerTypeCache.computeIfAbsent(currentType, type ->
			computeIsContainerType(type, options)
		);

		Class<?> currentRawType = currentType.getRawType();
		boolean addedToVisited = false;
		if (!isContainer && visitedTypes.add(currentRawType)) {
			addedToVisited = true;
		}

		try {
			Property nodeProperty = state.propertyByNode.computeIfAbsent(node, state.nodePropertyFactory);
			writeBackTypeMetadata(node, nodeProperty, state);
			PropertyNameResolver nameResolver = resolveNameResolver(node, nodeProperty, state);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, node.getIndex());

			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				nodeProperty,
				node.getIndex(),
				null,
				isContainer,
				nameResolver
			);
			double nullInject = resolveNullInjectGenerator(node, nodeProperty, state).generate(nullInjectContext);

			List<ConcreteTypeDefinition> typeDefinitions = Collections.singletonList(
				new ConcreteTypeDefinition(nodeProperty, Collections.emptyList())
			);

			ArbitraryProperty arbitraryProperty = new ArbitraryProperty(
				objectProperty,
				isContainer,
				nullInject,
				typeDefinitions
			);

			int depth = parentPath == null ? 0 : parentPath.getDepth() + 1;
			Property propertyPathProperty = state.propertyPathPropertyByNode.getOrDefault(node, nodeProperty);
			PropertyPath propertyPath = new PropertyPath(propertyPathProperty, parentPath, depth);

			List<JvmNode> children = deduplicateChildren(getChildrenForNode(node, state));
			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			for (JvmNode childNode : children) {
				PathExpression childPath = buildChildPath(currentPath, childNode, node, state);

				Property childProperty = state.propertyByNode.computeIfAbsent(childNode, state.nodePropertyFactory);
				writeBackTypeMetadata(childNode, childProperty, state);
				PropertyNameResolver childNameResolver = resolveNameResolver(childNode, childProperty, state);

				ObjectProperty childObjectProperty = new ObjectProperty(
					childProperty,
					childNameResolver,
					childNode.getIndex()
				);

				boolean childIsContainer = state.containerTypeCache.computeIfAbsent(childNode.getConcreteType(), type ->
					computeIsContainerType(type, options)
				);

				ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
					childProperty,
					childNode.getIndex(),
					arbitraryProperty,
					childIsContainer,
					childNameResolver
				);
				double childNullInject = resolveNullInjectGenerator(childNode, childProperty, state).generate(
					childNullInjectContext
				);

				List<ConcreteTypeDefinition> childTypeDefinitions = Collections.singletonList(
					new ConcreteTypeDefinition(childProperty, Collections.emptyList())
				);

				ArbitraryProperty childArbitraryProperty = new ArbitraryProperty(
					childObjectProperty,
					childIsContainer,
					childNullInject,
					childTypeDefinitions
				);

				childArbitraryProperties.add(childArbitraryProperty);
				nodeByArbitraryProperty.put(childArbitraryProperty, childNode);
				pathByArbitraryProperty.put(childArbitraryProperty, childPath);
			}

			return buildContextAndGenerate(
				nodeProperty,
				arbitraryProperty,
				childArbitraryProperties,
				parentContext,
				(currentContext, childProp) -> {
					JvmNode childNode = nodeByArbitraryProperty.get(childProp);
					if (childNode == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					PathExpression childPath = pathByArbitraryProperty.get(childProp);
					if (childPath == null) {
						return CombinableArbitrary.NOT_GENERATED;
					}
					return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
				},
				propertyPath,
				nullInject,
				state
			);
		} finally {
			if (addedToVisited) {
				visitedTypes.remove(currentRawType);
			}
		}
	}

	private Map<String, JvmNode> buildConcreteChildrenMap(JvmType concreteType, AssemblyState state) {
		if (state.nodeTreeAdapter == null || state.options == null) {
			return Collections.emptyMap();
		}

		JvmNodeTree concreteTree = state.nodeTreeAdapter.createConcreteNodeTree(concreteType, state.options);
		if (concreteTree == null) {
			return Collections.emptyMap();
		}

		JvmNode rootNode = concreteTree.getRootNode();
		List<JvmNode> children = concreteTree.getChildren(rootNode);
		Map<String, JvmNode> childrenByName = new HashMap<>();
		for (JvmNode child : children) {
			String nodeName = child.getNodeName();
			if (nodeName != null) {
				childrenByName.put(nodeName, child);
			}
		}
		return childrenByName;
	}

	private List<JvmNode> getChildrenForNode(JvmNode node, AssemblyState state) {
		List<JvmNode> children = state.nodeTree.getChildren(node);
		if (!children.isEmpty()) {
			return children;
		}

		JvmNodeTree concreteTree = state.concreteTreeByNode.get(node);
		if (concreteTree != null) {
			return concreteTree.getChildren(node);
		}

		return Collections.emptyList();
	}

	private void registerConcreteTree(JvmNodeTree concreteTree, AssemblyState state) {
		for (JvmNode treeNode : concreteTree.getAllNodes()) {
			state.concreteTreeByNode.put(treeNode, concreteTree);
		}
	}

	private CombinableArbitrary<?> buildContextAndGenerate(
		Property property,
		ArbitraryProperty arbitraryProperty,
		List<ArbitraryProperty> childArbitraryProperties,
		@Nullable ArbitraryGeneratorContext parentContext,
		BiFunction<ArbitraryGeneratorContext, ArbitraryProperty, CombinableArbitrary<?>> childResolver,
		PropertyPath propertyPath,
		double nullInject,
		AssemblyState state
	) {
		LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> propertyPath);

		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			property,
			arbitraryProperty,
			childArbitraryProperties,
			parentContext,
			childResolver,
			lazyPropertyPath,
			state.monkeyGeneratorContext,
			state.options.getGenerateUniqueMaxTries(),
			nullInject,
			state.loggingContext
		);

		return state.options.getDefaultArbitraryGenerator().generate(context).injectNull(nullInject);
	}

	private ArbitraryProperty buildChildArbitraryPropertyCached(
		JvmNode childNode,
		Property childProperty,
		ArbitraryProperty parentArbitraryProperty,
		AssemblyState state
	) {
		FixtureMonkeyOptions options = state.options;
		PropertyNameResolver childNameResolver = resolveNameResolver(childNode, childProperty, state);

		ObjectProperty childObjectProperty = new ObjectProperty(childProperty, childNameResolver, childNode.getIndex());

		boolean childIsContainer = state.containerTypeCache.computeIfAbsent(childNode.getConcreteType(), type ->
			computeIsContainerType(type, options)
		);

		ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
			childProperty,
			childNode.getIndex(),
			parentArbitraryProperty,
			childIsContainer,
			childNameResolver
		);
		double childNullInject = resolveNullInjectGenerator(childNode, childProperty, state).generate(
			childNullInjectContext
		);

		List<ConcreteTypeDefinition> childTypeDefinitions = Collections.singletonList(
			new ConcreteTypeDefinition(childProperty, Collections.emptyList())
		);

		return new ArbitraryProperty(childObjectProperty, childIsContainer, childNullInject, childTypeDefinitions);
	}

	private PathExpression buildChildPath(
		PathExpression parentPath,
		JvmNode childNode,
		JvmNode parentNode,
		AssemblyState state
	) {
		if (isSingleElementWrapper(parentNode.getConcreteType())) {
			String nodeName = childNode.getNodeName();
			Integer index = childNode.getIndex();

			if (nodeName == null && index == null) {
				return parentPath;
			}
		}

		String nodeName = childNode.getNodeName();
		Integer index = childNode.getIndex();

		if (index != null) {
			return parentPath.index(index);
		} else if (nodeName != null) {
			Property childProperty = state.propertyByNode.computeIfAbsent(childNode, state.nodePropertyFactory);
			PropertyNameResolver nameResolver = resolveNameResolver(childNode, childProperty, state);
			String resolvedName = nameResolver.resolve(childProperty);

			return parentPath.child(resolvedName);
		} else {
			return parentPath;
		}
	}

	private boolean isSingleElementWrapper(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		return (Supplier.class.isAssignableFrom(rawType)
			|| Optional.class.isAssignableFrom(rawType)
			|| OptionalInt.class.isAssignableFrom(rawType)
			|| OptionalLong.class.isAssignableFrom(rawType)
			|| OptionalDouble.class.isAssignableFrom(rawType));
	}

	private boolean computeIsContainerType(JvmType jvmType, @Nullable FixtureMonkeyOptions options) {
		Class<?> rawType = jvmType.getRawType();
		if (
			rawType.isArray()
				|| Collection.class.isAssignableFrom(rawType)
				|| Map.class.isAssignableFrom(rawType)
				|| Map.Entry.class.isAssignableFrom(rawType)
				|| Iterable.class.isAssignableFrom(rawType)
				|| BaseStream.class.isAssignableFrom(rawType)
				|| isSingleElementWrapper(jvmType)
		) {
			return true;
		}

		if (options != null) {
			Property property = JvmNodePropertyFactory.fromType(jvmType);
			for (MatcherOperator<ContainerPropertyGenerator> op : options.getContainerPropertyGenerators()) {
				if (op.getMatcher().match(property)) {
					return true;
				}
			}
		}

		return false;
	}

	private PropertyNameResolver resolveNameResolver(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache != null) {
			CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
			if (cached != null) {
				return cached.nameResolver;
			}
		}
		return state.options.getPropertyNameResolver(property);
	}

	private NullInjectGenerator resolveNullInjectGenerator(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache != null) {
			CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
			if (cached != null) {
				return cached.nullInjectGenerator;
			}
		}
		return state.options.getNullInjectGenerator(property);
	}

	@SuppressWarnings("deprecation")
	private void writeBackTypeMetadata(JvmNode node, Property property, AssemblyState state) {
		if (state.typeMetadataCache == null) {
			return;
		}
		JvmType jvmType = node.getConcreteType();
		if (!state.typeMetadataCache.containsKey(jvmType)) {
			PropertyNameResolver resolver = state.options.getPropertyNameResolver(property);
			NullInjectGenerator generator = state.options.getNullInjectGenerator(property);
			boolean isContainer = computeIsContainerType(jvmType, state.options);
			boolean hasCandidateResolvers = state.options.getCandidateConcretePropertyResolver(property) != null;
			state.typeMetadataCache.putIfAbsent(
				jvmType,
				new CachedTypeMetadata(resolver, generator, isContainer, hasCandidateResolvers)
			);
		}
	}

	private boolean hasChildPathValues(PathExpression parentPath, PathIndex pathIndex) {
		return pathIndex.hasChildPaths(parentPath);
	}

	private static boolean hasChildCandidateValues(
		PathExpression parentPath,
		Map<PathExpression, ValueCandidate> candidatesByPath
	) {
		String parentStr = parentPath.toExpression();
		for (PathExpression candidatePath : candidatesByPath.keySet()) {
			String candidateStr = candidatePath.toExpression();
			if (candidateStr.length() > parentStr.length() && candidateStr.startsWith(parentStr)) {
				char nextChar = candidateStr.charAt(parentStr.length());
				if (nextChar == '.' || nextChar == '[') {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isNullSetAfterAllChildren(
		ValueOrder nullOrder,
		PathExpression parentPath,
		Map<PathExpression, ValueCandidate> candidatesByPath
	) {
		String parentStr = parentPath.toExpression();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			String candidateStr = entry.getKey().toExpression();
			if (candidateStr.length() > parentStr.length() && candidateStr.startsWith(parentStr)) {
				char nextChar = candidateStr.charAt(parentStr.length());
				if (nextChar == '.' || nextChar == '[') {
					if (entry.getValue().order.compareTo(nullOrder) >= 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private @Nullable PathExpression findBestMatchingPath(
		Map<PathExpression, ValueCandidate> candidatesByPath,
		PathExpression path,
		@Nullable AssemblyState state
	) {
		PathExpression bestPath = null;
		// Sentinel initial values — RegisterOrder.of(MIN_VALUE) loses to any real comparison
		ValueOrder bestOrder = ValueOrder.RegisterOrder.of(Integer.MIN_VALUE);

		PathExpression bestTypePath = null;
		int bestTypeDepth = -1;
		ValueOrder bestTypeOrder = ValueOrder.RegisterOrder.of(Integer.MIN_VALUE);

		ValueCandidate exactCandidate = candidatesByPath.get(path);
		if (exactCandidate != null) {
			if (exactCandidate.order.compareTo(bestOrder) > 0) {
				bestOrder = exactCandidate.order;
				bestPath = path;
			}
		}

		if (state != null) {
			for (Map.Entry<PathExpression, ValueCandidate> entry : state.wildcardEntries) {
				PathExpression pattern = entry.getKey();
				if (pattern.matches(path)) {
					ValueOrder order = entry.getValue().order;
					if (order.compareTo(bestOrder) > 0) {
						bestOrder = order;
						bestPath = pattern;
					}
				}
			}

			for (Map.Entry<PathExpression, ValueCandidate> entry : state.typeSelectorEntries) {
				PathExpression pattern = entry.getKey();
				// depth = how deep in the tree the type match occurs; deeper = more specific = wins
				int depth = matchesTypePattern(pattern, path, state);
				if (depth >= 0) {
					ValueOrder order = entry.getValue().order;
					if (depth > bestTypeDepth || (depth == bestTypeDepth && order.compareTo(bestTypeOrder) > 0)) {
						bestTypeDepth = depth;
						bestTypeOrder = order;
						bestTypePath = pattern;
					}
				}
			}
		}

		if (bestTypePath != null) {
			if (bestPath == null || bestTypeOrder.compareTo(bestOrder) > 0) {
				return bestTypePath;
			}
		}

		return bestPath;
	}

	private int matchesTypePattern(PathExpression pattern, PathExpression path, AssemblyState state) {
		List<Segment> patternSegments = pattern.getSegments();
		List<Segment> pathSegments = path.getSegments();

		if (patternSegments.isEmpty()) {
			return -1;
		}

		int typeSegmentIndex = -1;
		TypeSelector typeSelector = null;
		for (int i = 0; i < patternSegments.size(); i++) {
			Segment seg = patternSegments.get(i);
			if (seg.isSingleSelector() && seg.getFirstSelector() instanceof TypeSelector) {
				typeSegmentIndex = i;
				typeSelector = (TypeSelector)seg.getFirstSelector();
				break;
			}
		}

		if (typeSelector == null) {
			return -1;
		}

		int patternSuffixLen = patternSegments.size() - typeSegmentIndex - 1;

		// TypeSelector-only pattern ($[type:T]) — only matches current node's type directly.
		// Descendant field matching is handled by resolveThenApplyAncestorValue.
		if (patternSuffixLen == 0 && typeSegmentIndex == 0) {
			JvmNode currentNode = findNodeForPath(path, state);
			if (currentNode != null && typeSelector.matchesType(currentNode.getConcreteType().getRawType())) {
				return pathSegments.size();
			}
			return -1;
		}

		if (patternSuffixLen > pathSegments.size()) {
			return -1;
		}

		int suffixStartInPath = pathSegments.size() - patternSuffixLen;
		for (int i = 0; i < patternSuffixLen; i++) {
			Segment patternSeg = patternSegments.get(typeSegmentIndex + 1 + i);
			Segment pathSeg = pathSegments.get(suffixStartInPath + i);
			if (!patternSeg.equals(pathSeg)
				&& !(patternSeg.isSingleSelector()
				&& patternSeg.getFirstSelector() instanceof WildcardSelector
				&& pathSeg.isSingleSelector()
				&& (pathSeg.getFirstSelector() instanceof IndexSelector
				|| pathSeg.getFirstSelector() instanceof KeySelector
				|| pathSeg.getFirstSelector() instanceof ValueSelector))
			) {
				return -1;
			}
		}

		int ownerPos = suffixStartInPath - 1;

		PathExpression nodeAtPath;
		if (ownerPos < 0) {
			nodeAtPath = PathExpression.root();
		} else {
			nodeAtPath = buildPathUpTo(path, ownerPos);
		}

		JvmNode node = findNodeForPath(nodeAtPath, state);
		if (node == null) {
			return -1;
		}

		if (typeSelector.matchesType(node.getConcreteType().getRawType())) {
			return ownerPos + 1;
		}

		return -1;
	}

	private PathExpression buildPathUpTo(PathExpression fullPath, int segmentIndex) {
		List<Segment> segments = fullPath.getSegments();
		PathExpression result = PathExpression.root();
		for (int i = 0; i <= segmentIndex && i < segments.size(); i++) {
			result = result.appendSegment(segments.get(i));
		}
		return result;
	}

	private boolean isRootTypeSelector(PathExpression path) {
		List<Segment> segments = path.getSegments();
		return (segments.size() == 1
			&& segments.get(0).isSingleSelector()
			&& segments.get(0).getFirstSelector() instanceof TypeSelector);
	}

	private boolean hasFieldLevelTypeSelectorSiblings(PathExpression rootTypePath, AssemblyState state) {
		TypeSelector rootTypeSelector = (TypeSelector)rootTypePath.getSegments().get(0).getFirstSelector();
		Class<?> rootType = rootTypeSelector.getTargetType();

		for (PathExpression path : state.candidatesByPath.keySet()) {
			if (path.equals(rootTypePath)) {
				continue;
			}
			List<Segment> segments = path.getSegments();
			if (segments.size() > 1
				&& segments.get(0).isSingleSelector()
				&& segments.get(0).getFirstSelector() instanceof TypeSelector) {
				TypeSelector ts = (TypeSelector)segments.get(0).getFirstSelector();
				if (ts.getTargetType().equals(rootType)) {
					return true;
				}
			}
		}
		return false;
	}

	private @Nullable Object resolveThenApplyAncestorValue(
		JvmNode node,
		PathExpression currentPath,
		AssemblyState state
	) {
		if (state.rootTypeSelectors.isEmpty()) {
			return null;
		}

		for (Map.Entry<PathExpression, ValueCandidate> entry : state.rootTypeSelectors) {
			TypeSelector typeSelector = (TypeSelector)entry.getKey().getSegments().get(0).getFirstSelector();

			if (node.getConcreteType() != null && typeSelector.matchesType(node.getConcreteType().getRawType())) {
				Object value = entry.getValue().value;
				if (value instanceof LazyValueHolder) {
					Object resolved = resolveLazyWithCache((LazyValueHolder)value, state);
					if (resolved == LazyValueHolder.RECURSION_BLOCKED) {
						return null;
					}
					return resolved;
				}
				return value;
			}

			List<Segment> pathSegments = currentPath.getSegments();
			for (int pos = pathSegments.size() - 1; pos >= 0; pos--) {
				PathExpression ancestorPath = buildPathUpTo(currentPath, pos - 1);
				JvmNode ancestorNode = findNodeForPath(ancestorPath, state);
				if (ancestorNode != null
					&& ancestorNode.getConcreteType() != null
					&& typeSelector.matchesType(ancestorNode.getConcreteType().getRawType())) {
					Object value = entry.getValue().value;
					Object resolved;
					if (value instanceof LazyValueHolder) {
						resolved = resolveLazyWithCache((LazyValueHolder)value, state);
						if (resolved == LazyValueHolder.RECURSION_BLOCKED || resolved == null) {
							return null;
						}
					} else {
						resolved = value;
					}

					Object current = resolved;
					for (int i = pos; i < pathSegments.size() && current != null; i++) {
						Segment pathSeg = pathSegments.get(i);
						Selector selector = pathSeg.getFirstSelector();

						if (selector instanceof NameSelector) {
							current = getFieldValueByName(current, ((NameSelector)selector).getName());
						} else if (selector instanceof IndexSelector) {
							current = getElementAtIndex(current, ((IndexSelector)selector).getIndex());
						} else {
							current = null;
						}
					}
					if (current != null) {
						return current;
					}
				}
			}
		}
		return null;
	}

	private static @Nullable Object getFieldValueByName(Object obj, String fieldName) {
		Field field = TypeCache.getFieldsByName(obj.getClass()).get(fieldName);
		if (field == null) {
			return null;
		}
		try {
			return field.get(obj);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	private static @Nullable Object getElementAtIndex(Object container, int index) {
		if (container instanceof List) {
			List<?> list = (List<?>)container;
			if (index >= 0 && index < list.size()) {
				return list.get(index);
			}
			return null;
		}
		if (container.getClass().isArray()) {
			int length = java.lang.reflect.Array.getLength(container);
			if (index >= 0 && index < length) {
				return java.lang.reflect.Array.get(container, index);
			}
			return null;
		}
		return null;
	}

	private @Nullable JvmNode findNodeForPath(PathExpression path, AssemblyState state) {
		String pathStr = path.toExpression();
		JvmNode node = state.nodeByPath.get(pathStr);
		if (node != null) {
			return node;
		}
		if (state.nodeTree != null) {
			return state.nodeTree.resolve(path);
		}
		return null;
	}

	private @Nullable Object resolveLazyWithCache(LazyValueHolder holder, AssemblyState state) {
		if (state.resolvedLazyCache.containsKey(holder)) {
			return state.resolvedLazyCache.get(holder);
		}
		Object resolved = holder.getValue();
		if (resolved != null && resolved != LazyValueHolder.RECURSION_BLOCKED) {
			state.resolvedLazyCache.put(holder, resolved);
		}
		return resolved;
	}

	private boolean hasMatchingTypeSelector(JvmType nodeType, PathIndex pathIndex) {
		Set<PathExpression> typePatternPaths = pathIndex.getTypePatternPaths();
		if (typePatternPaths.isEmpty()) {
			return false;
		}

		Class<?> rawType = nodeType.getRawType();
		for (PathExpression pattern : typePatternPaths) {
			List<Segment> segments = pattern.getSegments();
			for (Segment segment : segments) {
				if (segment.isSingleSelector() && segment.getFirstSelector() instanceof TypeSelector) {
					TypeSelector typeSelector = (TypeSelector)segment.getFirstSelector();
					if (typeSelector.matchesType(rawType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isUnderExcludedPath(PathExpression path, Set<PathExpression> excludedPaths) {
		for (PathExpression excluded : excludedPaths) {
			if (path.isChildOf(excluded)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private CombinableArbitrary<?> applyFilters(
		CombinableArbitrary<?> arbitrary,
		PathExpression path,
		Class<?> actualType,
		AssemblyState state
	) {
		List<AnalysisResult.PostConditionFilter> filters = state.filtersByPath.get(path);
		if (filters == null || filters.isEmpty()) {
			return arbitrary;
		}

		CombinableArbitrary<?> result = arbitrary;
		for (AnalysisResult.PostConditionFilter postCondition : filters) {
			Class<?> expectedType = postCondition.getType();
			if (!Types.isAssignable(actualType, expectedType)) {
				throw new IllegalArgumentException(
					"Wrong type filter is applied. Expected: " + expectedType + ", Actual: " + actualType
				);
			}
			Predicate filter = postCondition.getFilter();
			result = result.filter(filter);
		}
		return result;
	}

	private CombinableArbitrary<?> applyCustomizers(
		CombinableArbitrary<?> arbitrary,
		PathExpression path,
		AssemblyState state
	) {
		List<AnalysisResult.PropertyCustomizer> customizers = state.customizersByPath.get(path);
		if (customizers == null || customizers.isEmpty()) {
			return arbitrary;
		}

		boolean isValueSet = state.candidatesByPath.containsKey(path);

		CombinableArbitrary<?> result = arbitrary;
		for (AnalysisResult.PropertyCustomizer propertyCustomizer : customizers) {
			if (isValueSet && !propertyCustomizer.isAfterSet()) {
				continue;
			}
			Function<CombinableArbitrary<?>, CombinableArbitrary<?>> customizer = propertyCustomizer.getCustomizer();
			result = customizer.apply(result);
		}
		return result;
	}

	private static void applyDecomposeResult(DecomposeResult result, AssemblyState state) {
		for (PathExpression path : result.getSubtreesToRemove()) {
			state.candidatesByPath.remove(path);
			state.candidatesByPath.keySet().removeIf(key -> key.isChildOf(path));
		}

		state.candidatesByPath.putAll(result.getValuesToPut());
		for (PathExpression decomposedPath : result.getValuesToPut().keySet()) {
			state.traceContext.markDecomposedPath(decomposedPath.toExpression());
		}

		PathExpression limitPath = result.getLimitPath();
		if (limitPath != null) {
			state.limitsByPath.put(limitPath, result.getLimitValue());
		}
	}

	private @Nullable Object resolveLazyValue(@Nullable Object value, boolean isFromRegister, AssemblyState state) {
		if (!(value instanceof LazyValueHolder)) {
			return value;
		}
		// Register lazy: evaluate fresh each time (thenApply wraps entire object).
		// User lazy: cached within one sample for consistency (e.g., setLazy with Arbitraries.of()).
		return isFromRegister
			? ((LazyValueHolder)value).getValue()
			: resolveLazyWithCache((LazyValueHolder)value, state);
	}

	private CombinableArbitrary<?> traceAndReturnValue(
		@Nullable Object value,
		String source,
		PathExpression currentPath,
		Class<?> currentRawType,
		boolean isCurrentTypeContainer,
		@Nullable ArbitraryGeneratorContext parentContext,
		JvmType currentType,
		AssemblyState state
	) {
		if (state.traceContext.isEnabled()) {
			traceAssemblyStep(
				state,
				currentPath,
				source,
				value,
				0.0,
				isCurrentTypeContainer,
				parentContext != null ? parentContext.getArbitraryProperty().isContainer() : null,
				currentType.getRawType().getSimpleName()
			);
		}
		return wrapValueWithFiltersAndCustomizers(value, currentPath, currentRawType, state);
	}

	private CombinableArbitrary<?> wrapValueWithFiltersAndCustomizers(
		@Nullable Object value,
		PathExpression path,
		Class<?> rawType,
		AssemblyState state
	) {
		CombinableArbitrary<?> result =
			value instanceof CombinableArbitrary ? (CombinableArbitrary<?>)value : CombinableArbitrary.from(value);
		result = applyFilters(result, path, rawType, state);
		result = applyCustomizers(result, path, state);
		return result;
	}

	private void traceAssemblyStep(
		AssemblyState state,
		PathExpression path,
		String source,
		@Nullable Object value,
		double nullInject,
		boolean isContainer,
		@Nullable Boolean ownerIsContainer,
		@Nullable String typeName
	) {
		traceAssemblyStep(
			state,
			path,
			source,
			value,
			nullInject,
			isContainer,
			ownerIsContainer,
			typeName,
			null,
			null,
			null,
			null,
			null
		);
	}

	private void traceAssemblyStep(
		AssemblyState state,
		PathExpression path,
		String source,
		@Nullable Object value,
		double nullInject,
		boolean isContainer,
		@Nullable Boolean ownerIsContainer,
		@Nullable String typeName,
		@Nullable String creationMethod,
		@Nullable String creationDetail,
		@Nullable String introspector,
		@Nullable String declaredType,
		@Nullable String actualType
	) {
		state.traceContext.recordAssemblyStep(
			path.toExpression(),
			source,
			value,
			nullInject,
			isContainer,
			ownerIsContainer,
			typeName,
			creationMethod,
			creationDetail,
			introspector,
			declaredType,
			actualType
		);
	}

	private static @Nullable String formatCreationMethodType(@Nullable CreationMethod cm) {
		if (cm == null) {
			return null;
		}
		return cm.getType().name();
	}

	private static @Nullable String formatCreationDetail(@Nullable CreationMethod cm) {
		if (cm == null) {
			return null;
		}

		switch (cm.getType()) {
			case FIELD:
				if (cm instanceof FieldAccessCreationMethod) {
					return "field:" + ((FieldAccessCreationMethod)cm).getField().getName();
				}
				return "field";
			case CONSTRUCTOR:
				if (cm instanceof ConstructorParamCreationMethod) {
					ConstructorParamCreationMethod cpm = (ConstructorParamCreationMethod)cm;
					return "constructor[" + cpm.getParameterIndex() + "]";
				}
				return "constructor";
			case METHOD:
				if (cm instanceof MethodInvocationCreationMethod) {
					return "method:" + ((MethodInvocationCreationMethod)cm).getMethod().getName();
				}
				return "method";
			case CONTAINER_ELEMENT:
				return "element";
			default:
				return null;
		}
	}

	private List<JvmNode> deduplicateChildren(List<JvmNode> children) {
		Map<String, JvmNode> nodeByKey = new LinkedHashMap<>();

		for (JvmNode child : children) {
			String key = buildDeduplicationKey(child);

			JvmNode existing = nodeByKey.get(key);
			if (existing == null) {
				nodeByKey.put(key, child);
			} else {
				Class<?> existingType = existing.getConcreteType().getRawType();
				Class<?> childType = child.getConcreteType().getRawType();

				// Prefer concrete type over interface when both appear for the same field
				if (existingType.isInterface() && !childType.isInterface()) {
					nodeByKey.put(key, child);
				}
			}
		}

		return new ArrayList<>(nodeByKey.values());
	}

	private String buildDeduplicationKey(JvmNode node) {
		String nodeName = node.getNodeName();
		Integer index = node.getIndex();

		if (index != null) {
			return "index:" + index;
		} else if (nodeName != null) {
			return "name:" + nodeName;
		} else {
			return "type:" + node.getConcreteType().getRawType().getName();
		}
	}

	private static final class PathIndex {

		private static final Pattern INDEX_PATTERN = Pattern.compile("\\[\\d+]");

		private final Map<PathExpression, Set<PathExpression>> childPathsByParent;
		private final Set<PathExpression> wildcardParentPaths;
		private final Set<PathExpression> typePatternPaths;

		PathIndex(
			Set<PathExpression> valuePaths,
			Set<PathExpression> userContainerSizePaths,
			Set<PathExpression> customizerPaths,
			Set<PathExpression> notNullPaths
		) {
			this.childPathsByParent = new HashMap<>();
			this.wildcardParentPaths = new HashSet<>();
			this.typePatternPaths = new HashSet<>();

			indexAncestors(valuePaths);
			indexAncestors(userContainerSizePaths);
			indexAncestors(customizerPaths);
			indexAncestors(notNullPaths);

			for (PathExpression sizePath : userContainerSizePaths) {
				childPathsByParent.computeIfAbsent(sizePath, k -> new HashSet<>());
			}
		}

		private void indexAncestors(Set<PathExpression> paths) {
			for (PathExpression path : paths) {
				if (path.hasTypeSelector()) {
					typePatternPaths.add(path);
					continue;
				}

				PathExpression ancestor = path.getParent();
				while (ancestor != null && !ancestor.equals(path)) {
					childPathsByParent.computeIfAbsent(ancestor, k -> new HashSet<>()).add(path);
					PathExpression next = ancestor.getParent();
					if (next != null && next.equals(ancestor)) {
						break;
					}
					ancestor = next;
				}

				if (path.hasWildcard()) {
					PathExpression wildcardParent = path.getParent();
					if (wildcardParent != null) {
						wildcardParentPaths.add(wildcardParent);
					}
				}
			}
		}

		boolean hasChildPaths(PathExpression parentPath) {
			if (childPathsByParent.containsKey(parentPath)) {
				return true;
			}
			if (wildcardParentPaths.contains(parentPath)) {
				return true;
			}
			String pathStr = parentPath.toExpression();
			// Convert indexed paths ($.list[0]) to wildcard form ($.list[*]) to check wildcard coverage
			if (pathStr.contains("[") && !pathStr.contains("[*]")) {
				String wildcardForm = INDEX_PATTERN.matcher(pathStr).replaceAll("[*]");
				PathExpression wildcardPath = PathExpression.of(wildcardForm);
				return wildcardParentPaths.contains(wildcardPath)
					|| childPathsByParent.containsKey(wildcardPath);
			}
			return false;
		}

		Set<PathExpression> getTypePatternPaths() {
			return typePatternPaths;
		}
	}

	static final class CachedTypeMetadata {

		final PropertyNameResolver nameResolver;
		final NullInjectGenerator nullInjectGenerator;
		final boolean isContainerType;
		final boolean hasCandidateConcretePropertyResolvers;

		CachedTypeMetadata(
			PropertyNameResolver nameResolver,
			NullInjectGenerator nullInjectGenerator,
			boolean isContainerType,
			boolean hasCandidateConcretePropertyResolvers
		) {
			this.nameResolver = nameResolver;
			this.nullInjectGenerator = nullInjectGenerator;
			this.isContainerType = isContainerType;
			this.hasCandidateConcretePropertyResolvers = hasCandidateConcretePropertyResolvers;
		}
	}

	private static final class AssemblyState {

		final JvmNodeTree nodeTree;
		final Map<PathExpression, ValueCandidate> candidatesByPath;
		final com.navercorp.fixturemonkey.api.property.TreeRootProperty rootProperty;
		final FixtureMonkeyOptions options;
		final com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext monkeyGeneratorContext;
		final com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext loggingContext;
		final Set<PathExpression> justPaths;
		final Set<PathExpression> notNullPaths;
		final Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath;
		final Map<PathExpression, Integer> limitsByPath;
		final InterfaceSelectionStrategy interfaceSelectionStrategy;
		final long assemblySeed;
		final AtomicInteger interfaceSelectionCounter;
		final Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath;
		final TraceContext traceContext;
		final Map<Class<?>, ArbitraryIntrospector> introspectorsByType;

		final Map<JvmNode, JvmNodeTree> concreteTreeByNode;

		final Map<JvmType, Boolean> containerTypeCache;

		final Map<JvmNode, Property> propertyByNode;

		final Set<PathExpression> userContainerSizePaths;

		final PathIndex pathIndex;

		final @Nullable NodeTreeAdapter nodeTreeAdapter;

		final Function<JvmNode, Property> nodePropertyFactory;

		final Map<JvmNode, Property> propertyPathPropertyByNode;

		final Map<String, JvmNode> nodeByPath;

		final Map<LazyValueHolder, Object> resolvedLazyCache;

		final List<Map.Entry<PathExpression, ValueCandidate>> wildcardEntries;

		final List<Map.Entry<PathExpression, ValueCandidate>> typeSelectorEntries;

		final List<Map.Entry<PathExpression, ValueCandidate>> rootTypeSelectors;

		final ValueDecomposer valueDecomposer;

		final @Nullable ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache;

		AssemblyState(
			JvmNodeTree nodeTree,
			Map<PathExpression, ValueCandidate> candidatesByPath,
			com.navercorp.fixturemonkey.api.property.TreeRootProperty rootProperty,
			FixtureMonkeyOptions options,
			com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext monkeyGeneratorContext,
			com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext loggingContext,
			Set<PathExpression> justPaths,
			Set<PathExpression> notNullPaths,
			Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath,
			Map<PathExpression, Integer> limitsByPath,
			InterfaceSelectionStrategy interfaceSelectionStrategy,
			Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath,
			TraceContext traceContext,
			Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
			@Nullable NodeTreeAdapter nodeTreeAdapter,
			Set<PathExpression> userContainerSizePaths
		) {
			this.nodeTree = nodeTree;
			this.candidatesByPath = candidatesByPath;
			this.rootProperty = rootProperty;
			this.options = options;
			this.monkeyGeneratorContext = monkeyGeneratorContext;
			this.loggingContext = loggingContext;
			this.justPaths = justPaths;
			this.notNullPaths = notNullPaths;
			this.filtersByPath = filtersByPath;
			this.limitsByPath = limitsByPath;
			this.interfaceSelectionStrategy = interfaceSelectionStrategy;
			this.assemblySeed = ThreadLocalRandom.current().nextLong();
			this.interfaceSelectionCounter = new AtomicInteger(0);
			this.customizersByPath = customizersByPath;
			this.traceContext = traceContext;
			this.introspectorsByType = introspectorsByType;
			this.nodeTreeAdapter = nodeTreeAdapter;

			this.containerTypeCache = new HashMap<>();
			this.propertyByNode = new IdentityHashMap<>();
			this.concreteTreeByNode = new IdentityHashMap<>();
			this.propertyPathPropertyByNode = new IdentityHashMap<>();
			this.nodeByPath = new HashMap<>();
			this.resolvedLazyCache = new IdentityHashMap<>();
			this.valueDecomposer = new ValueDecomposer(
				candidatesByPath,
				limitsByPath,
				nodeTree,
				new ObjectValueExtractor()
			);
			this.userContainerSizePaths = userContainerSizePaths;
			this.pathIndex = new PathIndex(
				new HashSet<>(candidatesByPath.keySet()),
				userContainerSizePaths,
				new HashSet<>(customizersByPath.keySet()),
				notNullPaths
			);

			List<Map.Entry<PathExpression, ValueCandidate>> wildcards = new ArrayList<>();
			List<Map.Entry<PathExpression, ValueCandidate>> typeSelectors = new ArrayList<>();
			List<Map.Entry<PathExpression, ValueCandidate>> rootTypeOnly = new ArrayList<>();
			for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
				PathExpression pattern = entry.getKey();
				if (pattern.hasWildcard()) {
					wildcards.add(entry);
				} else if (pattern.hasTypeSelector()) {
					typeSelectors.add(entry);
					List<Segment> segments = pattern.getSegments();
					if (segments.size() == 1
						&& segments.get(0).isSingleSelector()
						&& segments.get(0).getFirstSelector() instanceof TypeSelector) {
						rootTypeOnly.add(entry);
					}
				}
			}
			this.wildcardEntries = wildcards;
			this.typeSelectorEntries = typeSelectors;
			this.rootTypeSelectors = rootTypeOnly;

			this.nodePropertyFactory = new JvmNodePropertyFactory(child -> {
				JvmNode parent = nodeTree.getParent(child);
				if (parent != null) {
					return parent;
				}
				JvmNodeTree concreteTree = this.concreteTreeByNode.get(child);
				if (concreteTree != null) {
					return concreteTree.getParent(child);
				}
				return null;
			});

			ConcurrentHashMap<?, ?> rawCache = nodeTreeAdapter != null ? nodeTreeAdapter.getNodeMetadataCache() : null;
			if (rawCache != null) {
				@SuppressWarnings("unchecked")
				ConcurrentHashMap<JvmType, CachedTypeMetadata> typedCache = (ConcurrentHashMap<
					JvmType,
					CachedTypeMetadata
					>)rawCache;
				this.typeMetadataCache = typedCache;
			} else {
				this.typeMetadataCache = null;
			}
		}
	}
}
