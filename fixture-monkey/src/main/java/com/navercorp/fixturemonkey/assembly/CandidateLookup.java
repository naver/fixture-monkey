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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Looks up the value candidate of each path during one assembly, as {@link ScopeLookup} looks up what the scopes
 * declared. It starts with the root scope's values, which planning expanded to their paths, and only
 * decomposing a value changes it afterwards: a candidate replaced by a defined scope's value, or the candidates a
 * container or an object value is decomposed into. Wildcard candidates come only from the root scope, since
 * assembly adds and removes candidates at concrete paths only.
 */
final class CandidateLookup {
	private final Map<PathExpression, ValueCandidate> candidatesByPath;
	private final List<Map.Entry<PathExpression, ValueCandidate>> rootWildcardEntries;
	private final Map<PathExpression, Integer> decomposedScopeDepthByPath = new HashMap<>();

	private CandidateLookup(Map<PathExpression, ValueCandidate> candidatesByPath) {
		this.candidatesByPath = candidatesByPath;
		List<Map.Entry<PathExpression, ValueCandidate>> wildcards = new ArrayList<>();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcards.add(entry);
			}
		}
		this.rootWildcardEntries = Collections.unmodifiableList(wildcards);
	}

	/**
	 * Lays out the root scope's values, which planning expanded to their paths, as a candidate at each path, ordered
	 * as the root scope declared them.
	 */
	static CandidateLookup from(Map<PathExpression, @Nullable Object> rootValuesByPath, ScopeLookup scopes) {
		Map<PathExpression, ValueCandidate> candidatesByPath = new HashMap<>();
		for (Map.Entry<PathExpression, @Nullable Object> entry : rootValuesByPath.entrySet()) {
			PathExpression path = entry.getKey();
			DirectivePrecedence precedence = DirectivePrecedence.rootScope(scopes.rootValueOrderAt(path));
			candidatesByPath.put(path, new ValueCandidate(entry.getValue(), precedence));
		}
		return new CandidateLookup(candidatesByPath);
	}

	@Nullable
	ValueCandidate at(PathExpression path) {
		return candidatesByPath.get(path);
	}

	boolean contains(PathExpression path) {
		return candidatesByPath.containsKey(path);
	}

	Set<PathExpression> getPaths() {
		return Collections.unmodifiableSet(candidatesByPath.keySet());
	}

	Set<Map.Entry<PathExpression, ValueCandidate>> getEntries() {
		return Collections.unmodifiableSet(candidatesByPath.entrySet());
	}

	/**
	 * The root scope's candidates at a wildcard path, which stay as they were laid out for the whole assembly.
	 */
	List<Map.Entry<PathExpression, ValueCandidate>> getRootWildcardEntries() {
		return rootWildcardEntries;
	}

	boolean hasRootWildcards() {
		return !rootWildcardEntries.isEmpty();
	}

	/**
	 * Returns whether a root scope's wildcard candidate reaches {@code path}.
	 */
	boolean matchesRootWildcard(PathExpression path) {
		for (Map.Entry<PathExpression, ValueCandidate> entry : rootWildcardEntries) {
			if (entry.getKey().matches(path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether a candidate sits below {@code parentPath}.
	 */
	boolean hasCandidateBelow(PathExpression parentPath) {
		String parentStr = parentPath.toExpression();
		for (PathExpression candidatePath : candidatesByPath.keySet()) {
			if (isBelow(candidatePath.toExpression(), parentStr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether every candidate below {@code parentPath} was declared before a null of {@code nullPrecedence}
	 * at it, so the null wins over them.
	 */
	boolean isNullDeclaredAfterEveryCandidateBelow(DirectivePrecedence nullPrecedence, PathExpression parentPath) {
		String parentStr = parentPath.toExpression();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			if (isBelow(entry.getKey().toExpression(), parentStr)
				&& entry.getValue().precedence.compareTo(nullPrecedence) >= 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the depth of the scope node whose value was decomposed into the candidate at {@code path} or at one of
	 * its ancestors, or -1 when no defined scope's value was decomposed there.
	 */
	int decomposedScopeDepthAt(PathExpression path) {
		if (decomposedScopeDepthByPath.isEmpty()) {
			return -1;
		}
		PathExpression current = path;
		while (true) {
			Integer depth = decomposedScopeDepthByPath.get(current);
			if (depth != null) {
				return depth;
			}
			PathExpression parent = current.getParent();
			if (parent.equals(current)) {
				return -1;
			}
			current = parent;
		}
	}

	/**
	 * Replaces the candidate at {@code path} with a defined scope's value, decomposed from the scope node at
	 * {@code scopeDepth}.
	 */
	void replaceWithDefinedScopeValue(PathExpression path, ValueCandidate candidate, int scopeDepth) {
		decomposedScopeDepthByPath.put(path, scopeDepth);
		candidatesByPath.put(path, candidate);
	}

	/**
	 * Removes the candidates at each of {@code paths} and below it: the elements a container value has beyond the
	 * tree it is assembled into.
	 */
	void removeCandidatesUnder(Set<PathExpression> paths) {
		for (PathExpression path : paths) {
			candidatesByPath.remove(path);
			candidatesByPath.keySet().removeIf(key -> key.isChildOf(path));
		}
	}

	/**
	 * Puts the candidates a value is decomposed into at their paths, replacing the candidates already there.
	 */
	void putDecomposedCandidates(Map<PathExpression, ValueCandidate> decomposedCandidates) {
		candidatesByPath.putAll(decomposedCandidates);
	}

	private static boolean isBelow(String candidate, String parent) {
		if (candidate.length() <= parent.length() || !candidate.startsWith(parent)) {
			return false;
		}
		char next = candidate.charAt(parent.length());
		return next == '.' || next == '[';
	}
}
