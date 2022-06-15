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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import java.util.LinkedList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeFieldResolver implements NodeResolver {
	private final String field;

	public NodeFieldResolver(String field) {
		this.field = field;
	}

	@Override
	public List<ArbitraryNode> resolve(ArbitraryTree arbitraryTree) {
		return null;
	}

	public List<ArbitraryNode> getNext(List<ArbitraryNode> nodes) {
		LinkedList<ArbitraryNode> nextNodes = new LinkedList<>();
		for (ArbitraryNode selectedNode : nodes) {
			List<ArbitraryNode> children = selectedNode.getChildren();
			for (ArbitraryNode child : children) {
				if (field.equals(child.getArbitraryProperty().getResolvePropertyName())) {
					child.setArbitraryProperty(child.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
					nextNodes.add(child);
				}
			}
		}
		return nextNodes;
	}
}
