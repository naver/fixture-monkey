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
package com.navercorp.fixturemonkey.tree;

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.type.Types.nullSafe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.SingleElementProperty;

/**
 * A {@link NodeResolver} that resolves the next nodes by the given {@link NextNodePredicate}.
 */
@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public final class NodePredicateResolver implements NodeResolver {
	private final NextNodePredicate nextNodePredicate;

	public NodePredicateResolver(NextNodePredicate nextNodePredicate) {
		this.nextNodePredicate = nextNodePredicate;
	}

	/**
	 * Resolves the next nodes by the given {@link NextNodePredicate}.
	 * If the {@code nextNodePredicate} is {@link StartNodePredicate}, it resolves the given {@code nextNode}.
	 *
	 * @param nextNode it may be the root node or the parent node resolved by the previous NodeResolver
	 * @return the next nodes that satisfy the given {@link NextNodePredicate},
	 * or an empty list if there are no such nodes. {@link StartNodePredicate} always returns given {@code nextNode}.
	 */
	@SuppressWarnings("dereference.of.nullable")
	@Override
	public List<ObjectNode> resolve(ObjectNode nextNode) {
		if (nextNodePredicate == StartNodePredicate.INSTANCE) {
			return Collections.singletonList(resolveStartNode(nextNode));
		}

		nextNode.expand();
		List<ObjectNode> resolved = nextNode.getChildren().asList().stream()
			.filter(it -> nextNodePredicate.test(it.getArbitraryProperty().getObjectProperty()))
			.collect(Collectors.toList());

		nextNode.setNullInject(NOT_NULL_INJECT);
		for (ObjectNode node : resolved) {
			node.setNullInject(NOT_NULL_INJECT);
		}

		return resolved;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		NodePredicateResolver that = (NodePredicateResolver)obj;
		return nextNodePredicate.equals(that.nextNodePredicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nextNodePredicate);
	}

	public ObjectNode resolveStartNode(ObjectNode startNode) {
		ObjectNode resultNode = getChildNodeIfWrapped(startNode);
		resultNode.setNullInject(NOT_NULL_INJECT);

		return resultNode;
	}

	private ObjectNode getChildNodeIfWrapped(ObjectNode objectNode) {
		ObjectNode searchNode = objectNode;

		searchNode.expand();
		while (isWrappedNode(searchNode)) {
			searchNode = nullSafe(searchNode.getChildren()).asList().get(0);
		}

		return searchNode;
	}

	private boolean isWrappedNode(ObjectNode searchNode) {
		List<ObjectNode> children = nullSafe(searchNode.getChildren()).asList();

		return children.size() == 1 && children.get(0).getResolvedProperty() instanceof SingleElementProperty;
	}
}
