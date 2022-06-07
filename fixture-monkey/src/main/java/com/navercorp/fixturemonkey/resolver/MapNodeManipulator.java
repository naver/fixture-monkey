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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class MapNodeManipulator implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	private final List<NodeManipulator> keyManipulators;
	private final List<NodeManipulator> valueManipulators;

	public MapNodeManipulator(
		ArbitraryTraverser traverser,
		List<NodeManipulator> keyManipulators,
		List<NodeManipulator> valueManipulators
	) {
		this.traverser = traverser;
		this.keyManipulators = keyManipulators;
		this.valueManipulators = valueManipulators;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		AddMapEntryNodeManipulator addMapEntryManipulator = new AddMapEntryNodeManipulator(traverser);
		addMapEntryManipulator.manipulate(arbitraryNode);

		ArbitraryNode entryNode = arbitraryNode.getChildren().get(arbitraryNode.getChildren().size() - 1);
		for (NodeManipulator keyManipulator : keyManipulators) {
			keyManipulator.manipulate(entryNode.getChildren().get(0));
		}
		for (NodeManipulator valueManipulator : valueManipulators) {
			valueManipulator.manipulate(entryNode.getChildren().get(1));
		}
	}
}
