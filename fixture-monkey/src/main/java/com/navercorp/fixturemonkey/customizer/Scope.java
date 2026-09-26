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

import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;

/**
 * Directives and instantiators declared for the nodes a {@link ScopeSelector} selects, with paths relative to a
 * selected node.
 * <p>
 * What the sampled builder itself declared is the root scope: it applies at the node a sample starts from and wins
 * over every other scope. Any other scope is a defined scope, whose nodes the user defines: {@code register(...)}
 * declares one, the matcher it is registered with becomes the selector, and what its builder declared becomes the
 * directives and instantiators of the scope. Where defined scopes conflict at the same node, a scope with a lower
 * priority number takes precedence, and otherwise the later declared one.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class Scope {
	/**
	 * The priority of the root scope, lower than any {@code register(...)} priority can be, so it wins over every
	 * defined scope.
	 */
	public static final int ROOT_PRIORITY = Integer.MIN_VALUE;

	private final ScopeSelector selector;
	private final int priority;
	private final List<PathDirective> directives;
	private final Map<Class<?>, InstantiatorProcessResult> instantiators;

	/**
	 * Returns the root scope of what the sampled builder itself declared.
	 *
	 * @param directives    the directives the builder declared, in order
	 * @param instantiators the instantiators the builder declared
	 * @return the root scope
	 */
	public static Scope root(List<PathDirective> directives, Map<Class<?>, InstantiatorProcessResult> instantiators) {
		return new Scope(ScopeSelector.root(), ROOT_PRIORITY, directives, instantiators);
	}

	public Scope(
		ScopeSelector selector,
		int priority,
		List<PathDirective> directives,
		Map<Class<?>, InstantiatorProcessResult> instantiators
	) {
		this.selector = selector;
		this.priority = priority;
		this.directives = directives;
		this.instantiators = instantiators;
	}

	/**
	 * The selector of the nodes this scope applies to.
	 */
	public ScopeSelector getSelector() {
		return selector;
	}

	/**
	 * The priority of this scope; a lower number takes precedence.
	 */
	public int getPriority() {
		return priority;
	}

	public List<PathDirective> getDirectives() {
		return directives;
	}

	public Map<Class<?>, InstantiatorProcessResult> getInstantiators() {
		return instantiators;
	}
}
