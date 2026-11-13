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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * The keys Fixture Monkey nests a seed scope by, other than a node's path and its {@link SeedPurpose}s. Each key
 * gives what it scopes a scope of its own, so it never draws what another does.
 */
@API(since = "1.2.4", status = Status.INTERNAL)
public enum SeedScopeKey {
	SAMPLE("sample"),
	CHILD("child"),
	FIX("fix"),
	NESTED_BUILDER("builder"),
	LAZY("lazy"),
	INTROSPECTION("introspection"),
	RESOLVER("resolver");

	private final long key;

	SeedScopeKey(String name) {
		this.key = name.hashCode();
	}

	/**
	 * Returns the key to pass to {@link com.navercorp.objectfarm.api.node.SeedSnapshot#scope(long)}.
	 *
	 * @return the key
	 */
	public long key() {
		return key;
	}
}
