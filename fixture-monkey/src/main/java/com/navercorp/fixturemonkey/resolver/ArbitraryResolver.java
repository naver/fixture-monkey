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

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryResolver {
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;

	public ArbitraryResolver(
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer
	) {
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
	}

	public Arbitrary<?> resolve(RootProperty rootProperty, List<BuilderManipulator> manipulators) {
		ArbitraryTree arbitraryTree = this.traverser.traverse(rootProperty);

		// manipulating 표현식 개수만큼 순회
		List<BuilderManipulator> optimizedManipulator = manipulatorOptimizer.optimize(manipulators).getManipulators();

		return arbitraryTree.generate();
	}
}
