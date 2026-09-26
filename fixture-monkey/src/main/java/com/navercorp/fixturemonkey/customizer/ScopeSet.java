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
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;

/**
 * The scopes that apply to one sample: the root scope and the defined scopes, sorted by precedence. Each scope knows
 * only what it declared; which scope's {@code instantiate(...)} applies where is decided here, since it takes
 * comparing the scopes, and planning and assembly both ask here so they follow the same declaration.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public final class ScopeSet {
	/**
	 * What the sampled builder declared: its path directives in declared order and its instantiate() declarations.
	 */
	private final Scope rootScope;

	/**
	 * The defined scopes that apply, the one with the lowest precedence first: a higher priority number first, and
	 * the earlier declared one first at the same priority.
	 */
	private final List<Scope> definedScopes;
	private final Map<Class<?>, InstantiatorProcessResult> globalInstantiators;
	private final List<Scope> scopesDeclaringAtNodes;
	private final Map<Scope, Map<Class<?>, Map<Class<?>, InstantiatorProcessResult>>> scopedInstantiatorsByScope =
		new IdentityHashMap<>();

	public ScopeSet(Scope rootScope, List<Scope> definedScopes) {
		this.rootScope = rootScope;
		List<Scope> lowestPrecedenceFirst = new ArrayList<>(definedScopes);
		lowestPrecedenceFirst.sort(Comparator.comparingInt(Scope::getPriority).reversed());
		this.definedScopes = lowestPrecedenceFirst;

		// A scope that selects every node of a type declares for all of them, so its declaration joins the global
		// instantiators: planning then keeps one cached node context instead of looking each node up
		Map<Class<?>, InstantiatorProcessResult> global = new HashMap<>();
		List<Scope> declaringAtNodes = new ArrayList<>();
		for (Scope scope : lowestPrecedenceFirst) {
			ScopeSelector selector = scope.getSelector();
			boolean declaresAtNodes = false;
			for (Map.Entry<Class<?>, InstantiatorProcessResult> declared : scope.getInstantiators().entrySet()) {
				Class<?> type = declared.getKey();
				if (declaredByRoot(type)) {
					continue;
				}
				if (selector.selectsEveryNodeOf(type)) {
					global.put(type, declared.getValue());
				} else {
					declaresAtNodes = true;
				}
			}
			if (declaresAtNodes) {
				declaringAtNodes.add(0, scope);
			}
		}
		global.putAll(rootScope.getInstantiators());
		this.globalInstantiators = Collections.unmodifiableMap(global);
		this.scopesDeclaringAtNodes = Collections.unmodifiableList(declaringAtNodes);
	}

	public Scope getRootScope() {
		return rootScope;
	}

	/**
	 * Returns the defined scopes, the one with the lowest precedence first.
	 */
	public List<Scope> getDefinedScopes() {
		return definedScopes;
	}

	/**
	 * Returns the instantiators that apply to every instance of their type: the root scope's, over the ones a defined
	 * scope declared for every node of a type it selects.
	 */
	public Map<Class<?>, InstantiatorProcessResult> getGlobalInstantiators() {
		return globalInstantiators;
	}

	/**
	 * Returns whether a defined scope declared an instantiator that applies only inside the nodes it selects.
	 */
	public boolean hasScopedInstantiators() {
		return !scopesDeclaringAtNodes.isEmpty();
	}

	/**
	 * Returns the instantiators an instance of {@code type} builds with when a defined scope selecting a node on its
	 * chain declared one for it: the outermost scope node wins, and at the same node the scope with the highest
	 * precedence. The same declaration always gives the same instance.
	 *
	 * @param type  the type of the instance
	 * @param chain the nodes from the outermost ancestor down to the instance's own node
	 * @return the declaring scope's instantiators over the global ones, or null when the global instantiators apply
	 */
	public @Nullable Map<Class<?>, InstantiatorProcessResult> scopedInstantiatorsAt(Class<?> type, ScopeChain chain) {
		for (int depth = 0; depth <= chain.depth(); depth++) {
			for (Scope scope : scopesDeclaringAtNodes) {
				if (declaresAtNodes(scope, type) && chain.selects(scope.getSelector(), depth)) {
					return scopedInstantiatorsOf(scope, type);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the instantiators an instance of {@code type} builds with where it sits.
	 */
	public Map<Class<?>, InstantiatorProcessResult> instantiatorsAt(Class<?> type, ScopeChain chain) {
		Map<Class<?>, InstantiatorProcessResult> scoped = scopedInstantiatorsAt(type, chain);
		return scoped != null ? scoped : globalInstantiators;
	}

	private boolean declaredByRoot(Class<?> type) {
		return rootScope.getInstantiators().containsKey(type);
	}

	private boolean declaresAtNodes(Scope scope, Class<?> type) {
		return scope.getInstantiators().containsKey(type)
			&& !declaredByRoot(type)
			&& !scope.getSelector().selectsEveryNodeOf(type);
	}

	private Map<Class<?>, InstantiatorProcessResult> scopedInstantiatorsOf(Scope scope, Class<?> type) {
		return scopedInstantiatorsByScope
			.computeIfAbsent(scope, it -> new HashMap<>())
			.computeIfAbsent(type, it -> {
				Map<Class<?>, InstantiatorProcessResult> scoped = new HashMap<>(globalInstantiators);
				scoped.put(type, scope.getInstantiators().get(type));
				return Collections.unmodifiableMap(scoped);
			});
	}
}
