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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

/**
 * Holds a LazyArbitrary for deferred evaluation during assembly.
 * This allows lazy values to participate in the type-based priority mechanism.
 * <p>
 * Includes recursion protection for root-level lazies: if the same owner type
 * is already being evaluated, returns {@link #RECURSION_BLOCKED} instead of causing infinite recursion.
 * Field-level lazies skip the recursion guard because they are simple value suppliers
 * that do not trigger nested assembly of the same type.
 */
public final class LazyValueHolder {
	/**
	 * Sentinel value returned when recursion guard blocks evaluation.
	 * Callers must check for this value to distinguish "recursion blocked" from
	 * "supplier intentionally returned null".
	 */
	public static final Object RECURSION_BLOCKED = new Object();

	/**
	 * Thread-local set to track which types are currently being lazily evaluated.
	 * Only the same type's nested evaluation is skipped to prevent infinite recursion,
	 * while different types can be resolved even during lazy evaluation.
	 */
	private static final ThreadLocal<@Nullable Set<Class<?>>> EVALUATING_LAZY_TYPES =
		ThreadLocal.withInitial(HashSet::new);

	private final LazyArbitrary<?> lazyArbitrary;
	private final Class<?> ownerType;
	private final boolean rootLevel;

	public LazyValueHolder(LazyArbitrary<?> lazyArbitrary, Class<?> ownerType, boolean rootLevel) {
		this.lazyArbitrary = lazyArbitrary;
		this.ownerType = ownerType;
		this.rootLevel = rootLevel;
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
	 *         or {@link #RECURSION_BLOCKED} if evaluation would cause recursion for the same type
	 */
	public @Nullable Object getValue() {
		if (!rootLevel) {
			lazyArbitrary.clear();
			Object value = lazyArbitrary.getValue();
			lazyArbitrary.clear();
			return value;
		}

		return evaluateWithRecursionGuard(ownerType, () -> {
			lazyArbitrary.clear();
			Object value = lazyArbitrary.getValue();
			lazyArbitrary.clear();
			return value;
		});
	}

	/**
	 * Executes the given action with recursion protection for the specified type.
	 * If the same type is already being evaluated on the current thread, returns
	 * {@link #RECURSION_BLOCKED} instead of causing infinite recursion.
	 *
	 * @param type   the type to guard against recursive evaluation
	 * @param action the action to execute
	 * @return the result of the action, or {@link #RECURSION_BLOCKED} if recursion was detected
	 */
	static @Nullable Object evaluateWithRecursionGuard(Class<?> type, Supplier<@Nullable Object> action) {
		Set<Class<?>> evaluatingTypes = EVALUATING_LAZY_TYPES.get();

		if (evaluatingTypes == null || evaluatingTypes.contains(type)) {
			return RECURSION_BLOCKED;
		}

		try {
			evaluatingTypes.add(type);
			return action.get();
		} finally {
			evaluatingTypes.remove(type);
		}
	}
}
