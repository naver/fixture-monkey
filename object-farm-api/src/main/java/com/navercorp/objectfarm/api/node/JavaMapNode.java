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

package com.navercorp.objectfarm.api.node;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Implementation of JvmMapNode that represents a Map entry in Java.
 * <p>
 * This class maintains 1:1 topology mapping with JvmMapNodeCandidate by:
 * <ul>
 *   <li>Representing a single map entry as one node</li>
 *   <li>Exposing key and value via {@link #getKeyNode()} and {@link #getValueNode()}</li>
 * </ul>
 */
public final class JavaMapNode implements JvmMapNode {
	private final JvmType type;
	@Nullable
	private final String nodeName;
	@Nullable
	private final Integer index;
	private final JvmNode keyNode;
	private final JvmNode valueNode;
	@Nullable
	private final CreationMethod creationMethod;

	/**
	 * Creates a new JavaMapNode without index and creation method.
	 *
	 * @param type      the concrete JVM type for this map node
	 * @param nodeName  the name of this node
	 * @param keyNode   the key node
	 * @param valueNode the value node
	 */
	public JavaMapNode(
		JvmType type,
		@Nullable String nodeName,
		JvmNode keyNode,
		JvmNode valueNode
	) {
		this(type, nodeName, null, keyNode, valueNode, null);
	}

	/**
	 * Creates a new JavaMapNode with an index but without a creation method.
	 * <p>
	 * This constructor is provided for backward compatibility.
	 *
	 * @param type      the concrete JVM type for this map node
	 * @param nodeName  the name of this node
	 * @param index     the index within the parent container (may be null)
	 * @param keyNode   the key node
	 * @param valueNode the value node
	 */
	public JavaMapNode(
		JvmType type,
		@Nullable String nodeName,
		@Nullable Integer index,
		JvmNode keyNode,
		JvmNode valueNode
	) {
		this(type, nodeName, index, keyNode, valueNode, null);
	}

	/**
	 * Creates a new JavaMapNode with all fields.
	 *
	 * @param type           the concrete JVM type for this map node
	 * @param nodeName       the name of this node
	 * @param index          the index within the parent container (may be null)
	 * @param keyNode        the key node
	 * @param valueNode      the value node
	 * @param creationMethod the creation method metadata (may be null)
	 */
	public JavaMapNode(
		JvmType type,
		@Nullable String nodeName,
		@Nullable Integer index,
		JvmNode keyNode,
		JvmNode valueNode,
		@Nullable CreationMethod creationMethod
	) {
		this.type = type;
		this.nodeName = nodeName;
		this.index = index;
		this.keyNode = keyNode;
		this.valueNode = valueNode;
		this.creationMethod = creationMethod;
	}

	@Override
	public JvmType getConcreteType() {
		return type;
	}

	@Override
	@Nullable
	public String getNodeName() {
		return nodeName;
	}

	@Override
	@Nullable
	public Integer getIndex() {
		return index;
	}

	@Override
	public JvmNode getKeyNode() {
		return keyNode;
	}

	@Override
	public JvmNode getValueNode() {
		return valueNode;
	}

	@Override
	@Nullable
	public CreationMethod getCreationMethod() {
		return creationMethod;
	}
}
