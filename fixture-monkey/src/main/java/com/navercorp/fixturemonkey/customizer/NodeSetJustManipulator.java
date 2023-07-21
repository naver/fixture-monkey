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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@API(since = "0.5.1", status = Status.MAINTAINED)
public final class NodeSetJustManipulator implements NodeManipulator {
	private final Just value;

	public NodeSetJustManipulator(Just value) {
		this.value = value;
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		Object exactValue = value.getValue();
		if (exactValue instanceof CombinableArbitrary) {
			objectNode.setArbitrary((CombinableArbitrary)exactValue);
		} else {
			objectNode.setArbitrary(CombinableArbitrary.from(exactValue));
		}
	}
}
