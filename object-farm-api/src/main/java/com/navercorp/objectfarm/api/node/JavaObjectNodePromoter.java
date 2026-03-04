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

import com.navercorp.objectfarm.api.nodecandidate.JavaMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapEntryNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;

public final class JavaObjectNodePromoter implements JvmNodePromoter {

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		if (node instanceof JavaMapNodeCandidate || node instanceof JvmMapEntryNodeCandidate) {
			return false;
		}
		return true;
	}

	@Override
	public List<JvmNode> promote(JvmNodeCandidate node, JvmNodeContext context) {
		return Collections.singletonList(new JavaNode(node.getType(), node.getName(), null, node.getCreationMethod()));
	}
}
