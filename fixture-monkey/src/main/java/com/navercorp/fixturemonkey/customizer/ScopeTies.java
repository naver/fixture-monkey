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

package com.navercorp.fixturemonkey.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.tree.SeedPurpose;
import com.navercorp.objectfarm.api.node.SeedSnapshot;

/**
 * Picks one of the defined scopes of the same priority that select the same node, so only the picked one applies
 * there. The pick is drawn from the seed scope of the node, so the same seed picks the same scope at the same node,
 * and nodes are picked for on their own.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class ScopeTies {
	private static final ScopeTies NONE = new ScopeTies(Collections.emptyMap(), new SeedSnapshot(0L, 0L));

	private final Map<ScopeSelector, TiedGroup> groupBySelector;
	private final SeedSnapshot sampleScope;

	private ScopeTies(Map<ScopeSelector, TiedGroup> groupBySelector, SeedSnapshot sampleScope) {
		this.groupBySelector = groupBySelector;
		this.sampleScope = sampleScope;
	}

	/**
	 * Returns the ties where no two scopes are tied, so every scope selects what its selector selects.
	 *
	 * @return the ties without any tie
	 */
	public static ScopeTies none() {
		return NONE;
	}

	/**
	 * Returns the ties among {@code definedScopes}: scopes of the same priority are tied wherever they select the
	 * same node.
	 *
	 * @param definedScopes the defined scopes of a sample
	 * @param sampleScope   the seed scope of the sample
	 * @return the ties
	 */
	public static ScopeTies of(List<Scope> definedScopes, SeedSnapshot sampleScope) {
		Map<Integer, List<ScopeSelector>> selectorsByPriority = new LinkedHashMap<>();
		for (Scope scope : definedScopes) {
			selectorsByPriority.computeIfAbsent(scope.getPriority(), it -> new ArrayList<>()).add(scope.getSelector());
		}
		Map<ScopeSelector, TiedGroup> groupBySelector = new IdentityHashMap<>();
		for (Map.Entry<Integer, List<ScopeSelector>> samePriority : selectorsByPriority.entrySet()) {
			List<ScopeSelector> selectors = samePriority.getValue();
			if (selectors.size() < 2) {
				continue;
			}
			TiedGroup group = new TiedGroup(samePriority.getKey(), selectors);
			for (ScopeSelector selector : selectors) {
				groupBySelector.put(selector, group);
			}
		}
		if (groupBySelector.isEmpty()) {
			return NONE;
		}
		return new ScopeTies(groupBySelector, sampleScope);
	}

	/**
	 * Returns whether {@code selector}, which selects the node at {@code depth} of {@code chain}, is the one picked
	 * there among the scopes tied with it.
	 */
	boolean picks(ScopeSelector selector, int depth, ScopeChain chain) {
		TiedGroup group = groupBySelector.get(selector);
		if (group == null) {
			return true;
		}
		return chain.pickedAt(depth, group.priority, () -> pick(group, depth, chain)) == selector;
	}

	private @Nullable ScopeSelector pick(TiedGroup group, int depth, ScopeChain chain) {
		List<ScopeSelector> selecting = new ArrayList<>(group.selectors.size());
		for (ScopeSelector tied : group.selectors) {
			if (chain.selectsRegardlessOfTies(tied, depth)) {
				selecting.add(tied);
			}
		}
		if (selecting.size() < 2) {
			return selecting.isEmpty() ? null : selecting.get(0);
		}
		Random random = SeedPurpose.SCOPE.randomFor(chain.seedScopeAt(sampleScope, depth), group.priority);
		return selecting.get(random.nextInt(selecting.size()));
	}

	private static final class TiedGroup {
		private final int priority;
		private final List<ScopeSelector> selectors;

		private TiedGroup(int priority, List<ScopeSelector> selectors) {
			this.priority = priority;
			this.selectors = selectors;
		}
	}
}
