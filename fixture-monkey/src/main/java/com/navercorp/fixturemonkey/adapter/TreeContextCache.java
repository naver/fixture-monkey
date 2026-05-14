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

package com.navercorp.fixturemonkey.adapter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeSubtreeContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Caches reusable {@link JvmNodeContext}s and {@link JvmNodeCandidateTree}s for the planning phase.
 * <p>
 * Caches are keyed by {@code (JvmType, FixtureMonkeyOptions identity)}. Per-call options
 * such as {@code propertyConfigurers} or {@code introspectorsByType} bypass the cache and
 * trigger a fresh build, since they alter how the context is constructed.
 * <p>
 * Owns the long-lived {@link JvmNodeCandidateTreeContext} (subtree caching for candidate trees)
 * and {@link JvmNodeSubtreeContext} (promoted POJO subtree snapshots for tree transformation).
 * Both are exposed for use during {@code JvmNodeTreeTransformer} setup.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class TreeContextCache {
	private final JvmNodeCandidateTreeContext treeContext;
	private final JvmNodeSubtreeContext subtreeContext;
	private final NodeContextFactory nodeContextFactory;

	private final Map<Key, JvmNodeContext> nodeContextCache;
	private final Map<Key, JvmNodeCandidateTree> candidateTreeCache;
	private final Map<Key, JvmNodeCandidateTree> concreteTypeCandidateTreeCache;

	public TreeContextCache(JvmNodeCandidateTreeContext treeContext, NodeContextFactory nodeContextFactory) {
		this.treeContext = treeContext;
		this.subtreeContext = new JvmNodeSubtreeContext();
		this.nodeContextFactory = nodeContextFactory;
		this.nodeContextCache = new ConcurrentHashMap<>();
		this.candidateTreeCache = new ConcurrentHashMap<>();
		this.concreteTypeCandidateTreeCache = new ConcurrentHashMap<>();
	}

	public JvmNodeCandidateTreeContext getTreeContext() {
		return treeContext;
	}

	public JvmNodeSubtreeContext getSubtreeContext() {
		return subtreeContext;
	}

	/**
	 * Returns a cached {@link JvmNodeContext} for the given root type and options, or builds and caches one.
	 * <p>
	 * Bypasses the cache when {@code propertyConfigurers} or {@code introspectorsByType} are non-empty,
	 * since those alter context construction.
	 */
	public JvmNodeContext getOrBuildNodeContext(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType
	) {
		if (!propertyConfigurers.isEmpty() || !introspectorsByType.isEmpty()) {
			return nodeContextFactory.build(rootType, options, propertyConfigurers, introspectorsByType);
		}

		Key cacheKey = new Key(rootType, options);
		return nodeContextCache.computeIfAbsent(cacheKey, key ->
			nodeContextFactory.build(rootType, options, propertyConfigurers, introspectorsByType)
		);
	}

	/**
	 * Returns a cached {@link JvmNodeCandidateTree} for the resolved root type, or builds and caches one.
	 * <p>
	 * When {@code hasCustomConfigurers} is true, uses a fresh {@link JvmNodeCandidateTreeContext} and
	 * skips caching to avoid leaking per-call configuration into shared state.
	 */
	public JvmNodeCandidateTree getOrBuildCandidateTree(
		JvmType resolvedRootType,
		JvmNodeContext context,
		@Nullable FixtureMonkeyOptions options,
		boolean hasCustomConfigurers
	) {
		if (hasCustomConfigurers) {
			return buildCandidateTree(resolvedRootType, context, new JvmNodeCandidateTreeContext());
		}

		Key cacheKey = new Key(resolvedRootType, options);
		return candidateTreeCache.computeIfAbsent(cacheKey, key ->
			buildCandidateTree(resolvedRootType, context, treeContext)
		);
	}

	/**
	 * Returns a cached {@link JvmNodeCandidateTree} for the concrete type produced during interface resolution,
	 * or builds and caches one.
	 */
	public JvmNodeCandidateTree getOrBuildConcreteCandidateTree(
		JvmType concreteType,
		JvmNodeContext context,
		@Nullable FixtureMonkeyOptions options
	) {
		Key cacheKey = new Key(concreteType, options);
		return concreteTypeCandidateTreeCache.computeIfAbsent(cacheKey, key ->
			buildCandidateTree(concreteType, context, treeContext)
		);
	}

	/**
	 * Clears all caches and the subtree context.
	 */
	public void clear() {
		nodeContextCache.clear();
		candidateTreeCache.clear();
		concreteTypeCandidateTreeCache.clear();
		subtreeContext.clear();
	}

	private static JvmNodeCandidateTree buildCandidateTree(
		JvmType type,
		JvmNodeContext context,
		JvmNodeCandidateTreeContext candidateTreeContext
	) {
		return new JvmNodeCandidateTree.Builder(type, context)
			.withTreeContext(candidateTreeContext)
			.withPreBuildResolvedTypes(true)
			.build();
	}

	/**
	 * Cache key for {@link JvmNodeCandidateTree} and {@link JvmNodeContext} entries.
	 * Uses options identity hash because for the same FixtureMonkey instance,
	 * options is always the same object reference.
	 */
	private static final class Key {
		private final JvmType type;
		private final int optionsIdentity;
		private final int hashCode;

		Key(JvmType type, @Nullable FixtureMonkeyOptions options) {
			this.type = type;
			this.optionsIdentity = options != null ? System.identityHashCode(options) : 0;
			this.hashCode = computeHashCode();
		}

		private int computeHashCode() {
			int result = type.hashCode();
			result = 31 * result + optionsIdentity;
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Key that = (Key)obj;
			return optionsIdentity == that.optionsIdentity && Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
