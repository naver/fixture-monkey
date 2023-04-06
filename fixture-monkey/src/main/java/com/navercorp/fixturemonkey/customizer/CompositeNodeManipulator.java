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

import java.util.Arrays;
import java.util.List;

import com.navercorp.fixturemonkey.tree.ObjectNode;

public final class CompositeNodeManipulator implements NodeManipulator {
	private final List<NodeManipulator> manipulators;

	public CompositeNodeManipulator(NodeManipulator... manipulators) {
		this.manipulators = Arrays.asList(manipulators);
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		for (NodeManipulator manipulator : manipulators) {
			manipulator.manipulate(objectNode);
		}
	}
}
