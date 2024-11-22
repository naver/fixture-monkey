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

package com.navercorp.fixturemonkey.api.tree;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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
import com.navercorp.fixturemonkey.api.type.Types;

public final class DefaultTraverseNode implements TraverseNode, TraverseNodeMetadata {
	private static final ConcurrentLruCache<Property, List<Property>> CANDIDATE_CONCRETE_PROPERTIES_BY_PROPERTY =
		new ConcurrentLruCache<>(1024);

	private final RootProperty rootProperty;

	@Nullable
	private final Property resolvedParentProperty;
	private TypeDefinition resolvedTypeDefinition;
	private final TreeProperty treeProperty;
	private final TraverseContext traverseContext;

	@Nullable
	private TraverseNode parent = null;
	private List<TraverseNode> children;
	@Nullable
	private TypeDefinition expandedTypeDefinition = null;

	private double nullInject;
	private final List<TreeNodeManipulator> containerInfoManipulators = new ArrayList<>();

	private final LazyArbitrary<PropertyPath> lazyPropertyPath = LazyArbitrary.lazy(() -> {
		Property resolvedProperty = this.resolvedTypeDefinition.getResolvedProperty();
		if (parent == null) {
			return new PropertyPath(resolvedProperty, null, 1);
		}

		PropertyPath parentPropertyPath = parent.getMetadata().getLazyPropertyPath().getValue();
		return new PropertyPath(
			resolvedProperty,
			parentPropertyPath,
			parentPropertyPath.getDepth() + 1
		);
	});

	DefaultTraverseNode(
		RootProperty rootProperty,
		@Nullable Property resolvedParentProperty,
		TypeDefinition resolvedTypeDefinition,
		TreeProperty treeProperty,
		double nullInject,
		TraverseContext traverseContext
	) {
		this.rootProperty = rootProperty;
		this.resolvedParentProperty = resolvedParentProperty;
		this.resolvedTypeDefinition = resolvedTypeDefinition;
		this.treeProperty = treeProperty;
		this.nullInject = nullInject;
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

	public TypeDefinition getResolvedTypeDefinition() {
		return resolvedTypeDefinition;
	}

	public void setResolvedTypeDefinition(TypeDefinition typeDefinition) {
		this.resolvedTypeDefinition = typeDefinition;
	}

	public TreeProperty getTreeProperty() {
		return this.treeProperty;
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
	@Override
	public TraverseNodeList resolveChildren() {
		this.expand(this.resolvedTypeDefinition);
		return new TraverseNodeList(this.children);
	}

	@Override
	public NodeList getChildren() {
		return new TraverseNodeList(this.children);
	}

	// TODO:
	public void setChildren(List<TraverseNode> children) {
		this.children = children;
		for (TraverseNode child : this.children) {
			DefaultTraverseNode defaultTraverseNode = (DefaultTraverseNode)child;
			defaultTraverseNode.parent = this;
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

	public double getNullInject() {
		return nullInject;
	}

	public void setNullInject(double nullInject) {
		this.nullInject = nullInject;
	}

	@Nullable
	public TraverseNode getParent() {
		return parent;
	}

	public RootProperty getRootProperty() {
		return rootProperty;
	}

	public boolean manipulated() {
		return !this.containerInfoManipulators.isEmpty();
	}

	@Override
	public List<TreeNodeManipulator> getTreeNodeManipulators() {
		return this.containerInfoManipulators;
	}

	@Override
	public void addTreeNodeManipulator(TreeNodeManipulator treeNodeManipulator) {
		this.addContainerManipulator(treeNodeManipulator);
	}

	@Nullable
	@Override
	public TreeNodeManipulator getAppliedContainerInfoManipulator() {
		if (containerInfoManipulators.isEmpty()) {
			return null;
		}

		return containerInfoManipulators.get(containerInfoManipulators.size() - 1);
	}

	public void addContainerManipulator(TreeNodeManipulator containerInfoManipulator) {
		traverseContext.addContainerInfoManipulator(containerInfoManipulator);
		this.containerInfoManipulators.add(containerInfoManipulator);
	}

	public LazyArbitrary<PropertyPath> getLazyPropertyPath() {
		return lazyPropertyPath;
	}

	@Override
	public LazyArbitrary<Boolean> getLazyChildManipulated() {
		return LazyArbitrary.lazy(() -> false);
	}

	@Override
	public boolean expand(TypeDefinition typeDefinition) {
		if (this.expandedTypeDefinition == resolvedTypeDefinition) {
			return false;
		}

		List<TraverseNode> newChildren;
		if (this.getTreeProperty().isContainer()) {
			newChildren = expandContainerNode(typeDefinition, this.traverseContext).collect(Collectors.toList());
		} else {
			newChildren = this.generateChildrenNodes(
				typeDefinition.getResolvedProperty(),
				typeDefinition.getPropertyGenerator()
					.generateChildProperties(typeDefinition.getResolvedProperty()),
				this.nullInject,
				false,
				this.traverseContext
			);
		}

		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
		return true;
	}

	@Override
	public boolean expand() {
		if (this.expandedTypeDefinition == resolvedTypeDefinition) {
			return false;
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
							false,
							this.traverseContext
						).stream();
					}
				)
				.collect(Collectors.toList())
		);
		this.expandedTypeDefinition = resolvedTypeDefinition;
		return true;
	}

	@Override
	public void forceExpand() {
		List<TraverseNode> newChildren = this.getTreeProperty().getTypeDefinitions().stream()
			.flatMap(
				typeDefinition -> {
					if (this.getTreeProperty().isContainer()) {
						return this.expandContainerNode(
							typeDefinition,
							traverseContext.withParentProperties()
						);
					}

					return this.generateChildrenNodes(
						typeDefinition.getResolvedProperty(),
						typeDefinition.getPropertyGenerator()
							.generateChildProperties(typeDefinition.getResolvedProperty()),
						this.nullInject,
						false,
						traverseContext.withParentProperties()
					).stream();
				}
			).collect(Collectors.toList());

		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	@Override
	public TraverseNodeMetadata getMetadata() {
		return this;
	}

	public void forceExpand(TypeDefinition typeDefinition) {
		List<TraverseNode> newChildren;
		if (this.getTreeProperty().isContainer()) {
			newChildren = expandContainerNode(
				typeDefinition,
				this.traverseContext.withParentProperties()
			)
				.collect(Collectors.toList());
		} else {
			newChildren = this.generateChildrenNodes(
				typeDefinition.getResolvedProperty(),
				typeDefinition.getPropertyGenerator()
					.generateChildProperties(typeDefinition.getResolvedProperty()),
				this.nullInject,
				false,
				this.traverseContext.withParentProperties()
			);
		}

		this.setChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	private Stream<TraverseNode> expandContainerNode(TypeDefinition typeDefinition, TraverseContext traverseContext) {
		TreeNodeManipulator appliedContainerInfoManipulator =
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
			true,
			traverseContext
		).stream();
	}

	public static DefaultTraverseNode generateRootNode(
		RootProperty rootProperty,
		TraverseContext traverseContext
	) {
		return DefaultTraverseNode.generateObjectNode(
			rootProperty,
			null,
			rootProperty,
			null,
			0.0d,
			traverseContext
		);
	}

	static DefaultTraverseNode generateObjectNode(
		RootProperty rootProperty,
		@Nullable Property resolvedParentProperty,
		Property property,
		@Nullable Integer propertySequence,
		double parentNullInject,
		TraverseContext context
	) {
		FixtureMonkeyOptions fixtureMonkeyOptions = context.getMonkeyContext().getFixtureMonkeyOptions();
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

		TreeNodeManipulator appliedContainerInfoManipulator = resolveAppliedContainerInfoManipulator(
			container,
			context.getTreeManipulators(),
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

		DefaultTraverseNode newObjectNode = new DefaultTraverseNode(
			rootProperty,
			resolvedParentProperty,
			new CompositeTypeDefinition(typeDefinitions).getResolvedTypeDefinition(),
			treeProperty,
			nullInject,
			nextTraverseContext
		);

		if (appliedContainerInfoManipulator != null) {
			newObjectNode.getMetadata().addTreeNodeManipulator(appliedContainerInfoManipulator);
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

	private List<TraverseNode> generateChildrenNodes(
		Property resolvedParentProperty,
		List<Property> childProperties,
		double parentNullInject,
		boolean container,
		TraverseContext context
	) {
		List<TraverseNode> children = new ArrayList<>();

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
			boolean childNodeAlreadyExists = this.children != null && this.children.size() > sequence;
			if (container && childNodeAlreadyExists) {
				TraverseNode currentChildNode = this.children.get(sequence);
				if (currentChildNode.getMetadata().manipulated()
					|| currentChildNode.getMetadata().getLazyChildManipulated().getValue()) {
					children.add(currentChildNode);
					continue;
				}
			}
			Property childProperty = childProperties.get(sequence);

			if (context.isTraversed(childProperty)
				&& !(resolvedParentProperty instanceof MapEntryElementProperty)) {
				continue;
			}

			TraverseNode childNode = generateObjectNode(
				rootProperty,
				resolvedParentProperty,
				childProperty,
				sequence,
				parentNullInject,
				context
			);
			children.add(childNode);
		}
		return children;
	}

	@Nullable
	private static TreeNodeManipulator resolveAppliedContainerInfoManipulator(
		boolean container,
		List<TreeNodeManipulator> containerInfoManipulators,
		List<ObjectProperty> objectProperties
	) {
		if (!container || objectProperties.isEmpty()
			|| !(objectProperties.get(0).getProperty() instanceof RootProperty)) {
			return null;
		}

		TreeNodeManipulator appliedContainerInfoManipulator = null;
		for (TreeNodeManipulator containerInfoManipulator : containerInfoManipulators) {
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
