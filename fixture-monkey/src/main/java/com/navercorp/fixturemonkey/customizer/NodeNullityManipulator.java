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

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.tree.ObjectNode;

public class NodeNullityManipulator implements NodeManipulator {
	private final boolean toNull;

	public NodeNullityManipulator(boolean toNull) {
		this.toNull = toNull;
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		ArbitraryProperty arbitraryProperty = objectNode.getArbitraryProperty();
		if (toNull) {
			objectNode.setArbitraryProperty(arbitraryProperty.withNullInject(ALWAYS_NULL_INJECT));
		} else {
			if (NOT_GENERATED.equals(objectNode.getArbitrary())) {
				objectNode.setArbitrary(null); // initializing for regenerate
			}
			objectNode.setArbitraryProperty(arbitraryProperty.withNullInject(NOT_NULL_INJECT));
		}
	}
}
