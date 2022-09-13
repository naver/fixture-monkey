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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

public class NodeNullityManipulator implements NodeManipulator {
	private final boolean toNull;

	public NodeNullityManipulator(boolean toNull) {
		this.toNull = toNull;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		if (toNull) {
			arbitraryNode.setArbitraryProperty(arbitraryProperty.withNullInject(ALWAYS_NULL_INJECT));
		} else {
			if (arbitraryNode.getArbitrary() != null) {
				//noinspection ConstantConditions
				if (arbitraryNode.getArbitrary().sample() == null) { // without nullInject
					arbitraryNode.setArbitrary(null);
				}
			}
			arbitraryNode.setArbitraryProperty(arbitraryProperty.withNullInject(NOT_NULL_INJECT));
		}
	}
}
