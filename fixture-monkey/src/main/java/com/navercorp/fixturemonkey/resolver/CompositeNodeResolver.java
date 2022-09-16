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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class CompositeNodeResolver implements NodeResolver {
	private final List<NodeResolver> nodeResolvers;

	public CompositeNodeResolver(NodeResolver... nodeResolvers) {
		this.nodeResolvers = Arrays.asList(nodeResolvers);
	}

	public CompositeNodeResolver(List<NodeResolver> nodeResolvers) {
		this.nodeResolvers = nodeResolvers;
	}

	@Override
	public List<ArbitraryNode> resolve(ArbitraryNode arbitraryNode) {
		LinkedList<ArbitraryNode> nextNodes = new LinkedList<>();
		nextNodes.add(arbitraryNode);
		for (NodeResolver nodeResolver : nodeResolvers) {
			List<ArbitraryNode> resolvedNodes = new LinkedList<>();
			while (!nextNodes.isEmpty()) {
				ArbitraryNode currentNode = nextNodes.pop();
				resolvedNodes.addAll(nodeResolver.resolve(currentNode));
			}
			nextNodes.addAll(resolvedNodes);
		}
		return nextNodes;
	}

	public List<NodeResolver> flatten() {
		List<NodeResolver> flatten = new ArrayList<>();

		for (NodeResolver nodeResolver : nodeResolvers) {
			if (nodeResolver instanceof CompositeNodeResolver) {
				flatten.addAll(((CompositeNodeResolver)nodeResolver).nodeResolvers);
			} else {
				flatten.add(nodeResolver);
			}
		}

		return flatten;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CompositeNodeResolver that = (CompositeNodeResolver)obj;
		return nodeResolvers.equals(that.nodeResolvers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeResolvers);
	}

	@Override
	public List<NextNodePredicate> toNextNodePredicate() {
		return flatten().stream()
			.flatMap(it -> it.toNextNodePredicate().stream())
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
}
