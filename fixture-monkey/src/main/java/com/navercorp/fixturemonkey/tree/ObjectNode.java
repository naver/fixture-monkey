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

package com.navercorp.fixturemonkey.tree;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.CompositeTypeDefinition;
import com.navercorp.fixturemonkey.api.property.DefaultCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.DefaultTypeDefinition;
import com.navercorp.fixturemonkey.api.property.ElementPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.LazyPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;
import com.navercorp.fixturemonkey.api.tree.ObjectTreeNode;
import com.navercorp.fixturemonkey.api.tree.TreeProperty;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectNode implements ObjectTreeNode {
	private static final ConcurrentLruCache<Property, List<Property>> CANDIDATE_CONCRETE_PROPERTIES_BY_PROPERTY =
		new ConcurrentLruCache<>(1024);

	@Nullable
	private final Property resolvedParentProperty;
	private TypeDefinition resolvedTypeDefinition;
	private TreeProperty treeProperty;
	@Nullable
	private ObjectNode parent = null;
	private List<ObjectNode> children;
	@Nullable
	private CombinableArbitrary<?> arbitrary;
	private double nullInject;
	@Nullable
	private TypeDefinition expandedTypeDefinition = null;

	private final List<NodeManipulator> manipulators = new ArrayList<>();
	private final List<ContainerInfoManipulator> containerInfoManipulators = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private final List<Predicate> arbitraryFilters = new ArrayList<>();
	private final List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> arbitraryCustomizers =
		new ArrayList<>();

	private final LazyArbitrary<Boolean> childNotCacheable = LazyArbitrary.lazy(() -> {
		for (ObjectNode child : this.resolveChildren()) {
			if (child.manipulated() || child.childNotCacheable.getValue() || child.treeProperty.isContainer()) {
				return true;
			}
		}

		return false;
	});
	private final LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> {
		Property resolvedProperty = this.resolvedTypeDefinition.getResolvedProperty();
		if (parent == null) {
			return new PropertyPath(resolvedProperty, null, 1);
		}

		PropertyPath parentPropertyPath = parent.getLazyPropertyPath().getValue();
		return new PropertyPath(
			resolvedProperty,
			parentPropertyPath,
			parentPropertyPath.getDepth() + 1
		);
	});

	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final TraverseContext traverseContext;

	ObjectNode(
		@Nullable Property resolvedParentProperty,
		TypeDefinition resolvedTypeDefinition,
		TreeProperty treeProperty,
		double nullInject,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		TraverseContext traverseContext
	) {
		this.resolvedParentProperty = resolvedParentProperty;
		this.resolvedTypeDefinition = resolvedTypeDefinition;
		this.treeProperty = treeProperty;
		this.nullInject = nullInject;
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.traverseContext = traverseContext;
	}

	@Nullable
	public Property getResolvedParentProperty() {
		return resolvedParentProperty;
	}

	/**
	 * The resolved property refers to the concrete type of {@link #getOriginalProperty()},
	 * it can't be an abstract class or interface unless it's an anonymous object.
	 *
	 * @return the resolvedProperty
	 */
	public Property getResolvedProperty() {
		return this.resolvedTypeDefinition.getResolvedProperty();
	}

	public void setResolvedTypeDefinition(TypeDefinition typeDefinition) {
		this.resolvedTypeDefinition = typeDefinition;
	}

	public TreeProperty getTreeProperty() {
		return this.treeProperty;
	}

	public void setTreeProperty(TreeProperty treeProperty) {
		this.treeProperty = treeProperty;
	}

	/**
	 * The original property refers to the class-time property, which means it is the type in a class file.
	 * It can be an abstract class or interface, unlike the {@link #getResolvedProperty()}
	 *
	 * @return the original property
	 */
	public Property getOriginalProperty() {
		return this.getTreeProperty().getObjectProperty().getProperty();
	}

	/**
	 * expands by the resolved property and gets the expanded child nodes.
	 * It may not return empty if {@link TypeDefinition#getPropertyGenerator()} generates the child nodes.
	 *
	 * @return expanded child node.
	 */
	public List<ObjectNode> resolveChildren() {
		this.expand(this.resolvedTypeDefinition);
		return this.children;
	}

	/**
	 * Gets the expanded child nodes, it may be return empty if not expanded.
	 *
	 * @return already expanded child nodes
	 */
	public List<ObjectNode> getChildren() {
		return this.children;
	}

	public void setChildren(List<ObjectNode> children) {
		this.children = children;
		for (ObjectNode child : this.children) {
			child.parent = this;
		}
	}

	/**
	 * The ArbitraryProperty remains for backward compatibility with {@link ArbitraryGeneratorContext}.
	 *
	 * @return the ArbitraryProperty transformed by ObjectNode's {@link #treeProperty}
	 */
	public ArbitraryProperty getArbitraryProperty() {
		return this.treeProperty.toArbitraryProperty(this.nullInject);
	}

	@Nullable
	public CombinableArbitrary<?> getArbitrary() {
		return this.arbitrary;
	}

	public void setArbitrary(@Nullable CombinableArbitrary<?> arbitrary) {
		this.arbitrary = arbitrary;
	}

	public void addContainerManipulator(ContainerInfoManipulator containerInfoManipulator) {
		this.traverseContext.addContainerInfoManipulator(containerInfoManipulator);
		this.containerInfoManipulators.add(containerInfoManipulator);
	}

	public void addManipulator(NodeManipulator nodeManipulator) {
		this.manipulators.add(nodeManipulator);
	}

	@SuppressWarnings("rawtypes")
	public List<Predicate> getArbitraryFilters() {
		return arbitraryFilters;
	}

	@SuppressWarnings("rawtypes")
	public void addArbitraryFilter(Predicate filter) {
		this.arbitraryFilters.add(filter);
	}

	public void addGeneratedArbitraryCustomizer(
		Function<CombinableArbitrary<?>, CombinableArbitrary<?>> arbitraryCustomizer
	) {
		this.arbitraryCustomizers.add(arbitraryCustomizer);
	}

	public List<Function<CombinableArbitrary<?>, CombinableArbitrary<?>>> getGeneratedArbitraryCustomizers() {
		return arbitraryCustomizers;
	}

	public void addArbitraryCustomizer(Function<CombinableArbitrary<?>, CombinableArbitrary<?>> arbitraryCustomizer) {
		this.arbitraryCustomizers.add(arbitraryCustomizer);
	}

	public double getNullInject() {
		return nullInject;
	}

	public void setNullInject(double nullInject) {
		this.nullInject = nullInject;
	}

	public boolean manipulated() {
		return !manipulators.isEmpty() || !containerInfoManipulators.isEmpty();
	}

	public boolean cacheable() {
		return !manipulated() && !treeProperty.isContainer() && !childNotCacheable.getValue();
	}

	@Nullable
	public ObjectNode getParent() {
		return parent;
	}

	@Nullable
	public ContainerInfoManipulator getAppliedContainerInfoManipulator() {
		if (containerInfoManipulators.isEmpty()) {
			return null;
		}

		return containerInfoManipulators.get(containerInfoManipulators.size() - 1);
	}

	public LazyArbitrary<PropertyPath> getLazyPropertyPath() {
		return lazyPropertyPath;
	}

	public void expand(TypeDefinition typeDefinition) {
		if (this.expandedTypeDefinition == resolvedTypeDefinition) {
			return;
		}

		List<ObjectNode> newChildren = this.getTreeProperty().getTypeDefinitions().stream()
			.filter(it -> it.getResolvedProperty().equals(typeDefinition.getResolvedProperty())
				&& it.getPropertyGenerator() == typeDefinition.getPropertyGenerator())
			.flatMap(
				it -> {
					if (this.getTreeProperty().isContainer()) {
						return expandContainerNode(it, this.traverseContext);
					}

					return this.generateChildrenNodes(
						it.getResolvedProperty(),
						it.getPropertyGenerator()
							.generateChildProperties(it.getResolvedProperty()),
						this.nullInject,
						this.traverseContext
					).stream();
				}
			)
			.collect(Collectors.toList());
		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	@Override
	public void expand() {
		if (this.expandedTypeDefinition == resolvedTypeDefinition) {
			return;
		}

		this.setChildren(
			this.getTreeProperty().getTypeDefinitions().stream()
				.flatMap(
					typeDefinition -> {
						if (this.getTreeProperty().isContainer()) {
							return expandContainerNode(typeDefinition, this.traverseContext);
						}

						return this.generateChildrenNodes(
							typeDefinition.getResolvedProperty(),
							typeDefinition.getPropertyGenerator()
								.generateChildProperties(typeDefinition.getResolvedProperty()),
							this.nullInject,
							this.traverseContext
						).stream();
					}
				)
				.collect(Collectors.toList())
		);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	public void forceExpand(TypeDefinition typeDefinition) {
		List<ObjectNode> newChildren = this.getTreeProperty().getTypeDefinitions().stream()
			.filter(it -> it.getResolvedProperty().equals(typeDefinition.getResolvedProperty())
				&& it.getPropertyGenerator() == typeDefinition.getPropertyGenerator())
			.flatMap(
				it -> {
					if (this.getTreeProperty().isContainer()) {
						return this.expandContainerNode(it, traverseContext.withNotRecursiveTreeProperties());
					}

					return this.generateChildrenNodes(
						it.getResolvedProperty(),
						it.getPropertyGenerator()
							.generateChildProperties(it.getResolvedProperty()),
						this.nullInject,
						traverseContext.withNotRecursiveTreeProperties()
					).stream();
				}
			).collect(Collectors.toList());

		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	@Override
	public void forceExpand() {
		List<ObjectNode> newChildren = this.getTreeProperty().getTypeDefinitions().stream()
			.flatMap(
				typeDefinition -> {
					if (this.getTreeProperty().isContainer()) {
						return this.expandContainerNode(
							typeDefinition,
							traverseContext.withNotRecursiveTreeProperties()
						);
					}

					return this.generateChildrenNodes(
						typeDefinition.getResolvedProperty(),
						typeDefinition.getPropertyGenerator()
							.generateChildProperties(typeDefinition.getResolvedProperty()),
						this.nullInject,
						traverseContext.withNotRecursiveTreeProperties()
					).stream();
				}
			).collect(Collectors.toList());

		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	private Stream<ObjectNode> expandContainerNode(TypeDefinition typeDefinition, TraverseContext traverseContext) {
		ContainerInfoManipulator appliedContainerInfoManipulator =
			this.getAppliedContainerInfoManipulator();

		ArbitraryContainerInfo containerInfo = appliedContainerInfoManipulator != null
			? appliedContainerInfoManipulator.getContainerInfo()
			: null;

		PropertyGenerator propertyGenerator = typeDefinition.getPropertyGenerator();
		if (propertyGenerator instanceof LazyPropertyGenerator) {
			propertyGenerator = ((LazyPropertyGenerator)propertyGenerator).getDelegate();
		}

		if (propertyGenerator instanceof ElementPropertyGenerator) {
			((ElementPropertyGenerator)propertyGenerator).updateContainerInfo(containerInfo);
		}

		List<Property> elementProperties = propertyGenerator.generateChildProperties(
			typeDefinition.getResolvedProperty()
		);

		return this.generateChildrenNodes(
			typeDefinition.getResolvedProperty(),
			elementProperties,
			this.nullInject,
			traverseContext
		).stream();
	}

	static ObjectNode generateRootNode(
		RootProperty rootProperty,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		TraverseContext traverseContext
	) {
		return ObjectNode.generateObjectNode(
			null,
			rootProperty,
			null,
			0.0d,
			fixtureMonkeyOptions,
			traverseContext
		);
	}

	static ObjectNode generateObjectNode(
		@Nullable Property resolvedParentProperty,
		Property property,
		@Nullable Integer propertySequence,
		double parentNullInject,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		TraverseContext context
	) {
		ContainerPropertyGenerator containerPropertyGenerator =
			fixtureMonkeyOptions.getContainerPropertyGenerator(property);
		boolean container = containerPropertyGenerator != null;

		ObjectPropertyGenerator objectPropertyGenerator =
			fixtureMonkeyOptions.getObjectPropertyGenerator(property);

		TreeProperty parentTreeProperty = context.getLastTreeProperty();

		ArbitraryProperty parentArbitraryProperty = parentTreeProperty != null
			? parentTreeProperty.toArbitraryProperty(parentNullInject)
			: null;

		ObjectPropertyGeneratorContext objectPropertyGeneratorContext = new ObjectPropertyGeneratorContext(
			property,
			resolveIndex(
				resolvedParentProperty,
				parentTreeProperty,
				propertySequence,
				fixtureMonkeyOptions
			),
			parentArbitraryProperty,
			container,
			fixtureMonkeyOptions.getPropertyNameResolver(property)
		);

		ObjectProperty objectProperty = objectPropertyGenerator.generate(objectPropertyGeneratorContext);

		List<Property> candidateProperties = resolveCandidateProperties(
			property,
			fixtureMonkeyOptions
		);

		List<ObjectProperty> objectProperties =
			context.getTreeProperties().stream()
				.map(TreeProperty::getObjectProperty).collect(Collectors.toList());
		objectProperties.add(objectProperty);

		ContainerInfoManipulator appliedContainerInfoManipulator = resolveAppliedContainerInfoManipulator(
			container,
			context.getContainerInfoManipulators(),
			objectProperties
		);

		List<TypeDefinition> typeDefinitions = candidateProperties.stream()
			.map(concreteProperty -> {
				if (!container) {
					LazyPropertyGenerator lazyPropertyGenerator =
						new LazyPropertyGenerator(
							getPropertyGenerator(context.getPropertyConfigurers(), fixtureMonkeyOptions)
						);

					return new DefaultTypeDefinition(
						concreteProperty,
						lazyPropertyGenerator
					);
				}

				PropertyGenerator containerElementPropertyGenerator = new ElementPropertyGenerator(
					property,
					containerPropertyGenerator,
					fixtureMonkeyOptions.getArbitraryContainerInfoGenerator(property),
					null
				);

				LazyPropertyGenerator lazyPropertyGenerator =
					new LazyPropertyGenerator(containerElementPropertyGenerator);

				return new DefaultTypeDefinition(
					concreteProperty,
					lazyPropertyGenerator
				);
			})
			.collect(Collectors.toList());

		double nullInject = fixtureMonkeyOptions.getNullInjectGenerator(property)
			.generate(objectPropertyGeneratorContext);

		TreeProperty treeProperty = new TreeProperty(
			objectProperty,
			container,
			typeDefinitions
		);

		TraverseContext nextTraverseContext = context.appendArbitraryProperty(treeProperty);

		ObjectNode newObjectNode = new ObjectNode(
			resolvedParentProperty,
			new CompositeTypeDefinition(typeDefinitions).getResolvedTypeDefinition(),
			treeProperty,
			nullInject,
			fixtureMonkeyOptions,
			nextTraverseContext
		);

		if (appliedContainerInfoManipulator != null) {
			newObjectNode.addContainerManipulator(appliedContainerInfoManipulator);
		}
		return newObjectNode;
	}

	@Nullable
	private static Integer resolveIndex(
		@Nullable Property resolvedParentProperty,
		@Nullable TreeProperty parentTreeProperty,
		@Nullable Integer propertySequence,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		if (resolvedParentProperty == null || parentTreeProperty == null) {
			return null;
		}

		boolean parentContainer =
			fixtureMonkeyOptions.getContainerPropertyGenerator(resolvedParentProperty) != null;
		if (!parentContainer) {
			return null;
		}

		if (propertySequence == null) {
			return null;
		}

		int index = propertySequence;
		if (parentTreeProperty.getObjectProperty().getProperty() instanceof MapEntryElementProperty) {
			index /= 2;
		}
		return index;
	}

	private List<ObjectNode> generateChildrenNodes(
		Property resolvedParentProperty,
		List<Property> childProperties,
		double parentNullInject,
		TraverseContext context
	) {
		List<ObjectNode> children = new ArrayList<>();

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
			Property childProperty = childProperties.get(sequence);

			if (context.isTraversed(childProperty)
				&& !(resolvedParentProperty instanceof MapEntryElementProperty)) {
				continue;
			}

			ObjectNode childNode = generateObjectNode(
				resolvedParentProperty,
				childProperty,
				sequence,
				parentNullInject,
				this.fixtureMonkeyOptions,
				context
			);
			children.add(childNode);
		}
		return children;
	}

	@Nullable
	private static ContainerInfoManipulator resolveAppliedContainerInfoManipulator(
		boolean container,
		List<ContainerInfoManipulator> containerInfoManipulators,
		List<ObjectProperty> objectProperties
	) {
		if (!container) {
			return null;
		}

		ContainerInfoManipulator appliedContainerInfoManipulator = null;
		for (ContainerInfoManipulator containerInfoManipulator : containerInfoManipulators) {
			if (containerInfoManipulator.isMatch(objectProperties)) {
				appliedContainerInfoManipulator = containerInfoManipulator;
			}
		}
		return appliedContainerInfoManipulator;
	}

	private static PropertyGenerator getPropertyGenerator(
		Map<Class<?>, List<Property>> propertyConfigurers,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		PropertyGenerator resolvedPropertyGenerator = property -> {
			Class<?> type = Types.getActualType(property.getType());
			List<Property> propertyConfigurer = propertyConfigurers.get(type);
			if (propertyConfigurer != null) {
				return propertyConfigurer;
			}

			PropertyGenerator propertyGenerator = fixtureMonkeyOptions.getOptionalPropertyGenerator(property);
			if (propertyGenerator != null) {
				return propertyGenerator.generateChildProperties(property);
			}

			ArbitraryGenerator defaultArbitraryGenerator = fixtureMonkeyOptions.getDefaultArbitraryGenerator();

			PropertyGenerator defaultArbitraryGeneratorPropertyGenerator =
				defaultArbitraryGenerator.getRequiredPropertyGenerator(property);

			if (defaultArbitraryGeneratorPropertyGenerator != null) {
				return defaultArbitraryGeneratorPropertyGenerator.generateChildProperties(property);
			}

			return fixtureMonkeyOptions.getDefaultPropertyGenerator().generateChildProperties(property);
		};

		return new LazyPropertyGenerator(resolvedPropertyGenerator);
	}

	private static List<Property> resolveCandidateProperties(
		Property property,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		CandidateConcretePropertyResolver candidateConcretePropertyResolver =
			fixtureMonkeyOptions.getCandidateConcretePropertyResolver(property);

		if (candidateConcretePropertyResolver == null) {
			return DefaultCandidateConcretePropertyResolver.INSTANCE.resolve(property);
		}

		return CANDIDATE_CONCRETE_PROPERTIES_BY_PROPERTY.computeIfAbsent(
			property,
			p -> {
				List<Property> resolvedCandidateProperties = new ArrayList<>();
				List<Property> candidateProperties = candidateConcretePropertyResolver.resolve(p);
				for (Property candidateProperty : candidateProperties) {
					// compares by type until a specific property implementation is created for the generic type.
					Type candidateType = candidateProperty.getType();

					if (p.getType().equals(candidateType)) {
						// prevents infinite recursion
						resolvedCandidateProperties.addAll(
							DefaultCandidateConcretePropertyResolver.INSTANCE.resolve(p)
						);
						continue;
					}
					resolvedCandidateProperties.addAll(
						resolveCandidateProperties(
							candidateProperty,
							fixtureMonkeyOptions
						)
					);
				}
				return resolvedCandidateProperties;
			}
		);
	}
}
