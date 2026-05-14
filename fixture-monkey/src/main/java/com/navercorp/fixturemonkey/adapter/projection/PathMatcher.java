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

package com.navercorp.fixturemonkey.adapter.projection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.KeySelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.TypeSelector;
import com.navercorp.objectfarm.api.expression.ValueSelector;
import com.navercorp.objectfarm.api.expression.WildcardSelector;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Stateless path-matching helpers extracted from {@code ValueProjectionAssembler}.
 *
 * <p>Covers: best-path resolution (exact + wildcard + type selector), descendant-path
 * existence checks, type-pattern matching, and node lookup by path. All methods are pure
 * functions of {@link AssemblyState} or path inputs — no mutation of the state.</p>
 */
final class PathMatcher {
	private PathMatcher() {
	}

	static boolean hasChildPathValues(PathExpression parentPath, PathIndex pathIndex) {
		return pathIndex.hasChildPaths(parentPath);
	}

	static boolean hasChildCandidateValues(
		PathExpression parentPath,
		Map<PathExpression, ValueCandidate> candidatesByPath
	) {
		String parentStr = parentPath.toExpression();
		for (PathExpression candidatePath : candidatesByPath.keySet()) {
			String candidateStr = candidatePath.toExpression();
			if (candidateStr.length() > parentStr.length() && candidateStr.startsWith(parentStr)) {
				char nextChar = candidateStr.charAt(parentStr.length());
				if (nextChar == '.' || nextChar == '[') {
					return true;
				}
			}
		}
		return false;
	}

	static boolean isNullSetAfterAllChildren(
		ValueOrder nullOrder,
		PathExpression parentPath,
		Map<PathExpression, ValueCandidate> candidatesByPath
	) {
		String parentStr = parentPath.toExpression();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			String candidateStr = entry.getKey().toExpression();
			if (candidateStr.length() > parentStr.length() && candidateStr.startsWith(parentStr)) {
				char nextChar = candidateStr.charAt(parentStr.length());
				if (nextChar == '.' || nextChar == '[') {
					if (entry.getValue().order.compareTo(nullOrder) >= 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	static @Nullable PathExpression findBestMatchingPath(
		Map<PathExpression, ValueCandidate> candidatesByPath,
		PathExpression path,
		@Nullable AssemblyState state
	) {
		PathExpression bestPath = null;
		// Sentinel initial values — RegisterOrder.of(MIN_VALUE) loses to any real comparison
		ValueOrder bestOrder = ValueOrder.RegisterOrder.of(Integer.MIN_VALUE);

		PathExpression bestTypePath = null;
		int bestTypeDepth = -1;
		ValueOrder bestTypeOrder = ValueOrder.RegisterOrder.of(Integer.MIN_VALUE);

		ValueCandidate exactCandidate = candidatesByPath.get(path);
		if (exactCandidate != null) {
			if (exactCandidate.order.compareTo(bestOrder) > 0) {
				bestOrder = exactCandidate.order;
				bestPath = path;
			}
		}

		if (state != null) {
			for (Map.Entry<PathExpression, ValueCandidate> entry : state.wildcardEntries) {
				PathExpression pattern = entry.getKey();
				if (pattern.matches(path)) {
					ValueOrder order = entry.getValue().order;
					if (order.compareTo(bestOrder) > 0) {
						bestOrder = order;
						bestPath = pattern;
					}
				}
			}

			for (Map.Entry<PathExpression, ValueCandidate> entry : state.typeSelectorEntries) {
				PathExpression pattern = entry.getKey();
				// depth = how deep in the tree the type match occurs; deeper = more specific = wins
				int depth = matchesTypePattern(pattern, path, state);
				if (depth >= 0) {
					ValueOrder order = entry.getValue().order;
					if (depth > bestTypeDepth || (depth == bestTypeDepth && order.compareTo(bestTypeOrder) > 0)) {
						bestTypeDepth = depth;
						bestTypeOrder = order;
						bestTypePath = pattern;
					}
				}
			}
		}

		if (bestTypePath != null) {
			if (bestPath == null || bestTypeOrder.compareTo(bestOrder) > 0) {
				return bestTypePath;
			}
		}

		return bestPath;
	}

	static boolean matchesAnyWildcardCandidate(PathExpression childPath, AssemblyState state) {
		for (Map.Entry<PathExpression, ValueCandidate> entry : state.wildcardEntries) {
			if (entry.getKey().matches(childPath)) {
				return true;
			}
		}
		return false;
	}

	static int matchesTypePattern(PathExpression pattern, PathExpression path, AssemblyState state) {
		List<Segment> patternSegments = pattern.getSegments();
		List<Segment> pathSegments = path.getSegments();

		if (patternSegments.isEmpty()) {
			return -1;
		}

		int typeSegmentIndex = -1;
		TypeSelector typeSelector = null;
		for (int i = 0; i < patternSegments.size(); i++) {
			Segment seg = patternSegments.get(i);
			if (seg.isSingleSelector() && seg.getFirstSelector() instanceof TypeSelector) {
				typeSegmentIndex = i;
				typeSelector = (TypeSelector)seg.getFirstSelector();
				break;
			}
		}

		if (typeSelector == null) {
			return -1;
		}

		int patternSuffixLen = patternSegments.size() - typeSegmentIndex - 1;

		// TypeSelector-only pattern ($[type:T]) — only matches current node's type directly.
		// Descendant field matching is handled by resolveThenApplyAncestorValue.
		if (patternSuffixLen == 0 && typeSegmentIndex == 0) {
			JvmNode currentNode = findNodeForPath(path, state);
			if (currentNode != null && typeSelector.matchesType(currentNode.getConcreteType().getRawType())) {
				return pathSegments.size();
			}
			return -1;
		}

		if (patternSuffixLen > pathSegments.size()) {
			return -1;
		}

		int suffixStartInPath = pathSegments.size() - patternSuffixLen;
		for (int i = 0; i < patternSuffixLen; i++) {
			Segment patternSeg = patternSegments.get(typeSegmentIndex + 1 + i);
			Segment pathSeg = pathSegments.get(suffixStartInPath + i);
			if (!patternSeg.equals(pathSeg)
				&& !(patternSeg.isSingleSelector()
				&& patternSeg.getFirstSelector() instanceof WildcardSelector
				&& pathSeg.isSingleSelector()
				&& (pathSeg.getFirstSelector() instanceof IndexSelector
				|| pathSeg.getFirstSelector() instanceof KeySelector
				|| pathSeg.getFirstSelector() instanceof ValueSelector))
			) {
				return -1;
			}
		}

		int ownerPos = suffixStartInPath - 1;

		PathExpression nodeAtPath;
		if (ownerPos < 0) {
			nodeAtPath = PathExpression.root();
		} else {
			nodeAtPath = buildPathUpTo(path, ownerPos);
		}

		JvmNode node = findNodeForPath(nodeAtPath, state);
		if (node == null) {
			return -1;
		}

		if (typeSelector.matchesType(node.getConcreteType().getRawType())) {
			return ownerPos + 1;
		}

		return -1;
	}

	static PathExpression buildPathUpTo(PathExpression fullPath, int segmentIndex) {
		List<Segment> segments = fullPath.getSegments();
		PathExpression result = PathExpression.root();
		for (int i = 0; i <= segmentIndex && i < segments.size(); i++) {
			result = result.appendSegment(segments.get(i));
		}
		return result;
	}

	static boolean isRootTypeSelector(PathExpression path) {
		List<Segment> segments = path.getSegments();
		return (segments.size() == 1
			&& segments.get(0).isSingleSelector()
			&& segments.get(0).getFirstSelector() instanceof TypeSelector);
	}

	static boolean hasFieldLevelTypeSelectorSiblings(PathExpression rootTypePath, AssemblyState state) {
		TypeSelector rootTypeSelector = (TypeSelector)rootTypePath.getSegments().get(0).getFirstSelector();
		Class<?> rootType = rootTypeSelector.getTargetType();

		for (PathExpression path : state.candidatesByPath.keySet()) {
			if (path.equals(rootTypePath)) {
				continue;
			}
			List<Segment> segments = path.getSegments();
			if (segments.size() > 1
				&& segments.get(0).isSingleSelector()
				&& segments.get(0).getFirstSelector() instanceof TypeSelector) {
				TypeSelector ts = (TypeSelector)segments.get(0).getFirstSelector();
				if (ts.getTargetType().equals(rootType)) {
					return true;
				}
			}
		}
		return false;
	}

	static @Nullable JvmNode findNodeForPath(PathExpression path, AssemblyState state) {
		String pathStr = path.toExpression();
		JvmNode node = state.nodeByPath.get(pathStr);
		if (node != null) {
			return node;
		}
		if (state.nodeTree != null) {
			return state.nodeTree.resolve(path);
		}
		return null;
	}

	static boolean hasMatchingTypeSelector(JvmType nodeType, PathIndex pathIndex) {
		Set<PathExpression> typePatternPaths = pathIndex.getTypePatternPaths();
		if (typePatternPaths.isEmpty()) {
			return false;
		}

		Class<?> rawType = nodeType.getRawType();
		for (PathExpression pattern : typePatternPaths) {
			List<Segment> segments = pattern.getSegments();
			for (Segment segment : segments) {
				if (segment.isSingleSelector() && segment.getFirstSelector() instanceof TypeSelector) {
					TypeSelector typeSelector = (TypeSelector)segment.getFirstSelector();
					if (typeSelector.matchesType(rawType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	static boolean isUnderExcludedPath(PathExpression path, Set<PathExpression> excludedPaths) {
		for (PathExpression excluded : excludedPaths) {
			if (path.isChildOf(excluded)) {
				return true;
			}
		}
		return false;
	}
}
