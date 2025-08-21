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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.nodecontext.JvmContainerNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

public final class JavaMapElementNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final JvmContainerNodeContext context;

	public JavaMapElementNodeCandidateGenerator(JvmContainerNodeContext context) {
		this.context = context;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		int mapSize = context.resolveContainerSize(jvmType);

		List<JvmNodeCandidate> nodeCandidates = new ArrayList<>();
		for (int i = 0; i < mapSize; i++) {
			List<? extends JvmType> typeVariables = jvmType.getTypeVariables();
			JvmType keyType = typeVariables.get(0);
			JvmType valueType = typeVariables.get(1);

			nodeCandidates.add(
				new JavaMapNodeCandidate(
					jvmType,
					null,
					new JavaNodeCandidate(keyType, null),
					new JavaNodeCandidate(valueType, null)
				)
			);
		}

		return Collections.unmodifiableList(nodeCandidates);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		return Types.isAssignable(jvmType.getRawType(), Map.class)
			&& jvmType.getTypeVariables().size() == 2;
	}
}
