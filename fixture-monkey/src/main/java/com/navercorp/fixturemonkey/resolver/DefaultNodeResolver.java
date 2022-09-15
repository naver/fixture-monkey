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

import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class DefaultNodeResolver implements NodeResolver {
	private final NextNodePredicate nextNodePredicate;

	public DefaultNodeResolver(NextNodePredicate nextNodePredicate) {
		this.nextNodePredicate = nextNodePredicate;
	}

	@Override
	public List<ArbitraryNode> resolve(ArbitraryNode arbitraryNode) {
		List<ArbitraryNode> resolved = arbitraryNode.getChildren().stream()
			.filter(it -> nextNodePredicate.test(
				arbitraryNode.getArbitraryProperty(),
				it.getArbitraryProperty().getObjectProperty(),
				it.getArbitraryProperty().getContainerProperty()
			))
			.collect(Collectors.toList());

		for (ArbitraryNode node : resolved) {
			node.setArbitraryProperty(node.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		}

		return resolved;
	}
}
