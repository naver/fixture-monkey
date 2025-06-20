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

package com.navercorp.objectfarm.api.tree;

import java.util.Set;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Context for controlling recursive node expansion based on user-defined paths.
 * When a cyclic reference is detected, this context determines whether to continue
 * expansion by checking if any user-defined paths exist beneath the current path.
 */
public final class ExpansionContext {
	private final Set<PathExpression> userDefinedPaths;

	/**
	 * Creates an expansion context with user-defined paths.
	 *
	 * @param userDefinedPaths set of path expressions where users explicitly set values
	 */
	public ExpansionContext(Set<PathExpression> userDefinedPaths) {
		this.userDefinedPaths = userDefinedPaths;
	}

	/**
	 * Determines if a cyclic path should be expanded.
	 * Returns true if user-defined paths exist beneath the current path.
	 *
	 * @param currentPath the path being evaluated
	 * @param type the type at this path (already in ancestors, hence cyclic)
	 * @param ancestors set of ancestor types in the current path
	 * @return true if expansion should continue, false otherwise
	 */
	boolean shouldExpandPath(PathExpression currentPath, Class<?> type, Set<Class<?>> ancestors) {
		if (!ancestors.contains(type)) {
			return true;
		}

		return hasChildPathsInUserSet(currentPath);
	}

	private boolean hasChildPathsInUserSet(PathExpression currentPath) {
		for (PathExpression userPath : userDefinedPaths) {
			if (userPath.isChildOf(currentPath)) {
				return true;
			}

			if (userPath.hasWildcard() && userPath.startsWith(currentPath)) {
				return true;
			}
		}

		return false;
	}
}
