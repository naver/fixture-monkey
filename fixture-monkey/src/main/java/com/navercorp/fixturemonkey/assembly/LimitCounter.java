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

package com.navercorp.fixturemonkey.assembly;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.planner.AnalyzedScope;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Counts what is left of the limits directives were declared with during one assembly. A limit is counted within
 * its scope: the node a sample starts from for the root scope's paths, each selected node for a defined scope's. A
 * limit on a container's own path caps how many elements it gets, as does the size of a container value decomposed
 * into it.
 */
final class LimitCounter {
	private final Map<Object, Integer> declaredLimitByPath;
	private final Map<Object, Map<JvmNode, Integer>> remainingByPathAndScope = new HashMap<>();
	private final Map<PathExpression, Integer> decomposedSizeByPath = new HashMap<>();

	LimitCounter(AnalyzedScope rootScope, List<AnalyzedScope> analyzedDefinedScopes) {
		this.declaredLimitByPath = new HashMap<>(rootScope.getLimitsByPath());
		for (AnalyzedScope analyzed : analyzedDefinedScopes) {
			analyzed.getLimitsByPath().forEach((relativePath, limit) ->
				declaredLimitByPath.put(new ScopedPath(analyzed.getSelector(), relativePath), limit)
			);
		}
	}

	/**
	 * Takes one from the limit of a directive's path within a scope, when the path has a limit. The path is a
	 * {@link PathExpression} for the root scope's directive, a {@link ScopedPath} for a defined scope's.
	 */
	void consume(Object declaredPath, JvmNode scopeNode) {
		Integer declared = declaredLimitByPath.get(declaredPath);
		if (declared == null) {
			return;
		}
		Map<JvmNode, Integer> remainingByScope =
			remainingByPathAndScope.computeIfAbsent(declaredPath, it -> new IdentityHashMap<>());
		remainingByScope.put(scopeNode, remainingByScope.getOrDefault(scopeNode, declared) - 1);
	}

	/**
	 * Returns whether the limit of a directive's path within a scope has some left, without taking from it.
	 */
	boolean hasRemaining(Object declaredPath, JvmNode scopeNode) {
		Integer declared = declaredLimitByPath.get(declaredPath);
		if (declared == null) {
			return true;
		}
		Map<JvmNode, Integer> remainingByScope = remainingByPathAndScope.get(declaredPath);
		int remaining = remainingByScope != null ? remainingByScope.getOrDefault(scopeNode, declared) : declared;
		return remaining > 0;
	}

	@Nullable
	Integer childLimit(PathExpression containerPath) {
		Integer decomposedSize = decomposedSizeByPath.get(containerPath);
		return decomposedSize != null ? decomposedSize : declaredLimitByPath.get(containerPath);
	}

	boolean hasChildLimit(PathExpression containerPath) {
		return childLimit(containerPath) != null;
	}

	void limitChildren(PathExpression containerPath, int size) {
		decomposedSizeByPath.put(containerPath, size);
	}
}
