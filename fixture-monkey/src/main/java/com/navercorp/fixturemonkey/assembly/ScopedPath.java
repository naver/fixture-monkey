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

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;

/**
 * A path declared in a defined scope, relative to the node the scope selects. It reaches the node at the end of a
 * {@link ScopeChain} when the path from a selected node on the chain down to that node matches it; the scope node
 * then sits at the scope depth of the match.
 */
final class ScopedPath {
	private final ScopeSelector scope;
	private final PathExpression relativePath;

	ScopedPath(ScopeSelector scope, PathExpression relativePath) {
		this.scope = scope;
		this.relativePath = relativePath;
	}

	/**
	 * Returns the path the root scope declared a directive at, relative to the node a sample starts from.
	 */
	static ScopedPath root(PathExpression path) {
		return new ScopedPath(ScopeSelector.root(), path);
	}

	ScopeSelector getScope() {
		return scope;
	}

	boolean isScopeRoot() {
		return relativePath.isRoot();
	}

	boolean isRootScope() {
		return scope == ScopeSelector.root();
	}

	/**
	 * Returns whether this is the root scope's exact path {@code path}.
	 */
	boolean isRootScopePath(PathExpression path) {
		return isRootScope() && relativePath.equals(path);
	}

	/**
	 * Returns the depth of the scope node on {@code chain} when this path reaches the node at its end.
	 *
	 * @return the scope depth, or -1 when this path does not reach the node
	 */
	int scopeDepthOf(ScopeChain chain) {
		return chain.scopeDepthOf(scope, relativePath);
	}

	/**
	 * Returns the segment of this path right below the node at the end of {@code chain} when this path reaches
	 * past it from a scope node at most {@code scopeDepthLimit} deep.
	 *
	 * @return the segment below, or null when this path does not reach below the node
	 */
	@Nullable Segment segmentBelow(ScopeChain chain, int scopeDepthLimit) {
		return chain.segmentBelow(scope, relativePath, scopeDepthLimit);
	}

	/**
	 * Returns the depth of the scope node this path reaches past the node at the end of {@code chain} from, at
	 * most {@code scopeDepthLimit} deep.
	 *
	 * @return the scope depth, or -1 when this path does not reach below the node
	 */
	int scopeDepthBelow(ScopeChain chain, int scopeDepthLimit) {
		return chain.scopeDepthBelow(scope, relativePath, scopeDepthLimit);
	}

	String toExpression() {
		return "$[" + scope.toExpression() + "]" + relativePath.toExpression().substring(1);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ScopedPath)) {
			return false;
		}
		ScopedPath that = (ScopedPath)obj;
		return scope.equals(that.scope) && relativePath.equals(that.relativePath);
	}

	@Override
	public int hashCode() {
		return 31 * scope.hashCode() + relativePath.hashCode();
	}

	@Override
	public String toString() {
		return toExpression();
	}
}
