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

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * The candidate that wins at a node by precedence, with where it was declared: its path, a {@link PathExpression}
 * from the root or a {@link ScopedPath} relative to a scope node, and the scope node it came from.
 */
final class WinningValueCandidate {
	final ScopedPath declaredPath;
	final ValueCandidate candidate;
	/**
	 * How many segments lead to the scope node the value came from, 0 for the root scope.
	 */
	final int scopeDepth;
	/**
	 * The scope node the value came from: the node a limit on its path is counted within.
	 */
	final JvmNode scopeNode;

	WinningValueCandidate(ScopedPath declaredPath, ValueCandidate candidate, int scopeDepth, JvmNode scopeNode) {
		this.declaredPath = declaredPath;
		this.candidate = candidate;
		this.scopeDepth = scopeDepth;
		this.scopeNode = scopeNode;
	}

	boolean isDeclaredAt(PathExpression path) {
		return declaredPath.isRootScopePath(path);
	}

	/**
	 * Returns whether the value came from a defined scope, so it is expanded during assembly rather than planning.
	 */
	boolean isFromDefinedScope() {
		return !candidate.precedence.isRootScope();
	}

	boolean isScopeRoot() {
		return !declaredPath.isRootScope() && declaredPath.isScopeRoot();
	}
}
