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

public final class JavaNodeCandidate implements JvmNodeCandidate {
	private final JvmType jvmType;
	@Nullable
	private final String name;

	public JavaNodeCandidate(JvmType jvmType, @Nullable String name) {
		this.jvmType = jvmType;
		this.name = name;
	}

	@Override
	public JvmType getJvmType() {
		return jvmType;
	}

	@Override
	@Nullable
	public String getName() {
		return name;
	}
}
