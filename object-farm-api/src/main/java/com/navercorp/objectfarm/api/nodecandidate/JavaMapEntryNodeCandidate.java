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

package com.navercorp.objectfarm.api.nodecandidate;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Implementation of JvmMapEntryNodeCandidate for standalone Map.Entry fields.
 */
public final class JavaMapEntryNodeCandidate implements JvmMapEntryNodeCandidate {
	private final JvmType jvmType;
	@Nullable
	private final String name;
	private final JvmNodeCandidate keyNode;
	private final JvmNodeCandidate valueNode;
	@Nullable
	private final CreationMethod creationMethod;

	/**
	 * Creates a new JavaMapEntryNodeCandidate with a creation method.
	 *
	 * @param jvmType        the JVM type for this map entry candidate
	 * @param name           the name of this candidate (may be null)
	 * @param keyNode        the key node candidate
	 * @param valueNode      the value node candidate
	 * @param creationMethod the creation method metadata (may be null)
	 */
	public JavaMapEntryNodeCandidate(
		JvmType jvmType,
		@Nullable String name,
		JvmNodeCandidate keyNode,
		JvmNodeCandidate valueNode,
		@Nullable CreationMethod creationMethod
	) {
		this.jvmType = jvmType;
		this.name = name;
		this.keyNode = keyNode;
		this.valueNode = valueNode;
		this.creationMethod = creationMethod;
	}

	@Override
	public JvmType getType() {
		return this.jvmType;
	}

	@Override
	public JvmNodeCandidate getKey() {
		return this.keyNode;
	}

	@Override
	public JvmNodeCandidate getValue() {
		return this.valueNode;
	}

	@Override
	@Nullable
	public String getName() {
		return this.name;
	}

	@Override
	@Nullable
	public CreationMethod getCreationMethod() {
		return this.creationMethod;
	}
}
