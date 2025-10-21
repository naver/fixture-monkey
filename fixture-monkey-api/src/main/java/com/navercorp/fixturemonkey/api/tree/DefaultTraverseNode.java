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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.CompositeTypeDefinition;
import com.navercorp.fixturemonkey.api.property.DefaultTypeDefinition;
import com.navercorp.fixturemonkey.api.property.ElementPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.LazyPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;

@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public final class DefaultTraverseNode implements TraverseNode, TraverseNodeMetadata {
	private final TreeRootProperty rootProperty;

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
		TreeRootProperty rootProperty,
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

	@Nullable
	@Override
	public NodeList getChildren() {
		if (children == null) {
			return null;
		}
		return new TraverseNodeList(this.children);
	}

	public void setMergedChildren(List<TraverseNode> children) {
		this.children = mergeWithNewChildren(children);
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

	public TreeRootProperty getRootProperty() {
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
	public TreeNodeManipulator getAppliedTreeNodeManipulator() {
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
	public boolean expand() {
		if (this.expandedTypeDefinition != null) {
			return false;
		}

		this.setMergedChildren(
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
						traverseContext.withParentProperties()
					).stream();
				}
			).collect(Collectors.toList());

		this.setMergedChildren(newChildren);
		this.expandedTypeDefinition = resolvedTypeDefinition;
	}

	@Override
	public void forceExpand(TypeDefinition typeDefinition) {
		List<TraverseNode> children;
		if (this.getTreeProperty().isContainer()) {
			children = this.expandContainerNode(
				typeDefinition,
				traverseContext.withParentProperties()
			).collect(Collectors.toList());
		} else {
			children = this.generateChildrenNodes(
				typeDefinition.getResolvedProperty(),
				typeDefinition.getPropertyGenerator()
					.generateChildProperties(typeDefinition.getResolvedProperty()),
				this.nullInject,
				traverseContext.withParentProperties()
			);
		}
		this.setMergedChildren(children);
		this.expandedTypeDefinition = typeDefinition;
	}

	@Override
	public TraverseNodeMetadata getMetadata() {
		return this;
	}

	private Stream<TraverseNode> expandContainerNode(TypeDefinition typeDefinition, TraverseContext traverseContext) {
		TreeNodeManipulator appliedContainerInfoManipulator =
			this.getAppliedTreeNodeManipulator();

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

	public static DefaultTraverseNode generateRootNode(
		TreeRootProperty rootProperty,
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
		TreeRootProperty rootProperty,
		@Nullable Property resolvedParentProperty,
		Property property,
		@Nullable Integer propertySequence,
		double parentNullInject,
		TraverseContext context
	) {
		ContainerPropertyGenerator containerPropertyGenerator = context.getContainerPropertyGenerator(property);
		boolean container = containerPropertyGenerator != null;

		ObjectPropertyGenerator objectPropertyGenerator = context.getObjectPropertyGenerator(property);

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
				context
			),
			parentArbitraryProperty,
			container,
			context.getPropertyNameResolver(property)
		);

		ObjectProperty objectProperty = objectPropertyGenerator.generate(objectPropertyGeneratorContext);

		List<Property> candidateProperties = context.resolveCandidateProperties(property);

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
					LazyPropertyGenerator lazyPropertyGenerator = context.getResolvedPropertyGenerator();

					return new DefaultTypeDefinition(
						concreteProperty,
						lazyPropertyGenerator
					);
				}

				PropertyGenerator containerElementPropertyGenerator = new ElementPropertyGenerator(
					property,
					containerPropertyGenerator,
					context.getArbitraryContainerInfoGenerator(property),
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

		double nullInject = context.getNullInjectGenerator(property)
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
		TraverseContext context
	) {
		if (resolvedParentProperty == null || parentTreeProperty == null) {
			return null;
		}

		boolean parentContainer =
			context.getContainerPropertyGenerator(resolvedParentProperty) != null;
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
		TraverseContext context
	) {
		List<TraverseNode> children = new ArrayList<>();

		for (int sequence = 0; sequence < childProperties.size(); sequence++) {
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
			|| !(objectProperties.get(0).getProperty() instanceof TreeRootProperty)) {
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

	private List<TraverseNode> mergeWithNewChildren(List<TraverseNode> newChildren) {
		if (this.children == null) {
			return newChildren;
		}

		boolean shrinkChildNodes = this.children.size() > newChildren.size();
		if (shrinkChildNodes) {
			return this.children.subList(0, newChildren.size());
		}

		boolean expandChildNodes = this.children.size() < newChildren.size();
		if (expandChildNodes) {
			Map<ObjectProperty, TraverseNode> existingNodesByObjectProperty = this.children.stream()
				.collect(toMap(it -> it.getMetadata().getTreeProperty().getObjectProperty(), Function.identity()));

			List<TraverseNode> concatNewChildren = new ArrayList<>();
			for (TraverseNode newChild : newChildren) {
				TraverseNode existingNode =
					existingNodesByObjectProperty.get(newChild.getMetadata().getTreeProperty().getObjectProperty());
				if (existingNode != null) {
					concatNewChildren.add(existingNode);
				} else {
					concatNewChildren.add(newChild);
				}
			}
			return concatNewChildren;
		}
		return this.children;
	}
}
