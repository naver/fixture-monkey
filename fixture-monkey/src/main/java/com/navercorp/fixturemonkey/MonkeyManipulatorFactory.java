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

package com.navercorp.fixturemonkey;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;

@API(since = "0.4.10", status = Status.EXPERIMENTAL)
public final class MonkeyManipulatorFactory {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;

	public MonkeyManipulatorFactory(ArbitraryTraverser traverser, ManipulateOptions manipulateOptions) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
	}

	public NodeManipulator convertToNodeManipulator(@Nullable Object value) {
		if (value instanceof Arbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy(() -> ((Arbitrary<?>)value).sample()),
				false
			);
		} else if (value instanceof Supplier) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				LazyArbitrary.lazy((Supplier<?>)value),
				false
			);
		} else if (value instanceof LazyArbitrary) {
			return new NodeSetLazyManipulator<>(
				traverser,
				manipulateOptions,
				(LazyArbitrary<?>)value,
				false
			);
		} else if (value == null) {
			return new NodeNullityManipulator(true);
		} else {
			return new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value, false);
		}
	}
}
