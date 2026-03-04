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
 * Default implementation of JvmNode for Java types.
 * <p>
 * This class represents a node in the JVM node tree. Child nodes are managed
 * by {@link com.navercorp.objectfarm.api.tree.JvmNodeTree} rather than by this class.
 */
public final class JavaNode implements JvmNode {
	private final JvmType type;
	@Nullable
	private final String nodeName;
	@Nullable
	private final Integer index;
	@Nullable
	private final CreationMethod creationMethod;

	/**
	 * Creates a new JavaNode without index and creation method.
	 *
	 * @param type     the concrete JVM type for this node
	 * @param nodeName the name of this node
	 */
	public JavaNode(JvmType type, @Nullable String nodeName) {
		this(type, nodeName, null, null);
	}

	/**
	 * Creates a new JavaNode with an index but without a creation method.
	 * <p>
	 * This constructor is provided for backward compatibility.
	 *
	 * @param type     the concrete JVM type for this node
	 * @param nodeName the name of this node
	 * @param index    the index within the parent container (may be null)
	 */
	public JavaNode(JvmType type, @Nullable String nodeName, @Nullable Integer index) {
		this(type, nodeName, index, null);
	}

	/**
	 * Creates a new JavaNode with all fields.
	 *
	 * @param type           the concrete JVM type for this node
	 * @param nodeName       the name of this node
	 * @param index          the index within the parent container (may be null)
	 * @param creationMethod the creation method metadata (may be null)
	 */
	public JavaNode(
		JvmType type,
		@Nullable String nodeName,
		@Nullable Integer index,
		@Nullable CreationMethod creationMethod
	) {
		this.type = type;
		this.nodeName = nodeName;
		this.index = index;
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
	@Nullable
	public CreationMethod getCreationMethod() {
		return creationMethod;
	}
}
