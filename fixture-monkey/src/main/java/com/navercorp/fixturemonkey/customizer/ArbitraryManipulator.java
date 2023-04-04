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

import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryManipulator {
	private final NodeResolver nodeResolver;
	private final NodeManipulator nodeManipulator;

	ArbitraryManipulator(NodeResolver nodeResolver, NodeManipulator nodeManipulator) {
		this.nodeResolver = nodeResolver;
		this.nodeManipulator = nodeManipulator;
	}

	ArbitraryManipulator withPrependNodeResolver(NodeResolver nodeResolver) {
		return new ArbitraryManipulator(
			new CompositeNodeResolver(nodeResolver, this.nodeResolver),
			this.nodeManipulator
		);
	}

	public NodeResolver getNodeResolver() {
		return nodeResolver;
	}

	public NodeManipulator getNodeManipulator() {
		return nodeManipulator;
	}

	public void manipulate(ObjectTree tree) {
		tree.manipulate(nodeResolver, nodeManipulator);
	}
}
