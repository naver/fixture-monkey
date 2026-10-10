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

package com.navercorp.fixturemonkey.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.objectfarm.api.node.SeedState;

final class NamedScopeResolver {
	private final SeedState seedState;

	NamedScopeResolver(SeedState seedState) {
		this.seedState = seedState;
	}

	List<Scope> resolve(List<Scope> definedScopes, List<String> selectedNames) {
		Map<ScopeSelector, List<Scope>> alternativesBySelector = new LinkedHashMap<>();
		for (Scope scope : definedScopes) {
			if (scope.getName() != null) {
				alternativesBySelector.computeIfAbsent(scope.getSelector(), it -> new ArrayList<>()).add(scope);
			}
		}
		if (alternativesBySelector.isEmpty()) {
			return definedScopes;
		}

		Set<Scope> chosen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (List<Scope> alternatives : alternativesBySelector.values()) {
			if (!anySelected(alternatives, selectedNames)) {
				chosen.add(choose(alternatives));
			}
		}

		List<Scope> resolved = new ArrayList<>(definedScopes.size());
		for (Scope scope : definedScopes) {
			String name = scope.getName();
			if (name == null || chosen.contains(scope)) {
				resolved.add(scope);
				continue;
			}
			int selectedIndex = selectedNames.indexOf(name);
			if (selectedIndex >= 0) {
				resolved.add(scope.selected(selectedIndex + 1));
			}
		}
		return resolved;
	}

	private static boolean anySelected(List<Scope> alternatives, List<String> selectedNames) {
		for (Scope alternative : alternatives) {
			String name = alternative.getName();
			if (name != null && selectedNames.contains(name)) {
				return true;
			}
		}
		return false;
	}

	private Scope choose(List<Scope> alternatives) {
		int lowestPriority = Integer.MAX_VALUE;
		for (Scope alternative : alternatives) {
			lowestPriority = Math.min(lowestPriority, alternative.getPriority());
		}
		List<Scope> candidates = new ArrayList<>(alternatives.size());
		for (Scope alternative : alternatives) {
			if (alternative.getPriority() == lowestPriority) {
				candidates.add(alternative);
			}
		}
		if (candidates.size() == 1) {
			return candidates.get(0);
		}
		int selectorHash = candidates.get(0).getSelector().toExpression().hashCode();
		return pickByWeight(candidates, seedState.alternativeSnapshot().randomFor(selectorHash));
	}

	static Scope pickByWeight(List<Scope> candidates, Random random) {
		long totalWeight = 0;
		for (Scope candidate : candidates) {
			totalWeight += candidate.getWeight();
		}
		long point = Math.floorMod(random.nextLong(), totalWeight);
		for (Scope candidate : candidates) {
			point -= candidate.getWeight();
			if (point < 0) {
				return candidate;
			}
		}
		return candidates.get(candidates.size() - 1);
	}
}
