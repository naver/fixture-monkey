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

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

/**
 * Holds a LazyArbitrary for deferred evaluation during assembly.
 * This allows lazy values to participate in the type-based priority mechanism.
 * <p>
 * The scope identifies the declaration the lazy belongs to, and a root-level lazy stands for the whole object
 * of its scope (e.g. thenApply) rather than a single field.
 */
public final class LazyValueHolder {
	private final LazyArbitrary<?> lazyArbitrary;
	private final Object scope;
	private final boolean rootLevel;

	public LazyValueHolder(LazyArbitrary<?> lazyArbitrary, Object scope, boolean rootLevel) {
		this.lazyArbitrary = lazyArbitrary;
		this.scope = scope;
		this.rootLevel = rootLevel;
	}

	public LazyArbitrary<?> getLazyArbitrary() {
		return lazyArbitrary;
	}

	public Object getScope() {
		return scope;
	}

	public boolean isRootLevel() {
		return rootLevel;
	}
}
