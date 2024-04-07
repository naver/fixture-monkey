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

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.SingleElementProperty;

public final class WrappedNodeResolver implements NodeResolver {
	private final NextNodePredicate nextNodePredicate;

	public WrappedNodeResolver(NextNodePredicate nextNodePredicate) {
		this.nextNodePredicate = nextNodePredicate;
	}

	@Override
	public List<ObjectNode> resolve(ObjectNode objectNode) {
		List<ObjectNode> resolved = objectNode.getChildren().stream()
			.filter(it -> isSingleValueProperty(it.getArbitraryProperty().getObjectProperty()))
			.map(it -> {
				it.setArbitraryProperty(it.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
				return it;
			})
			.collect(Collectors.toList());

		if (resolved.isEmpty()) {
			objectNode.setArbitraryProperty(objectNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
			return Collections.singletonList(objectNode);
		}

		return resolved;
	}

	private boolean isSingleValueProperty(ObjectProperty currentObjectProperty) {
		Property property = currentObjectProperty.getProperty();

		return property instanceof SingleElementProperty;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		WrappedNodeResolver that = (WrappedNodeResolver)obj;
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
