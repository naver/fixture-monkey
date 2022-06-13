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

package com.navercorp.fixturemonkey.resolver;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ApplyExpressionStrictModeResolver implements NodeResolver {
	private final NodeResolver nodeResolver;

	public ApplyExpressionStrictModeResolver(
		NodeResolver nodeResolver
	) {
		this.nodeResolver = nodeResolver;
	}

	@Override
	public List<ArbitraryNode> resolve(ArbitraryTree arbitraryTree) {
		List<ArbitraryNode> selectedNodes = nodeResolver.resolve(arbitraryTree);

		if (selectedNodes.isEmpty()) {
			String message = "No matching results for given expression.";
			if (nodeResolver instanceof ExpressionNodeResolver) {
				String expression = ((ExpressionNodeResolver)nodeResolver).getExpression();
				message += " Expression: \"" + expression + "\"";
			}
			throw new IllegalArgumentException(message);
		}
		return selectedNodes;
	}
}
