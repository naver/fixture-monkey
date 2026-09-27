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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.planner.LazyValueHolder;

/**
 * Helpers around {@link LazyValueHolder} resolution.
 *
 * <p>Stateless: every method takes the {@link AssemblyState} (which owns the per-call
 * {@code resolvedLazyCache}) explicitly so the assembler keeps tree-traversal as its sole
 * responsibility.</p>
 */
final class LazyResolver {
	/**
	 * Sentinel value returned when recursion guard blocks evaluation.
	 * Callers must check for this value to distinguish "recursion blocked" from
	 * "supplier intentionally returned null".
	 */
	static final Object RECURSION_BLOCKED = new Object();

	/**
	 * Thread-local set to track which scopes are currently being lazily evaluated.
	 * Only the same scope's nested evaluation is skipped to prevent infinite recursion,
	 * while different scopes can be resolved even during lazy evaluation.
	 */
	private static final ThreadLocal<@Nullable Set<Object>> EVALUATING_LAZY_SCOPES =
		ThreadLocal.withInitial(HashSet::new);

	private LazyResolver() {
	}

	static @Nullable Object resolveLazyValue(@Nullable Object value, DirectivePrecedence order, AssemblyState state) {
		if (!(value instanceof LazyValueHolder)) {
			return value;
		}
		return order.isRootScope()
			? resolveLazyWithCache((LazyValueHolder)value, state)
			: evaluate((LazyValueHolder)value);
	}

	static @Nullable Object resolveLazyValueWithCache(@Nullable Object value, AssemblyState state) {
		return value instanceof LazyValueHolder ? resolveLazyWithCache((LazyValueHolder)value, state) : value;
	}

	static @Nullable Object resolveLazyWithCache(LazyValueHolder holder, AssemblyState state) {
		if (state.resolvedLazyCache.containsKey(holder)) {
			return state.resolvedLazyCache.get(holder);
		}
		Object resolved = evaluate(holder);
		if (resolved != null && resolved != RECURSION_BLOCKED) {
			state.resolvedLazyCache.put(holder, resolved);
		}
		return resolved;
	}

	/**
	 * Gets the lazy value with recursion protection (root-level only).
	 * Always clears the shared LazyArbitrary before evaluation to ensure a fresh value,
	 * preventing stale cached values from leaking across sample() calls or nested evaluations.
	 * <p>
	 * Field-level lazies (e.g., setLazy("field", ...)) are evaluated directly without
	 * recursion guard, because they are simple value suppliers. Only root-level lazies
	 * (e.g., thenApply which internally calls sample()) need recursion protection.
	 *
	 * @return the evaluated value (may be null if supplier returns null),
	 *         or {@link #RECURSION_BLOCKED} if evaluation would cause recursion for the same scope
	 */
	static @Nullable Object evaluate(LazyValueHolder holder) {
		LazyArbitrary<?> lazyArbitrary = holder.getLazyArbitrary();
		if (!holder.isRootLevel()) {
			return evaluateFresh(lazyArbitrary);
		}
		return evaluateWithRecursionGuard(holder.getScope(), () -> evaluateFresh(lazyArbitrary));
	}

	private static @Nullable Object evaluateFresh(LazyArbitrary<?> lazyArbitrary) {
		lazyArbitrary.clear();
		Object value = lazyArbitrary.getValue();
		lazyArbitrary.clear();
		return value;
	}

	private static @Nullable Object evaluateWithRecursionGuard(Object scope, Supplier<@Nullable Object> action) {
		Set<Object> evaluatingScopes = EVALUATING_LAZY_SCOPES.get();

		if (evaluatingScopes == null || evaluatingScopes.contains(scope)) {
			return RECURSION_BLOCKED;
		}

		try {
			evaluatingScopes.add(scope);
			return action.get();
		} finally {
			evaluatingScopes.remove(scope);
		}
	}
}
