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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class DefaultNodeResolver implements NodeResolver {
	private final NextNodePredicate nextNodePredicate;

	public DefaultNodeResolver(NextNodePredicate nextNodePredicate) {
		this.nextNodePredicate = nextNodePredicate;
	}

	@Override
	public List<ObjectNode> resolve(ObjectNode objectNode) {
		List<ObjectNode> resolved = objectNode.getChildren().stream()
			.filter(it -> nextNodePredicate.test(it.getArbitraryProperty().getObjectProperty()))
			.collect(Collectors.toList());

		objectNode.setManipulated(true);
		objectNode.setArbitraryProperty(objectNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		for (ObjectNode node : resolved) {
			node.setManipulated(true);
			node.setArbitraryProperty(node.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		}

		return resolved;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DefaultNodeResolver that = (DefaultNodeResolver)obj;
		return nextNodePredicate.equals(that.nextNodePredicate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nextNodePredicate);
	}

	@Override
	public List<NextNodePredicate> toNextNodePredicate() {
		return Collections.singletonList(nextNodePredicate);
	}
}
