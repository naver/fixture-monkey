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

package com.navercorp.fixturemonkey.tree;

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.node.SeedSnapshot;

/**
 * A seed scope that hands out the scopes nested in it one by one for each {@link SeedScopeKey}. The n-th scope handed
 * out for a key is always the same, so what draws from it depends only on how many came before it for that key.
 */
@API(since = "1.2.4", status = Status.INTERNAL)
public final class SeedScopeSequence {
	private final SeedSnapshot scope;
	private final AtomicIntegerArray counts = new AtomicIntegerArray(SeedScopeKey.values().length);

	public SeedScopeSequence(SeedSnapshot scope) {
		this.scope = scope;
	}

	/**
	 * Returns the scope the scopes are nested in.
	 *
	 * @return the scope
	 */
	public SeedSnapshot getScope() {
		return scope;
	}

	/**
	 * Returns the next scope for {@code key}.
	 *
	 * @param key the key to hand out the scope for
	 * @return the next scope for the key
	 */
	public SeedSnapshot next(SeedScopeKey key) {
		return scope.scope(key.key()).scope(counts.getAndIncrement(key.ordinal()));
	}
}
