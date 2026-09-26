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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.TraceableCombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
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
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PropertyCustomizer;
import com.navercorp.fixturemonkey.planner.LazyValueHolder;
import com.navercorp.fixturemonkey.planner.ValueProjection;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
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
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

/**
 * Assembles the values a {@link ValueProjection} holds into an object, generating what no value is given for.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class ValueProjectionAssembler {
	private final AssembleContext context;

	private ValueProjectionAssembler(AssembleContext context) {
		this.context = context;
	}

	/**
	 * Assembles the plan's values into a CombinableArbitrary.
	 * <p>
	 * Values that are explicitly set in the projection are used directly; missing values
	 * are generated using fixture-monkey's arbitrary generation infrastructure.
	 *
	 * @param context the assembly context, with the plan whose values and tree are assembled
	 * @return a CombinableArbitrary that produces the assembled object
	 */
	public static CombinableArbitrary<?> assemble(AssembleContext context) {
		return new ValueProjectionAssembler(context).assemble();
	}

	private CombinableArbitrary<?> assemble() {
		AssemblyState state = new AssemblyState(context);
		if (context.getTraceContext().isEnabled()) {
			recordMergedCandidates(state);
		}
		JvmNode rootNode = context.getPlan().getNodeTree().getRootNode();
		return assembleNode(rootNode, state, null, null, PathExpression.root(), new HashSet<>());
	}

	private void recordMergedCandidates(AssemblyState state) {
		Map<String, ValueCandidate> mergedCandidates = new LinkedHashMap<>();
		for (Map.Entry<PathExpression, ValueCandidate> entry : state.candidates.getEntries()) {
			mergedCandidates.put(entry.getKey().toExpression(), entry.getValue());
		}
		for (Map.Entry<ScopedPath, ValueCandidate> entry : state.scopes.getDefinedScopeValues()) {
			mergedCandidates.put(entry.getKey().toExpression(), entry.getValue());
		}
		Map<String, @Nullable Object> traceValues = new LinkedHashMap<>();
		Map<String, Integer> traceOrders = new LinkedHashMap<>();
		Map<String, String> traceSources = new LinkedHashMap<>();
		mergedCandidates.forEach((path, candidate) -> {
			traceValues.put(path, candidate.value);
			traceOrders.put(path, candidate.precedence.sequence());
			traceSources.put(path, candidate.sourceLabel());
		});
		context.getTraceContext().recordMergedCandidates(traceValues, traceOrders, traceSources);
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
		FixtureMonkeyOptions options = state.context.getOptions();
		JvmType currentType = node.getConcreteType();

		state.assemblyTree.placeNode(currentPath, node);

		boolean isCurrentTypeContainer = TypeMetadataResolver.isContainerType(currentType, state);

		Class<?> currentRawType = currentType.getRawType();
		boolean isCircular = !isCurrentTypeContainer && visitedTypes.contains(currentRawType);

		boolean addedToVisited = false;
		if (!isCurrentTypeContainer && visitedTypes.add(currentRawType)) {
			addedToVisited = true;
		}

		try {
			if (state.scopes.isUnderRootJust(currentPath)) {
				return assembleNodeDefault(node, state, parentContext, parentPath, currentPath, visitedTypes);
			}

			ValueCandidate justCandidate =
				state.scopes.isRootJust(currentPath) ? state.candidates.at(currentPath) : null;
			if (justCandidate != null) {
				return traceAndReturnValue(
					justCandidate.value,
					justCandidate.sourceLabel(),
					currentPath,
					currentRawType,
					isCurrentTypeContainer,
					parentContext,
					currentType,
					state,
					null
				);
			}

			boolean isValueSet = state.candidates.contains(currentPath);
			boolean hasChildValues = state.pathIndex.hasChildPaths(currentPath);

			// set("field", null) vs child values priority:
			// If null was set AFTER all child values, null wins. Otherwise children win.
			if (isValueSet && hasChildValues) {
				ValueCandidate nullCandidate = state.candidates.at(currentPath);
				if (nullCandidate != null && nullCandidate.value == null) {
					boolean hasActualChildCandidates =
						state.candidates.hasCandidateBelow(currentPath);
					if (!hasActualChildCandidates) {
						return wrapValueWithFiltersAndCustomizers(null, currentPath, currentRawType, state);
					}
					if (state.candidates.isNullDeclaredAfterEveryCandidateBelow(
						nullCandidate.precedence,
						currentPath
					)) {
						return wrapValueWithFiltersAndCustomizers(null, currentPath, currentRawType, state);
					}
				}
			}

			WinningValueCandidate winner = !hasChildValues || !isValueSet
				? PathMatcher.findWinningCandidate(currentPath, state)
				: null;
			ScopeChain chain = state.chainOf(currentPath);
			if (!hasChildValues
				&& winner == null
				&& (state.scopes.definedScopeNotNullDepth(chain) != Integer.MAX_VALUE
				|| state.scopes.hasDefinedScopeDirectiveBelow(chain, Integer.MAX_VALUE))) {
				state.scopes.markNotNullRequired(currentPath);
				if (isCurrentTypeContainer) {
					int requiredElementCount = state.scopes.definedScopeRequiredElementCount(chain);
					growChildrenTo(node, currentPath, requiredElementCount, state);
				}
			}
			if (winner != null && (!hasChildValues || winner.isFromDefinedScope())) {
				ValueCandidate bestCandidate = winner.candidate;
				int scopeDepth = winner.scopeDepth;
				if (!winner.isDeclaredAt(currentPath)) {
					state.limits.consume(winner.declaredPath, winner.scopeNode);
				}

				Object setValue = LazyResolver.resolveLazyValue(
					bestCandidate.value,
					bestCandidate.precedence,
					state
				);
				boolean unusable = setValue == LazyResolver.RECURSION_BLOCKED
					|| (setValue == null
					&& winner.isFromDefinedScope()
					&& state.scopes.isNotNullRequired(currentPath));

				int outerLimit = winner.isScopeRoot() ? scopeDepth : scopeDepth - 1;
				boolean outrankedInside = winner.isFromDefinedScope()
					&& (hasChildValues
					|| state.scopes.hasDefinedScopeDirectiveBelow(chain, outerLimit)
					|| state.scopes.hasDefinedScopeFilterBelow(chain, outerLimit)
					|| state.scopes.hasDefinedScopeCustomizerBelow(chain, outerLimit));

				if (!unusable && setValue != null && outrankedInside) {
					state.candidates.replaceWithDefinedScopeValue(
						currentPath,
						bestCandidate.withValue(setValue),
						scopeDepth
					);
					if (isCurrentTypeContainer) {
						resizeChildrenToValue(node, currentPath, setValue, state);
					}
					isValueSet = true;
				} else if (!hasChildValues) {
					if (unusable) {
						return assembleNodeDefault(node, state, parentContext, parentPath, currentPath, visitedTypes);
					}
					return traceAndReturnValue(
						setValue,
						bestCandidate.sourceLabel(),
						currentPath,
						currentRawType,
						isCurrentTypeContainer,
						parentContext,
						currentType,
						state,
						winner
					);
				}
			}

			if (isValueSet) {
				ValueCandidate currentCandidate = state.candidates.at(currentPath);
				JvmNodeTree valueClassTree = hasChildValues && !isCurrentTypeContainer
					? treeOfValueClass(node, currentPath, currentCandidate, state)
					: null;
				if (valueClassTree != null) {
					return assembleNode(
						valueClassTree.getRootNode(),
						state,
						parentContext,
						parentPath,
						currentPath,
						visitedTypes
					);
				}
				DirectivePrecedence parentPrecedence = currentCandidate != null
					? currentCandidate.precedence
					: DirectivePrecedence.rootScope(0);
				boolean underDefinedScopeValue = state.candidates.decomposedScopeDepthAt(currentPath) >= 0;
				if (underDefinedScopeValue
					&& isCurrentTypeContainer
					&& currentCandidate != null
					&& currentCandidate.value != null) {
					resizeChildrenToValue(node, currentPath, currentCandidate.value, state);
				}
				DecomposeResult decomposeResult = state.valueDecomposer.decompose(
					currentPath,
					currentRawType,
					isCurrentTypeContainer,
					parentPrecedence,
					underDefinedScopeValue
				);
				applyDecomposeResult(decomposeResult, state);
				if (decomposeResult.hasEarlyReturn()) {
					Object earlyValue = decomposeResult.getEarlyReturnValue();

					// Apply wildcard overrides to container elements when a wildcard has higher order
					if (isCurrentTypeContainer && earlyValue != null && state.candidates.hasRootWildcards()) {
						earlyValue = applyWildcardOverridesToContainer(
							earlyValue, currentPath, parentPrecedence, state
						);
					}

					return wrapValueWithFiltersAndCustomizers(
						earlyValue,
						currentPath,
						currentRawType,
						state
					);
				}
			}

			if (!isValueSet && !hasChildValues) {
				Object typedValue = LazyResolver.resolveThenApplyAncestorValue(currentPath, state);
				if (typedValue != null) {
					return traceAndReturnValue(
						typedValue,
						"REGISTER",
						currentPath,
						currentRawType,
						isCurrentTypeContainer,
						parentContext,
						currentType,
						state,
						null
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
					Object setValue = state.candidates.at(currentPath).value;
					if (state.context.getTraceContext().isEnabled()) {
						traceAssemblyStep(
							state,
							currentPath,
							state.candidates.at(currentPath).sourceLabel(),
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
				// AnonymousArbitraryIntrospector's proxy substitutes self for self-type methods,
				// so null here is safe and breaks the assembleNodeDefault recursion cycle.
				if (isCircular) {
					return wrapValueWithFiltersAndCustomizers(null, currentPath, currentRawType, state);
				}
				JvmNodeTree anonymousTree = state.context.getPlan().getNodeTreeFactory().createAnonymousNodeTree(
					nodeType,
					currentPath,
					state.assemblyTree.ancestorNodes(currentPath)
				);
				if (anonymousTree != null) {
					state.assemblyTree.attach(anonymousTree);
					return assembleNodeDefault(
						anonymousTree.getRootNode(),
						state,
						parentContext,
						parentPath,
						currentPath,
						visitedTypes
					);
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

			Property nodeProperty = state.properties.generationPropertyOf(node);
			TypeMetadataResolver.writeBackTypeMetadata(node, nodeProperty, state);

			PropertyNameResolver nameResolver =
				TypeMetadataResolver.resolveNameResolver(node, nodeProperty, state);

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
			double nullInject = TypeMetadataResolver.resolveNullInjectGenerator(node, nodeProperty, state)
				.generate(nullInjectContext);

			// Primitive slots cannot hold null; the introspector would throw IllegalArgumentException
			// when the array/setter writes the null produced by injectNull.
			if (currentRawType.isPrimitive()) {
				nullInject = 0.0;
			}

			if (state.scopes.isNotNullRequired(currentPath)) {
				nullInject = 0.0;
			}

			if (nullInject > 0 && state.scopes.isSelectedByDefinedScopeWithValues(chain)) {
				nullInject = 0.0;
			}

			if (nullInject > 0 && state.candidates.matchesRootWildcard(currentPath)) {
				nullInject = 0.0;
			}

			if (nullInject > 0 && (isValueSet || hasChildValues)) {
				nullInject = 0.0;
			}

			// A user-supplied postCondition predicate (setPostCondition) is applied to the
			// generated value verbatim, so null injection would invoke the predicate with null
			// and typically NPE inside the user lambda. Treat the filter as an implicit not-null.
			if (nullInject > 0 && !filtersFor(currentPath, state).isEmpty()) {
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

			List<JvmNode> allChildren = deduplicateChildren(state.assemblyTree.childrenOf(node));

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
							state.pathIndex.hasChildPaths(childPath)
								|| state.candidates.contains(childPath)
						) {
							children.add(child);
						}
					}
				}
			} else {
				children = allChildren;
			}

			Integer childLimit = isCurrentTypeContainer ? state.limits.childLimit(currentPath) : null;
			if (childLimit != null && children.size() > childLimit) {
				children = children.subList(0, childLimit);
			}

			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			for (JvmNode childNode : children) {
				PathExpression childPath = buildChildPath(currentPath, childNode, node, state);

				Property childProperty = state.properties.generationPropertyOf(childNode);
				TypeMetadataResolver.writeBackTypeMetadata(childNode, childProperty, state);
				PropertyNameResolver childNameResolver =
					TypeMetadataResolver.resolveNameResolver(childNode, childProperty, state);

				ObjectProperty childObjectProperty = new ObjectProperty(
					childProperty,
					childNameResolver,
					childNode.getIndex()
				);

				boolean childIsContainer = TypeMetadataResolver.isContainerType(childNode.getConcreteType(), state);

				ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
					childProperty,
					childNode.getIndex(),
					arbitraryProperty, // now we can reference the parent's arbitraryProperty
					childIsContainer,
					childNameResolver
				);
				double childNullInject =
					TypeMetadataResolver.resolveNullInjectGenerator(childNode, childProperty, state)
					.generate(
					childNullInjectContext
				);

				if (childNullInject > 0) {
					if (
						state.candidates.contains(childPath)
							|| state.pathIndex.hasChildPaths(childPath)
							|| state.scopes.isNotNullRequired(childPath)
							|| state.scopes.hasCustomizerAt(state.chainOf(childPath), childPath)
							|| !filtersFor(childPath, state).isEmpty()
							|| state.candidates.matchesRootWildcard(childPath)
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
				state.context.getGeneratorContext(),
				options.getGenerateUniqueMaxTries(),
				nullInject,
				state.context.getLoggingContext()
			);

			Class<?> actualType = com.navercorp.fixturemonkey.api.type.Types.normalizeRawType(
				nodeProperty.getJvmType().getRawType()
			);
			ArbitraryIntrospector typeSpecificIntrospector = introspectorFor(actualType, node, currentPath, state);

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
			result = applyCustomizers(result, currentPath, state, null);

			if (state.context.getTraceContext().isEnabled()) {
				String introspectorName =
					typeSpecificIntrospector != null ? typeSpecificIntrospector.getClass().getSimpleName() : null;
				CreationMethod nodeCreationMethod = node.getCreationMethod();
				ValueCandidate traceCandidate = isValueSet ? state.candidates.at(currentPath) : null;
				String assemblySource;
				if (traceCandidate == null) {
					assemblySource = "GENERATED";
				} else if (state.context.getTraceContext().isDecomposedPath(currentPath.toExpression())) {
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
		CachedTypeMetadata cached = state.typeMetadataCache.get(node.getConcreteType());
		if (cached != null) {
			return cached.hasCandidateConcretePropertyResolvers;
		}
		Property property = JvmNodePropertyFactory.fromType(node.getConcreteType());
		return state.context.getOptions().getCandidateConcretePropertyResolver(property) != null;
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
		FixtureMonkeyOptions options = state.context.getOptions();

		Property interfaceProperty = state.properties.ownGenerationPropertyOf(node);

		@SuppressWarnings("deprecation")
		CandidateConcretePropertyResolver resolver = options.getCandidateConcretePropertyResolver(interfaceProperty);
		List<Property> implementations =
			resolver != null ? resolver.resolve(interfaceProperty) : Collections.emptyList();

		if (implementations == null || implementations.isEmpty()) {
			return CombinableArbitrary.NOT_GENERATED;
		}

		InterfaceSelectionStrategy strategy = InterfaceSelectionStrategy.RANDOM;
		long seed = state.assemblySeed;
		int sampleIndex = state.interfaceSelectionCounter.getAndIncrement();

		int selectedIndex = strategy.selectIndex(implementations.size(), seed, sampleIndex);
		Property selectedProperty = implementations.get(selectedIndex);

		JvmType concreteType = selectedProperty.getJvmType();

		JvmNodeTree concreteTree = state.context.getPlan().getNodeTreeFactory().createConcreteNodeTree(
			concreteType,
			node.getDeclaredType(),
			currentPath,
			state.assemblyTree.ancestorNodes(currentPath)
		);

		CombinableArbitrary<?> result;
		if (concreteTree != null) {
			JvmNode concreteRootNode = concreteTree.getRootNode();

			state.assemblyTree.attach(concreteTree);
			state.properties.chooseGenerationProperty(concreteRootNode, selectedProperty);

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
		Property nodeProperty = state.properties.ownGenerationPropertyOf(node);
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
		FixtureMonkeyOptions options = state.context.getOptions();
		PropertyNameResolver nameResolver = options.getPropertyNameResolver(concreteProperty);

		ObjectProperty objectProperty = new ObjectProperty(concreteProperty, nameResolver, originalNode.getIndex());

		Class<?> actualType = com.navercorp.fixturemonkey.api.type.Types.normalizeRawType(
			concreteProperty.getJvmType().getRawType()
		);
		ArbitraryIntrospector typeSpecificIntrospector = introspectorFor(actualType, originalNode, currentPath, state);

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
		boolean isContainer = TypeMetadataResolver.isContainerType(originalType, state);

		Property propertyForNullInject = parentPath == null ? new RootProperty(concreteProperty) : concreteProperty;
		ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
			propertyForNullInject,
			originalNode.getIndex(),
			null,
			isContainer,
			nameResolver
		);
		double nullInject = options.getNullInjectGenerator(concreteProperty).generate(nullInjectContext);

		if (state.scopes.isNotNullRequired(normalizedPath)) {
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

		JvmType concreteJvmType = concreteProperty.getJvmType();
		Map<String, JvmNode> concreteChildrenByName =
			buildConcreteChildrenMap(concreteJvmType, originalNode.getDeclaredType(), currentPath, state);

		try {
			if (!addedToVisited) {
				List<Property> filteredChildren = new ArrayList<>();
				for (Property childProp : childProperties) {
					Class<?> childRawType = Types.normalizeRawType(childProp.getJvmType().getRawType());
					boolean isRecursiveChild =
						visitedTypes.contains(childRawType) || childRawType.isAssignableFrom(actualType);
					if (!isRecursiveChild) {
						filteredChildren.add(childProp);
					} else {
						PropertyNameResolver childNr = options.getPropertyNameResolver(childProp);
						String childName = childNr.resolve(childProp);
						PathExpression childPath = currentPath.child(childName);
						if (
							state.pathIndex.hasChildPaths(childPath)
								|| state.candidates.contains(childPath)
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

				JvmType childJvmType = childProperty.getJvmType();
				boolean childIsContainer = TypeMetadataResolver.isContainerType(childJvmType, state);

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
						childNode = new JavaNode(childProperty.getJvmType(), childName != null ? childName : "");
					}

					return assembleNode(childNode, state, currentContext, propertyPath, childPath, visitedTypes);
				},
				lazyPropertyPath,
				state.context.getGeneratorContext(),
				options.getGenerateUniqueMaxTries(),
				nullInject,
				state.context.getLoggingContext()
			);

			CombinableArbitrary<?> result;
			if (typeSpecificIntrospector != null) {
				ArbitraryIntrospectorResult introspectorResult = typeSpecificIntrospector.introspect(context);
				result = introspectorResult.getValue();
			} else {
				result = options.getDefaultArbitraryGenerator().generate(context);
			}

			Class<?> concreteRawType = Types.normalizeRawType(concreteProperty.getJvmType().getRawType());
			if (!concreteRawType.isPrimitive()) {
				result = result.injectNull(nullInject);
			}

			CombinableArbitrary<?> filtered = applyFilters(result, normalizedPath, concreteRawType, state);
			return applyCustomizers(filtered, normalizedPath, state, null);
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
		FixtureMonkeyOptions options = state.context.getOptions();

		JvmType nodeType = mapEntryNode.getConcreteType();
		boolean isNodeContainer = TypeMetadataResolver.isContainerType(nodeType, state);

		Class<?> nodeRawType = nodeType.getRawType();
		boolean addedToVisited = false;
		if (!isNodeContainer && visitedTypes.add(nodeRawType)) {
			addedToVisited = true;
		}

		try {
			List<JvmNode> treeChildren = state.assemblyTree.childrenOf(mapEntryNode);
			JvmNode keyNode = !treeChildren.isEmpty() ? treeChildren.get(0) : mapEntryNode.getKeyNode();
			JvmNode valueNode = treeChildren.size() > 1 ? treeChildren.get(1) : mapEntryNode.getValueNode();

			Property keyProperty = state.properties.generationPropertyOf(keyNode);
			Property valueProperty = state.properties.generationPropertyOf(valueNode);

			Property nodeProperty = state.properties.generationPropertyOf(mapEntryNode);
			PropertyNameResolver nameResolver =
				TypeMetadataResolver.resolveNameResolver(mapEntryNode, nodeProperty, state);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, mapEntryNode.getIndex());

			boolean isContainer = true;

			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				nodeProperty,
				mapEntryNode.getIndex(),
				null,
				isContainer,
				nameResolver
			);
			double nullInject = TypeMetadataResolver.resolveNullInjectGenerator(mapEntryNode, nodeProperty, state)
				.generate(
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
		FixtureMonkeyOptions options = state.context.getOptions();

		JvmType nodeType = mapLikeNode.getConcreteType();
		boolean isNodeContainer = TypeMetadataResolver.isContainerType(nodeType, state);

		Class<?> rawType = nodeType.getRawType();
		boolean addedToVisited = false;
		if (!isNodeContainer && visitedTypes.add(rawType)) {
			addedToVisited = true;
		}

		try {
			List<JvmNode> treeChildren = state.assemblyTree.childrenOf(mapLikeNode);
			JvmNode keyNode = treeChildren.size() > 0 ? treeChildren.get(0) : fallbackKeyNode;
			JvmNode valueNode = treeChildren.size() > 1 ? treeChildren.get(1) : fallbackValueNode;

			Property keyProperty = state.properties.generationPropertyOf(keyNode);
			Property valueProperty = state.properties.generationPropertyOf(valueNode);

			Property mapEntryProperty = state.properties.generationPropertyOf(mapLikeNode);

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

			if (state.context.getTraceContext().isEnabled()) {
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
		FixtureMonkeyOptions options = state.context.getOptions();

		Property keyProperty = state.properties.generationPropertyOf(keyNode);
		Property valueProperty = state.properties.generationPropertyOf(valueNode);

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

		JvmType currentType = node.getConcreteType();
		boolean isContainer = TypeMetadataResolver.isContainerType(currentType, state);

		Class<?> currentRawType = currentType.getRawType();
		boolean addedToVisited = false;
		if (!isContainer && visitedTypes.add(currentRawType)) {
			addedToVisited = true;
		}

		try {
			Property nodeProperty = state.properties.generationPropertyOf(node);
			TypeMetadataResolver.writeBackTypeMetadata(node, nodeProperty, state);
			PropertyNameResolver nameResolver =
				TypeMetadataResolver.resolveNameResolver(node, nodeProperty, state);

			ObjectProperty objectProperty = new ObjectProperty(nodeProperty, nameResolver, node.getIndex());

			ObjectPropertyGeneratorContext nullInjectContext = new ObjectPropertyGeneratorContext(
				nodeProperty,
				node.getIndex(),
				null,
				isContainer,
				nameResolver
			);
			double nullInject = TypeMetadataResolver.resolveNullInjectGenerator(node, nodeProperty, state)
				.generate(nullInjectContext);

			// Primitive slots cannot hold null; the introspector would throw IllegalArgumentException
			// when the array/setter writes the null produced by injectNull.
			if (currentRawType.isPrimitive()) {
				nullInject = 0.0;
			}

			if (state.scopes.isNotNullRequired(currentPath)) {
				nullInject = 0.0;
			}

			// A path matched by a wildcard candidate (e.g. $.list[*]) but exhausted by limit
			// still belongs to the root scope's targeted set; injecting null contradicts the intent.
			if (nullInject > 0 && state.candidates.matchesRootWildcard(currentPath)) {
				nullInject = 0.0;
			}

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

			List<JvmNode> children = deduplicateChildren(state.assemblyTree.childrenOf(node));
			List<ArbitraryProperty> childArbitraryProperties = new ArrayList<>();
			Map<ArbitraryProperty, JvmNode> nodeByArbitraryProperty = new HashMap<>();
			Map<ArbitraryProperty, PathExpression> pathByArbitraryProperty = new HashMap<>();

			for (JvmNode childNode : children) {
				PathExpression childPath = buildChildPath(currentPath, childNode, node, state);

				Property childProperty = state.properties.generationPropertyOf(childNode);
				TypeMetadataResolver.writeBackTypeMetadata(childNode, childProperty, state);
				PropertyNameResolver childNameResolver =
					TypeMetadataResolver.resolveNameResolver(childNode, childProperty, state);

				ObjectProperty childObjectProperty = new ObjectProperty(
					childProperty,
					childNameResolver,
					childNode.getIndex()
				);

				boolean childIsContainer = TypeMetadataResolver.isContainerType(childNode.getConcreteType(), state);

				ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
					childProperty,
					childNode.getIndex(),
					arbitraryProperty,
					childIsContainer,
					childNameResolver
				);
				double childNullInject =
					TypeMetadataResolver.resolveNullInjectGenerator(childNode, childProperty, state)
					.generate(
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

	private Map<String, JvmNode> buildConcreteChildrenMap(
		JvmType concreteType,
		JvmType declaredType,
		PathExpression path,
		AssemblyState state
	) {
		JvmNodeTree concreteTree = state.context.getPlan().getNodeTreeFactory().createConcreteNodeTree(
			concreteType,
			declaredType,
			path,
			state.assemblyTree.ancestorNodes(path)
		);
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

	private void resizeChildrenToValue(
		JvmNode containerNode,
		PathExpression containerPath,
		Object containerValue,
		AssemblyState state
	) {
		int valueSize = state.valueDecomposer.containerSize(containerValue);
		if (valueSize >= 0) {
			resizeChildren(containerNode, containerPath, valueSize, state);
		}
	}

	private static @Nullable ArbitraryIntrospector introspectorFor(
		Class<?> type,
		JvmNode node,
		PathExpression path,
		AssemblyState state
	) {
		ScopeSet scopeSet = state.context.getPlan().getScopeSet();
		Map<Class<?>, InstantiatorProcessResult> instantiators;
		if (scopeSet.hasScopedInstantiators()) {
			List<JvmNode> chain = state.assemblyTree.ancestorNodes(path);
			chain.add(node);
			instantiators =
				scopeSet.instantiatorsAt(type, ScopeChain.ofNodes(chain, state.properties::matchingPropertyOf));
		} else {
			instantiators = scopeSet.getGlobalInstantiators();
		}
		InstantiatorProcessResult instantiator = instantiators.get(type);
		return instantiator != null ? instantiator.getIntrospector() : null;
	}

	private void growChildrenTo(
		JvmNode containerNode,
		PathExpression containerPath,
		int elementCount,
		AssemblyState state
	) {
		if (elementCount > state.assemblyTree.childrenOf(containerNode).size()) {
			resizeChildren(containerNode, containerPath, elementCount, state);
		}
	}

	private void resizeChildren(
		JvmNode containerNode,
		PathExpression containerPath,
		int valueSize,
		AssemblyState state
	) {
		if (state.scopes.isRootSizedContainer(containerPath)) {
			return;
		}
		List<JvmNode> planned = state.assemblyTree.childrenOf(containerNode);
		if (planned.size() == valueSize) {
			return;
		}
		if (planned.size() > valueSize) {
			state.assemblyTree.resize(containerNode, new ArrayList<>(planned.subList(0, valueSize)));
			return;
		}
		JvmNodeTree generated = state.context.getPlan().getNodeTreeFactory().createContainerNodeTree(
			containerNode.getConcreteType(),
			containerNode.getDeclaredType(),
			containerPath,
			state.assemblyTree.ancestorNodes(containerPath),
			valueSize
		);
		if (generated == null) {
			return;
		}
		state.assemblyTree.attach(generated);
		List<JvmNode> generatedChildren = generated.getChildren(generated.getRootNode());
		if (generatedChildren.size() < valueSize) {
			return;
		}
		List<JvmNode> merged = new ArrayList<>(planned);
		merged.addAll(generatedChildren.subList(planned.size(), valueSize));
		state.assemblyTree.resize(containerNode, merged);
	}

	private @Nullable JvmNodeTree treeOfValueClass(
		JvmNode node,
		PathExpression currentPath,
		@Nullable ValueCandidate candidate,
		AssemblyState state
	) {
		if (candidate == null || candidate.value == null || candidate.value instanceof LazyValueHolder) {
			return null;
		}
		Class<?> nodeType = node.getConcreteType().getRawType();
		Class<?> valueType = candidate.value.getClass();
		boolean abstractNode =
			Modifier.isInterface(nodeType.getModifiers()) || Modifier.isAbstract(nodeType.getModifiers());
		if (!abstractNode || valueType == nodeType || !nodeType.isAssignableFrom(valueType)) {
			return null;
		}
		JvmNodeTree concreteTree = state.context.getPlan().getNodeTreeFactory().createConcreteNodeTree(
			new ReflectiveJvmType(valueType),
			node.getDeclaredType(),
			currentPath,
			state.assemblyTree.ancestorNodes(currentPath)
		);
		if (concreteTree != null) {
			state.assemblyTree.attach(concreteTree);
		}
		return concreteTree;
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
			state.context.getGeneratorContext(),
			state.context.getOptions().getGenerateUniqueMaxTries(),
			nullInject,
			state.context.getLoggingContext()
		);

		return state.context.getOptions().getDefaultArbitraryGenerator().generate(context).injectNull(nullInject);
	}

	private ArbitraryProperty buildChildArbitraryPropertyCached(
		JvmNode childNode,
		Property childProperty,
		ArbitraryProperty parentArbitraryProperty,
		AssemblyState state
	) {
		PropertyNameResolver childNameResolver =
			TypeMetadataResolver.resolveNameResolver(childNode, childProperty, state);

		ObjectProperty childObjectProperty = new ObjectProperty(childProperty, childNameResolver, childNode.getIndex());

		boolean childIsContainer = TypeMetadataResolver.isContainerType(childNode.getConcreteType(), state);

		ObjectPropertyGeneratorContext childNullInjectContext = new ObjectPropertyGeneratorContext(
			childProperty,
			childNode.getIndex(),
			parentArbitraryProperty,
			childIsContainer,
			childNameResolver
		);
		double childNullInject = TypeMetadataResolver.resolveNullInjectGenerator(childNode, childProperty, state)
			.generate(
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
		if (TypeMetadataResolver.isSingleElementWrapper(parentNode.getConcreteType())) {
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
			Property childProperty = state.properties.generationPropertyOf(childNode);
			PropertyNameResolver nameResolver =
				TypeMetadataResolver.resolveNameResolver(childNode, childProperty, state);
			String resolvedName = nameResolver.resolve(childProperty);

			return parentPath.child(resolvedName);
		} else {
			return parentPath;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private CombinableArbitrary<?> applyFilters(
		CombinableArbitrary<?> arbitrary,
		PathExpression path,
		Class<?> actualType,
		AssemblyState state
	) {
		List<AnalysisResult.PostConditionFilter> filters = filtersFor(path, state);
		if (filters.isEmpty()) {
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

	private static List<AnalysisResult.PostConditionFilter> filtersFor(PathExpression path, AssemblyState state) {
		if (!state.scopes.hasFilters()) {
			return Collections.emptyList();
		}
		return state.scopes.filtersAt(state.chainOf(path), path);
	}

	private CombinableArbitrary<?> applyCustomizers(
		CombinableArbitrary<?> arbitrary,
		PathExpression path,
		AssemblyState state,
		@Nullable WinningValueCandidate winner
	) {
		if (!state.scopes.hasCustomizers()) {
			return arbitrary;
		}
		ValueCandidate value = winner != null ? winner.candidate : state.candidates.at(path);
		int valueScopeDepth = winner != null ? winner.scopeDepth : PathMatcher.valueScopeDepth(path, state);

		CombinableArbitrary<?> result = arbitrary;
		for (PropertyCustomizer customizer
			: state.scopes.customizersAppliedAt(state.chainOf(path), path, value, valueScopeDepth)) {
			result = customizer.getCustomizer().apply(result);
		}
		return result;
	}

	private static void applyDecomposeResult(DecomposeResult result, AssemblyState state) {
		state.candidates.removeCandidatesUnder(result.getSubtreesToRemove());
		state.candidates.putDecomposedCandidates(result.getValuesToPut());
		for (PathExpression decomposedPath : result.getValuesToPut().keySet()) {
			state.context.getTraceContext().markDecomposedPath(decomposedPath.toExpression());
		}

		PathExpression limitPath = result.getLimitPath();
		if (limitPath != null) {
			state.limits.limitChildren(limitPath, result.getLimitValue());
		}
	}

	/**
	 * Applies wildcard overrides to container elements when a wildcard has a higher order
	 * than the container's own value. This handles the case where a container value is
	 * returned via earlyReturn (all decomposed elements match), but a wildcard like
	 * {@code $.values[*].values[*]} should override individual elements.
	 */
	private Object applyWildcardOverridesToContainer(
		Object container,
		PathExpression containerPath,
		DirectivePrecedence containerPrecedence,
		AssemblyState state
	) {
		if (container instanceof List) {
			List<?> list = (List<?>)container;
			List<@Nullable Object> result = null;
			for (int i = 0; i < list.size(); i++) {
				PathExpression elementPath = containerPath.index(i);
				for (Map.Entry<PathExpression, ValueCandidate> entry : state.candidates.getRootWildcardEntries()) {
					if (entry.getKey().matches(elementPath)
						&& entry.getValue().precedence.compareTo(containerPrecedence) > 0) {
						if (result == null) {
							result = new ArrayList<>(list);
						}
						result.set(i, LazyResolver.resolveLazyValueWithCache(entry.getValue().value, state));
						break;
					}
				}
			}
			return result != null ? result : container;
		} else if (container.getClass().isArray()) {
			int length = Array.getLength(container);
			boolean modified = false;
			for (int i = 0; i < length; i++) {
				PathExpression elementPath = containerPath.index(i);
				for (Map.Entry<PathExpression, ValueCandidate> entry : state.candidates.getRootWildcardEntries()) {
					if (entry.getKey().matches(elementPath)
						&& entry.getValue().precedence.compareTo(containerPrecedence) > 0) {
						if (!modified) {
							Class<?> componentType = container.getClass().getComponentType();
							if (componentType == null) {
								break;
							}
							Object copy = Array.newInstance(componentType, length);
							//noinspection SuspiciousSystemArraycopy
							System.arraycopy(container, 0, copy, 0, length);
							container = copy;
							modified = true;
						}
						Object resolved = LazyResolver.resolveLazyValueWithCache(entry.getValue().value, state);
						if (resolved != null) {
							Array.set(container, i, resolved);
						}
						break;
					}
				}
			}
		}
		return container;
	}

	private CombinableArbitrary<?> traceAndReturnValue(
		@Nullable Object value,
		String source,
		PathExpression currentPath,
		Class<?> currentRawType,
		boolean isCurrentTypeContainer,
		@Nullable ArbitraryGeneratorContext parentContext,
		JvmType currentType,
		AssemblyState state,
		@Nullable WinningValueCandidate winner
	) {
		if (state.context.getTraceContext().isEnabled()) {
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
		return wrapValueWithFiltersAndCustomizers(value, currentPath, currentRawType, state, winner);
	}

	private CombinableArbitrary<?> wrapValueWithFiltersAndCustomizers(
		@Nullable Object value,
		PathExpression path,
		Class<?> rawType,
		AssemblyState state
	) {
		return wrapValueWithFiltersAndCustomizers(value, path, rawType, state, null);
	}

	private CombinableArbitrary<?> wrapValueWithFiltersAndCustomizers(
		@Nullable Object value,
		PathExpression path,
		Class<?> rawType,
		AssemblyState state,
		@Nullable WinningValueCandidate winner
	) {
		CombinableArbitrary<?> result =
			value instanceof CombinableArbitrary ? (CombinableArbitrary<?>)value : CombinableArbitrary.from(value);
		result = applyFilters(result, path, rawType, state);
		result = applyCustomizers(result, path, state, winner);
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
		state.context.getTraceContext().recordAssemblyStep(
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
}
