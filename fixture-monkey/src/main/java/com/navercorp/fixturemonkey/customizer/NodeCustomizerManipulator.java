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

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.tree.GenerateFixtureContext;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@API(since = "1.0.9", status = Status.EXPERIMENTAL)
public final class NodeCustomizerManipulator<T> implements NodeManipulator {
	private final Function<CombinableArbitrary<? extends T>, CombinableArbitrary<? extends T>> arbitraryCustomizer;

	public NodeCustomizerManipulator(
		Function<CombinableArbitrary<? extends T>, CombinableArbitrary<? extends T>> arbitraryCustomizer
	) {
		this.arbitraryCustomizer = arbitraryCustomizer;
	}

	@SuppressWarnings({"rawtypes", "unchecked", "argument"})
	@Override
	public void manipulate(ObjectNode objectNode) {
		GenerateFixtureContext generateFixtureContext = (GenerateFixtureContext)objectNode.getObjectNodeContext();
		if (generateFixtureContext.getArbitrary() != null) {
			CombinableArbitrary<? extends T> customized = arbitraryCustomizer.apply(
				(CombinableArbitrary<? extends T>)generateFixtureContext.getArbitrary());
			generateFixtureContext.setArbitrary(customized);
		} else {
			generateFixtureContext.addGeneratedArbitraryCustomizer((Function)arbitraryCustomizer);
		}
	}
}
