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

import static com.navercorp.fixturemonkey.api.type.Types.nullSafe;

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

	public void setChildren(List<ObjectNode> newChildren) {
		this.children = newChildren;
		for (ObjectNode child : this.children) {
			child.setParent(this);
		}
	}

	public void setParent(@Nullable ObjectNode parent) {
		this.parent = parent;
	}

	@Override
	public boolean expand() {
		if (!this.traverseNode.expand() && this.children != null) {
			return false;
		}

		this.setChildren(
			nullSafe(this.traverseNode.getChildren()).asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
		return true;
	}

	@Override
	public void forceExpand() {
		this.traverseNode.forceExpand();
		this.setChildren(
			nullSafe(this.traverseNode.getChildren()).asList().stream()
				.map(it -> new ObjectNode(it, generateFixtureContext.newChildNodeContext()))
				.collect(Collectors.toList())
		);
	}

	@Override
	public TraverseNodeMetadata getMetadata() {
		return traverseNode.getMetadata();
	}

	@Nullable
	@Override
	public ObjectNode getParent() {
		return this.parent;
	}

	@Nullable
	@Override
	public ObjectNodeList getChildren() {
		if (children == null) {
			return null;
		}
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
	public TreeNodeManipulator getAppliedTreeNodeManipulator() {
		return this.traverseNode.getMetadata().getAppliedTreeNodeManipulator();
	}

	public GenerateFixtureContext getObjectNodeContext() {
		return generateFixtureContext;
	}
}
