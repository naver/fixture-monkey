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

package com.navercorp.fixturemonkey.projection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Pre-built lookup index for paths that have any descendant directives (values, sizes,
 * customizers, notNull). Used by the assembler to skip "find best matching path" work when a
 * parent path has no relevant descendants.
 */
final class PathIndex {
	private static final Pattern INDEX_PATTERN = Pattern.compile("\\[\\d+]");

	private final Map<PathExpression, Set<PathExpression>> childPathsByParent;
	private final Set<PathExpression> wildcardParentPaths;
	private final Set<PathExpression> typePatternPaths;

	PathIndex(
		Set<PathExpression> valuePaths,
		Set<PathExpression> userContainerSizePaths,
		Set<PathExpression> customizerPaths,
		Set<PathExpression> notNullPaths
	) {
		this.childPathsByParent = new HashMap<>();
		this.wildcardParentPaths = new HashSet<>();
		this.typePatternPaths = new HashSet<>();

		indexAncestors(valuePaths);
		indexAncestors(userContainerSizePaths);
		indexAncestors(customizerPaths);
		indexAncestors(notNullPaths);

		for (PathExpression sizePath : userContainerSizePaths) {
			childPathsByParent.computeIfAbsent(sizePath, k -> new HashSet<>());
		}
	}

	private void indexAncestors(Set<PathExpression> paths) {
		for (PathExpression path : paths) {
			if (path.hasTypeSelector()) {
				typePatternPaths.add(path);
				continue;
			}

			PathExpression ancestor = path.getParent();
			while (ancestor != null && !ancestor.equals(path)) {
				childPathsByParent.computeIfAbsent(ancestor, k -> new HashSet<>()).add(path);
				PathExpression next = ancestor.getParent();
				if (next != null && next.equals(ancestor)) {
					break;
				}
				ancestor = next;
			}

			if (path.hasWildcard()) {
				PathExpression wildcardParent = path.getParent();
				if (wildcardParent != null) {
					wildcardParentPaths.add(wildcardParent);
				}
			}
		}
	}

	boolean hasChildPaths(PathExpression parentPath) {
		if (childPathsByParent.containsKey(parentPath)) {
			return true;
		}
		if (wildcardParentPaths.contains(parentPath)) {
			return true;
		}
		String pathStr = parentPath.toExpression();
		// Convert indexed paths ($.list[0]) to wildcard form ($.list[*]) to check wildcard coverage
		if (pathStr.contains("[") && !pathStr.contains("[*]")) {
			String wildcardForm = INDEX_PATTERN.matcher(pathStr).replaceAll("[*]");
			PathExpression wildcardPath = PathExpression.of(wildcardForm);
			return wildcardParentPaths.contains(wildcardPath)
				|| childPathsByParent.containsKey(wildcardPath);
		}
		return false;
	}

	Set<PathExpression> getTypePatternPaths() {
		return typePatternPaths;
	}
}
