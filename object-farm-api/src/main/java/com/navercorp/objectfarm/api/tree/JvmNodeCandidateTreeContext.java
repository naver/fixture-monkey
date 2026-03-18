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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Context for creating and caching JvmNodeCandidateTree instances.
 * <p>
 * This class manages the creation of JvmNodeCandidateTree and caches subtree information
 * for specific JvmTypes to reduce tree construction costs. When building a tree, if a
 * subtree for a particular JvmType already exists in the cache, it will be reused instead
 * of regenerating the entire subtree.
 * <p>
 * The caching mechanism stores:
 * <ul>
 *   <li>Child candidates for each JvmType</li>
 *   <li>Parent-child relationships within the subtree</li>
 * </ul>
 * <p>
 * This is particularly useful when multiple trees share common type structures,
 * significantly reducing redundant computation.
 */
public final class JvmNodeCandidateTreeContext {
	private final Map<JvmType, SubtreeSnapshot> subtreeCache;

	/**
	 * Creates a new JvmNodeCandidateTreeContext with the specified JvmNodeContext.
	 */
	public JvmNodeCandidateTreeContext() {
		this.subtreeCache = new ConcurrentHashMap<>();
	}

	/**
	 * Retrieves cached subtree information for the given JvmType.
	 *
	 * @param jvmType the type to look up in the cache
	 * @return cached subtree snapshot if available, null otherwise
	 */
	@Nullable
	SubtreeSnapshot getCachedSubtree(JvmType jvmType) {
		return subtreeCache.get(jvmType);
	}

	/**
	 * Caches subtree information for the given JvmType.
	 * Abstract classes and interfaces are not cached as they require concrete implementation resolution.
	 *
	 * @param jvmType the type to cache
	 * @param children the list of child candidates
	 * @param parentChildMap the parent-child relationships in the subtree
	 */
	void cacheSubtree(
		JvmType jvmType,
		List<JvmNodeCandidate> children,
		Map<JvmNodeCandidate, List<JvmNodeCandidate>> parentChildMap
	) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers())) {
			return;
		}

		SubtreeSnapshot snapshot = new SubtreeSnapshot(
			new ArrayList<>(children),
			new HashMap<>(parentChildMap)
		);
		subtreeCache.putIfAbsent(jvmType, snapshot);
	}

	/**
	 * Clears all cached subtree information.
	 * Useful when generator configurations change or when memory needs to be freed.
	 */
	public void clearCache() {
		subtreeCache.clear();
	}

	/**
	 * Returns the size of the cache (number of cached types).
	 *
	 * @return the number of cached JvmTypes
	 */
	public int getCacheSize() {
		return subtreeCache.size();
	}

	/**
	 * Checks if subtree information for the given type is cached.
	 *
	 * @param jvmType the type to check
	 * @return true if cached, false otherwise
	 */
	public boolean isCached(JvmType jvmType) {
		return subtreeCache.containsKey(jvmType);
	}

	/**
	 * Represents a snapshot of a subtree structure for a specific JvmType.
	 * Contains the immediate children and all parent-child relationships
	 * within the subtree at the time of caching.
	 */
	static final class SubtreeSnapshot {
		private final List<JvmNodeCandidate> children;
		private final Map<JvmNodeCandidate, List<JvmNodeCandidate>> parentChildMap;

		SubtreeSnapshot(
			List<JvmNodeCandidate> children,
			Map<JvmNodeCandidate, List<JvmNodeCandidate>> parentChildMap
		) {
			this.children = children;
			this.parentChildMap = parentChildMap;
		}

		/**
		 * Returns a copy of the cached children list.
		 *
		 * @return list of child candidates
		 */
		List<JvmNodeCandidate> getChildren() {
			return new ArrayList<>(children);
		}

		/**
		 * Returns a copy of the cached parent-child map.
		 *
		 * @return map of parent-child relationships
		 */
		Map<JvmNodeCandidate, List<JvmNodeCandidate>> getParentChildMap() {
			return new HashMap<>(parentChildMap);
		}
	}
}

