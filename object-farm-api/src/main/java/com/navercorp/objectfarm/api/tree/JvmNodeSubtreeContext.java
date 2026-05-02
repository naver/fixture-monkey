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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Caches JvmNode subtree snapshots by JvmType for reuse across multiple transform() calls.
 * <p>
 * When a POJO type is first expanded during tree transformation, its node structure
 * (parent-child relationships, candidate mappings, etc.) is captured as a {@link Snapshot}.
 * Subsequent expansions of the same type can reuse this cached structure,
 * only dynamically expanding container nodes (whose sizes vary per call).
 */
public final class JvmNodeSubtreeContext {
	private final ConcurrentHashMap<JvmType, Snapshot> cache = new ConcurrentHashMap<>();

	@Nullable
	Snapshot get(JvmType type) {
		return cache.get(type);
	}

	void put(JvmType type, Snapshot snapshot) {
		cache.put(type, snapshot);
	}

	public void clear() {
		cache.clear();
	}

	public int size() {
		return cache.size();
	}

	/**
	 * Snapshot of a POJO subtree structure.
	 * Contains JvmNode instances, parent-child relationships,
	 * and metadata needed to restore the subtree into a new TransformContext.
	 * Container nodes are recorded but not expanded — they must be dynamically
	 * expanded each time the snapshot is applied.
	 */
	static final class Snapshot {

		final List<JvmNode> directChildren;
		final Map<JvmNode, List<JvmNode>> parentChildMap;
		final List<JvmNode> allNodes;
		final Map<JvmNode, JvmNodeCandidate> nodeToCandidate;
		final Map<JvmNodeCandidate, List<JvmNode>> candidateToNodes;
		/** Relative path from the subtree root to each node (for computing absolute paths). */
		final Map<JvmNode, PathExpression> relativePathFromRoot;
		/** Container nodes that need dynamic expansion when the snapshot is applied. */
		final List<JvmNode> containerNodes;
		/** Parent mapping within the subtree for nodeToParent restoration. */
		final Map<JvmNode, JvmNode> nodeToParent;
		/** Whether this subtree is self-recursive (the root type appears in its own subtree). */
		final boolean selfRecursive;
		/** All raw types present in this subtree (for ancestor overlap detection). */
		final Set<Class<?>> subtreeTypes;
		/**
		 * Ancestors that were active when this snapshot was captured.
		 * Used to detect when a snapshot's cycle truncation doesn't match the current context.
		 */
		final Set<Class<?>> capturedAncestors;

		Snapshot(
			List<JvmNode> directChildren,
			Map<JvmNode, List<JvmNode>> parentChildMap,
			List<JvmNode> allNodes,
			Map<JvmNode, JvmNodeCandidate> nodeToCandidate,
			Map<JvmNodeCandidate, List<JvmNode>> candidateToNodes,
			Map<JvmNode, PathExpression> relativePathFromRoot,
			List<JvmNode> containerNodes,
			Map<JvmNode, JvmNode> nodeToParent,
			boolean selfRecursive,
			Set<Class<?>> subtreeTypes,
			Set<Class<?>> capturedAncestors
		) {
			this.directChildren = directChildren;
			this.parentChildMap = parentChildMap;
			this.allNodes = allNodes;
			this.nodeToCandidate = nodeToCandidate;
			this.candidateToNodes = candidateToNodes;
			this.relativePathFromRoot = relativePathFromRoot;
			this.containerNodes = containerNodes;
			this.nodeToParent = nodeToParent;
			this.selfRecursive = selfRecursive;
			this.subtreeTypes = subtreeTypes;
			this.capturedAncestors = capturedAncestors;
		}

		/**
		 * Checks if this snapshot can be safely applied in the given ancestor context.
		 * <p>
		 * A snapshot is incompatible when:
		 * <ul>
		 *   <li>The captured ancestors are not a subset of the current ancestors — the snapshot
		 *       cycle-truncated types that wouldn't be truncated now (less restrictive context).</li>
		 *   <li>The current ancestors contain types present in the subtree that were NOT ancestors
		 *       during capture — the snapshot expanded them, but they should now be cycle-truncated.</li>
		 * </ul>
		 */
		boolean isCompatibleWithAncestors(Set<Class<?>> currentAncestors) {
			// Check 1: all captured ancestors must still be ancestors
			if (!currentAncestors.containsAll(capturedAncestors)) {
				return false;
			}
			// Check 2: no new ancestor should overlap with subtree types
			for (Class<?> ancestor : currentAncestors) {
				if (!capturedAncestors.contains(ancestor) && subtreeTypes.contains(ancestor)) {
					return false;
				}
			}
			return true;
		}
	}
}
