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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.nodecontext.JvmContainerNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

public final class JavaLinearContainerElementNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final JvmContainerNodeContext context;

	public JavaLinearContainerElementNodeCandidateGenerator(JvmContainerNodeContext context) {
		this.context = context;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		JvmType typeVariables = jvmType.getTypeVariables().get(0);

		int containerSize = context.resolveContainerSize(jvmType);

		List<JvmNodeCandidate> elementNodeCandidates = new ArrayList<>();
		for (int i = 0; i < containerSize; i++) {
			elementNodeCandidates.add(new JavaNodeCandidate(typeVariables, null));
		}

		return Collections.unmodifiableList(elementNodeCandidates);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		boolean collectionType = Types.isAssignable(rawType, Collection.class)
			&& !Types.isAssignable(rawType, Map.class)
			&& !rawType.isArray();

		boolean singleTypeVariable = jvmType.getTypeVariables().size() == 1;
		return collectionType && singleTypeVariable;
	}
}
