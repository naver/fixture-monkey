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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;
import com.navercorp.fixturemonkey.api.tree.TraverseNode;
import com.navercorp.fixturemonkey.api.tree.TraverseNodeMetadata;
import com.navercorp.fixturemonkey.api.tree.TreeNodeManipulator;
import com.navercorp.fixturemonkey.api.tree.TreeProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectNode implements TraverseNode, TraverseNodeMetadata {
	private final TraverseNode traverseNode;
	private final GenerateFixtureContext generateFixtureContext;

	@Nullable
	private ObjectNode parent;
	private List<ObjectNode> children;

	public ObjectNode(TraverseNode traverseNode, GenerateFixtureContext generateFixtureContext) {
		this.traverseNode = traverseNode;
		this.generateFixtureContext = generateFixtureContext;
		this.generateFixtureContext.setTraverseNode(this);
	}

	public void setChildren(List<ObjectNode> children) {
		this.children = children;
		for (ObjectNode child : this.children) {
			child.setParent(this);
		}
	}

	public void setParent(@Nullable ObjectNode parent) {
		this.parent = parent;
	}

	@Override
	public boolean expand() {
		if (!this.traverseNode.expand()) {
			return false;
		}
		this.setChildren(
			this.traverseNode.getChildren().asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
		return true;
	}

	@Override
	public boolean expand(TypeDefinition typeDefinition) {
		if (!this.traverseNode.expand(typeDefinition)) {
			return false;
		}
		this.setChildren(
			this.traverseNode.getChildren().asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
		return true;
	}

	@Override
	public void forceExpand() {
		this.traverseNode.forceExpand();
		this.setChildren(
			this.traverseNode.getChildren().asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
	}

	@Override
	public void forceExpand(TypeDefinition typeDefinition) {
		this.traverseNode.forceExpand(typeDefinition);
		this.setChildren(
			this.traverseNode.getChildren().asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
	}

	@Override
	public TraverseNodeMetadata getMetadata() {
		return traverseNode.getMetadata();
	}

	@Override
	public ObjectNodeList resolveChildren() {
		if (this.traverseNode.expand(this.traverseNode.getMetadata().getResolvedTypeDefinition())) {
			this.setChildren(
				this.traverseNode.getChildren().asList().stream()
					.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
					.collect(Collectors.toList())
			);
		}
		return new ObjectNodeList(this.getChildren().asList());
	}

	@Nullable
	@Override
	public ObjectNode getParent() {
		return this.parent;
	}

	public ObjectNodeList getChildren() {
		return new ObjectNodeList(children);
	}

	public ArbitraryProperty getArbitraryProperty() {
		return this.getMetadata().getTreeProperty().toArbitraryProperty(getMetadata().getNullInject());
	}

	@Override
	public RootProperty getRootProperty() {
		return this.getMetadata().getRootProperty();
	}

	@Nullable
	@Override
	public Property getResolvedParentProperty() {
		return this.getMetadata().getResolvedParentProperty();
	}

	public TypeDefinition getResolvedTypeDefinition() {
		return this.getMetadata().getResolvedTypeDefinition();
	}

	public Property getResolvedProperty() {
		return this.getMetadata().getResolvedTypeDefinition().getResolvedProperty();
	}

	public Property getOriginalProperty() {
		return this.getMetadata().getOriginalProperty();
	}

	public void setNullInject(double nullInject) {
		this.getMetadata().setNullInject(nullInject);
	}

	public TreeProperty getTreeProperty() {
		return this.getMetadata().getTreeProperty();
	}

	@Override
	public double getNullInject() {
		return this.getMetadata().getNullInject();
	}

	@Override
	public LazyArbitrary<PropertyPath> getLazyPropertyPath() {
		return this.getMetadata().getLazyPropertyPath();
	}

	@Override
	public LazyArbitrary<Boolean> getLazyChildManipulated() {
		return generateFixtureContext.getChildManipulated();
	}

	@Override
	public boolean manipulated() {
		return this.getMetadata().manipulated() && getObjectNodeContext().manipulated();
	}

	@Override
	public List<TreeNodeManipulator> getTreeNodeManipulators() {
		return this.traverseNode.getMetadata().getTreeNodeManipulators();
	}

	@Override
	public void addTreeNodeManipulator(TreeNodeManipulator treeNodeManipulator) {
		this.traverseNode.getMetadata().addTreeNodeManipulator(treeNodeManipulator);
	}

	public void setResolvedTypeDefinition(TypeDefinition typeDefinition) {
		this.traverseNode.getMetadata().setResolvedTypeDefinition(typeDefinition);
	}

	@Nullable
	public TreeNodeManipulator getAppliedContainerInfoManipulator() {
		return this.traverseNode.getMetadata().getAppliedContainerInfoManipulator();
	}

	public GenerateFixtureContext getObjectNodeContext() {
		return generateFixtureContext;
	}
}
