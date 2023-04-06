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

package com.navercorp.fixturemonkey.tree;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ApplyStrictModeResolver implements NodeResolver {
	private final NodeResolver nodeResolver;

	public ApplyStrictModeResolver(NodeResolver nodeResolver) {
		this.nodeResolver = nodeResolver;
	}

	@Override
	public List<ObjectNode> resolve(ObjectNode objectNode) {
		List<ObjectNode> selectedNodes = nodeResolver.resolve(objectNode);

		if (selectedNodes.isEmpty()) {
			throw new IllegalArgumentException("No matching results for given NodeResolvers.");
		}
		return selectedNodes;
	}

	@Override
	public List<NextNodePredicate> toNextNodePredicate() {
		return nodeResolver.toNextNodePredicate();
	}
}
