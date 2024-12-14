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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.tree.DefaultTraverseNode;
import com.navercorp.fixturemonkey.api.tree.TraverseContext;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ObjectTree {
	private final ObjectNode rootNode;
	private final ObjectTreeMetadata metadata;
	private final GenerateFixtureContext generateFixtureContext;

	public ObjectTree(
		TreeRootProperty rootProperty,
		GenerateFixtureContext generateFixtureContext,
		TraverseContext traverseContext
	) {
		this.rootNode = new ObjectNode(
			DefaultTraverseNode.generateRootNode(rootProperty, traverseContext),
			generateFixtureContext
		);
		MetadataCollector metadataCollector = new MetadataCollector(rootNode);
		this.metadata = metadataCollector.collect();
		this.generateFixtureContext = this.rootNode.getObjectNodeContext();
	}

	public ObjectTreeMetadata getMetadata() {
		return metadata;
	}

	public void manipulate(NodeResolver nodeResolver, NodeManipulator nodeManipulator) {
		List<ObjectNode> nodes = nodeResolver.resolve(rootNode);

		for (ObjectNode node : nodes) {
			nodeManipulator.manipulate(node);
			node.getObjectNodeContext().addManipulator(nodeManipulator);
		}
	}

	public CombinableArbitrary<?> generate() {
		return generateFixtureContext.generate(null);
	}
}
