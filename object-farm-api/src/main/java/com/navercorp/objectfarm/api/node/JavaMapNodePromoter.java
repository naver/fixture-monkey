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

import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.JvmMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;

/**
 * Promotes JvmMapNodeCandidate to a JvmMapNode.
 * <p>
 * This promoter creates a single JvmMapNode that wraps key and value nodes,
 * maintaining 1:1 mapping with the candidate.
 */
public final class JavaMapNodePromoter implements JvmNodePromoter {
	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		return node instanceof JvmMapNodeCandidate;
	}

	@Override
	public List<JvmNode> promote(JvmNodeCandidate node, JvmNodeContext context) {
		JvmMapNodeCandidate mapNodeCandidate = (JvmMapNodeCandidate)node;

		JvmNode keyNode = new JavaNode(
			mapNodeCandidate.getKey().getType(),
			mapNodeCandidate.getKey().getName(),
			null,
			mapNodeCandidate.getKey().getCreationMethod()
		);

		JvmNode valueNode = new JavaNode(
			mapNodeCandidate.getValue().getType(),
			mapNodeCandidate.getValue().getName(),
			null,
			mapNodeCandidate.getValue().getCreationMethod()
		);

		// Create a JavaMapNode that wraps key and value
		JavaMapNode mapNode = new JavaMapNode(
			mapNodeCandidate.getType(),
			mapNodeCandidate.getName(),
			null,
			keyNode,
			valueNode,
			mapNodeCandidate.getCreationMethod()
		);

		return Collections.singletonList(mapNode);
	}
}
