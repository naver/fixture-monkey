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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	private final T value;

	public NodeSetDecomposedValueManipulator(ArbitraryTraverser traverser, T value) {
		this.traverser = traverser;
		this.value = value;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (!actualType.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException(
				"The value is not of the same type as the property."
					+ " node type: " + arbitraryNode.getProperty().getType().getTypeName()
					+ " value type: " + value.getClass().getTypeName()
			);
		}
		setValue(arbitraryNode, value);
	}

	private void setValue(ArbitraryNode arbitraryNode, Object value) {
		if (value == null) {
			arbitraryNode.setArbitrary(Arbitraries.just(null));
			return;
		}

		if (arbitraryNode.getArbitraryProperty().isContainer()) {
			int decomposedContainerSize = arbitraryNode.getChildren().size();

			Class<?> actualType = Types.getActualType(value.getClass());
			Class<?> nodeActualType = Types.getActualType(arbitraryNode.getProperty().getType());

			if (Collection.class.isAssignableFrom(actualType)) {
				Collection<?> container = (Collection<?>)value;
				decomposedContainerSize = container.size();
			} else if (Map.class.isAssignableFrom(actualType)) {
				Map<?, ?> map = (Map<?, ?>)value;
				decomposedContainerSize = map.size();
			} else if (isDecomposeMapEntry(actualType, nodeActualType)) {
				decomposedContainerSize = 1;
			}

			if (decomposedContainerSize != arbitraryNode.getChildren().size()) {
				NodeSizeManipulator nodeSizeManipulator =
					new NodeSizeManipulator(traverser, decomposedContainerSize, decomposedContainerSize);
				nodeSizeManipulator.manipulate(arbitraryNode);
			}
		}

		List<ArbitraryNode> children = arbitraryNode.getChildren();
		arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		if (children.isEmpty()) {
			arbitraryNode.setArbitrary(Arbitraries.just(value));
			return;
		}

		for (ArbitraryNode child : children) {
			Property childProperty = child.getProperty();
			setValue(child, childProperty.getValue(value));
		}
	}

	private boolean isDecomposeMapEntry(Class<?> actualType, Class<?> nodeActualType) {
		return Map.Entry.class.isAssignableFrom(actualType) && Map.Entry.class.isAssignableFrom(nodeActualType);
	}
}
