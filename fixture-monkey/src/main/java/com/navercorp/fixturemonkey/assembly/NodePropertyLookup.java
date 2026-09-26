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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Looks up the property of each node during one assembly: the one a node is generated with, which an implementation
 * chosen during assembly can replace, and the one it is matched with, which has the type it is declared with.
 */
final class NodePropertyLookup {
	private final Map<JvmNode, Property> generationPropertyByNode = new IdentityHashMap<>();
	private final Function<JvmNode, Property> generationPropertyFactory;
	private final Map<JvmNode, Property> matchingPropertyByNode = new IdentityHashMap<>();
	private final Function<JvmNode, Property> matchingPropertyFactory;

	private NodePropertyLookup(Function<JvmNode, @Nullable JvmNode> parentOf) {
		this.generationPropertyFactory = new JvmNodePropertyFactory(parentOf);
		this.matchingPropertyFactory = JvmNodePropertyFactory.forMatching(parentOf);
	}

	/**
	 * Lays out the properties of the nodes of {@code tree}, whose parents give each property its owner.
	 */
	static NodePropertyLookup from(AssemblyTree tree) {
		return new NodePropertyLookup(tree::parentOf);
	}

	/**
	 * Returns the property of a node for generating its value, with the type chosen to be built for it.
	 */
	Property generationPropertyOf(JvmNode node) {
		return generationPropertyByNode.computeIfAbsent(node, generationPropertyFactory);
	}

	/**
	 * Makes {@code chosen}, the implementation chosen for a node during assembly, the property it is generated with
	 * in place of its own.
	 */
	void chooseGenerationProperty(JvmNode node, Property chosen) {
		generationPropertyByNode.put(node, chosen);
	}

	/**
	 * Returns the property a node is generated with on its own, ignoring an implementation chosen for it during
	 * assembly.
	 */
	Property ownGenerationPropertyOf(JvmNode node) {
		return generationPropertyFactory.apply(node);
	}

	/**
	 * Returns the property of a node for matching what it is, with the type it is declared with.
	 */
	Property matchingPropertyOf(JvmNode node) {
		return matchingPropertyByNode.computeIfAbsent(node, matchingPropertyFactory);
	}
}
