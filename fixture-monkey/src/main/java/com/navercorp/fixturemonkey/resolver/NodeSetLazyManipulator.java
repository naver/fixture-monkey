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

import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class NodeSetLazyManipulator<T> implements NodeManipulator {
	private final Supplier<T> supplier;

	NodeSetLazyManipulator(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		T value = supplier.get();

		if (value instanceof Arbitrary) {
			NodeSetArbitraryManipulator<?> nodeSetArbitraryManipulator = new NodeSetArbitraryManipulator<>(
				(Arbitrary<?>)supplier.get());
			nodeSetArbitraryManipulator.manipulate(arbitraryNode);
			return;
		}

		NodeSetDecomposedValueManipulator<T> nodeSetDecomposedValueManipulator =
			new NodeSetDecomposedValueManipulator<>(value);
		nodeSetDecomposedValueManipulator.manipulate(arbitraryNode);
	}
}
