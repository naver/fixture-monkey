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

package com.navercorp.fixturemonkey.resolver;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetLazyManipulator<T> implements NodeManipulator {
	private final int sequence;
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;
	private final LazyArbitrary<T> lazyArbitrary;

	public NodeSetLazyManipulator(
		int sequence,
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions,
		LazyArbitrary<T> lazyArbitrary
	) {
		this.sequence = sequence;
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
		this.lazyArbitrary = lazyArbitrary;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		T value = lazyArbitrary.getValue();

		if (value == null) {
			NodeNullityManipulator nullityManipulator = new NodeNullityManipulator(true);
			nullityManipulator.manipulate(arbitraryNode);
			return;
		}

		if (value instanceof Arbitrary) {
			value = (T)((Arbitrary<?>)value).sample();
		}

		NodeSetDecomposedValueManipulator<T> nodeSetDecomposedValueManipulator =
			new NodeSetDecomposedValueManipulator<>(sequence, traverser, manipulateOptions, value);
		nodeSetDecomposedValueManipulator.manipulate(arbitraryNode);
		lazyArbitrary.clear();
	}
}
