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

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaMapNodeCandidate implements JvmMapNodeCandidate {
	private final JvmType jvmType;
	@Nullable // It may be null if nested in a container
	private final String name;
	private final JvmNodeCandidate keyNode;
	private final JvmNodeCandidate valueNode;

	public JavaMapNodeCandidate(JvmType jvmType, @Nullable String name, JvmNodeCandidate keyNode,
		JvmNodeCandidate valueNode) {
		this.jvmType = jvmType;
		this.name = name;
		this.keyNode = keyNode;
		this.valueNode = valueNode;
	}

	@Override
	public JvmType getJvmType() {
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
}
