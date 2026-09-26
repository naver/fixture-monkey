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

import java.util.Random;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.objectfarm.api.node.SeedSnapshot;

/**
 * The purposes a node's seed scope is split by. Each purpose draws from its own scope nested in the node's, so the
 * size, the implementation and the value at a node are independent of each other.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public enum SeedPurpose {
	SIZE,
	IMPLEMENTATION,
	VALUE;

	private static final long RESOLVER = "resolver".hashCode();

	/**
	 * Returns the random a node draws from for this purpose.
	 *
	 * @param nodeScope the seed scope of the node
	 * @param typeHash  the hash of the type drawn for
	 * @return the random for this purpose
	 */
	public Random randomFor(SeedSnapshot nodeScope, int typeHash) {
		return scopeIn(nodeScope).randomFor(typeHash);
	}

	/**
	 * Returns the seed a node draws from for this purpose.
	 *
	 * @param nodeScope the seed scope of the node
	 * @return the seed for this purpose
	 */
	public long seedFor(SeedSnapshot nodeScope) {
		return scopeIn(nodeScope).seedFor(0);
	}

	/**
	 * Returns the scope a node draws from for this purpose.
	 *
	 * @param nodeScope the seed scope of the node
	 * @return the scope of this purpose at the node
	 */
	public SeedSnapshot scopeIn(SeedSnapshot nodeScope) {
		return nodeScope.scope(name().hashCode());
	}

	/**
	 * Returns the scope what a resolver or a plugin draws at a node for this purpose comes from, run with
	 * {@link SeedSnapshot#runIn}. It is apart from {@link #randomFor} and {@link #seedFor}, so it does not repeat what
	 * Fixture Monkey draws itself.
	 *
	 * @param nodeScope the seed scope of the node
	 * @param typeHash  the hash of the type drawn for
	 * @return the scope to run the resolver in
	 */
	public SeedSnapshot resolverScopeIn(SeedSnapshot nodeScope, int typeHash) {
		return scopeIn(nodeScope).scope(RESOLVER).scope(typeHash);
	}
}
