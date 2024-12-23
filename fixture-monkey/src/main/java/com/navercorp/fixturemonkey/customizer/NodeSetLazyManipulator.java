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

package com.navercorp.fixturemonkey.customizer;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.tree.GenerateFixtureContext;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class NodeSetLazyManipulator<T> implements NodeManipulator {
	private final int sequence;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;
	private final LazyArbitrary<T> lazyArbitrary;

	public NodeSetLazyManipulator(
		int sequence,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		LazyArbitrary<T> lazyArbitrary
	) {
		this.sequence = sequence;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.containerPropertyGenerators = containerPropertyGenerators;
		this.lazyArbitrary = lazyArbitrary;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void manipulate(ObjectNode objectNode) {
		T value = lazyArbitrary.getValue();

		if (value == null) {
			NodeNullityManipulator nullityManipulator = new NodeNullityManipulator(true);
			nullityManipulator.manipulate(objectNode);
			return;
		}

		if (value instanceof Arbitrary) {
			value = (T)((Arbitrary<?>)value).sample();
		}

		if (value instanceof Just) {
			Just just = (Just)value;
			GenerateFixtureContext generateFixtureContext = (GenerateFixtureContext)objectNode.getObjectNodeContext();
			generateFixtureContext.setArbitrary(CombinableArbitrary.from(just::getValue));
			lazyArbitrary.clear();
			return;
		}

		NodeSetDecomposedValueManipulator<T> nodeSetDecomposedValueManipulator =
			new NodeSetDecomposedValueManipulator<>(
				sequence,
				decomposedContainerValueFactory,
				containerPropertyGenerators,
				value
			);
		nodeSetDecomposedValueManipulator.manipulate(objectNode);
		lazyArbitrary.clear();
	}
}
