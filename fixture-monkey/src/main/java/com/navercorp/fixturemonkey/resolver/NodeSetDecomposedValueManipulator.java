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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.type.Types.isAssignable;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final int sequence;
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;
	@Nullable
	private final T value;

	public NodeSetDecomposedValueManipulator(
		int sequence,
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions,
		@Nullable T value
	) {
		this.sequence = sequence;
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
		this.value = value;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (value != null && !isAssignable(value.getClass(), actualType)) {
			String parentNodeLogMessage = arbitraryNode.getResolvedParentProperty() != null
				? String.format(
				"parent node type : %s",
				arbitraryNode.getResolvedParentProperty().getType().getTypeName()
			)
				: "";
			throw new IllegalArgumentException(
				String.format(
					"The value is not of the same type as the property.\n"
						+ "%s node name: %s, node type: %s, value type: %s",
					parentNodeLogMessage,
					arbitraryNode.getArbitraryProperty()
						.getObjectProperty()
						.getResolvedPropertyName(),
					arbitraryNode.getProperty().getType().getTypeName(),
					value.getClass().getTypeName()
				)
			);
		}
		setValue(arbitraryNode, value);
	}

	private void setValue(ArbitraryNode arbitraryNode, @Nullable Object value) {
		arbitraryNode.setManipulated(true);
		arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		if (value == null) {
			arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(ALWAYS_NULL_INJECT));
			return;
		}

		ContainerProperty containerProperty = arbitraryNode.getArbitraryProperty().getContainerProperty();
		if (containerProperty != null) {
			DecomposableContainerValue decomposableContainerValue =
				manipulateOptions.getDecomposedContainerValueFactory().from(value);
			Object containerValue = decomposableContainerValue.getContainer();
			int decomposedContainerSize = decomposableContainerValue.getSize();

			boolean forced = containerProperty.getContainerInfo().getManipulatingSequence() == null
				|| sequence > containerProperty.getContainerInfo().getManipulatingSequence();
			if (forced) {
				ContainerInfoManipulator containerInfoManipulator = new ContainerInfoManipulator(
					IdentityNodeResolver.INSTANCE.toNextNodePredicate(),
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize)
				);

				ArbitraryNode newNode = traverser.traverse(
					arbitraryNode.getProperty(),
					Collections.singletonList(containerInfoManipulator),
					Collections.emptyList()
				);
				arbitraryNode.setArbitraryProperty(
					arbitraryNode.getArbitraryProperty()
						.withContainerProperty(newNode.getArbitraryProperty().getContainerProperty())
				);
				arbitraryNode.setChildren(newNode.getChildren());
			}

			List<ArbitraryNode> children = arbitraryNode.getChildren();

			if (arbitraryNode.getArbitraryProperty()
				.getObjectProperty()
				.getProperty() instanceof MapEntryElementProperty) {
				decomposedContainerSize *= 2; // key, value
			}

			int decomposedNodeSize = Math.min(decomposedContainerSize, children.size());

			for (int i = 0; i < decomposedNodeSize; i++) {
				ArbitraryNode child = children.get(i);
				Property childProperty = child.getProperty();
				setValue(child, childProperty.getValue(containerValue));
			}
			return;
		}

		List<ArbitraryNode> children = arbitraryNode.getChildren();
		if (children.isEmpty() || Types.getActualType(arbitraryNode.getProperty().getType()).isInterface()) {
			arbitraryNode.setArbitrary(Arbitraries.just(value));
			return;
		}

		Entry<Property, List<Property>> childPropertiesByResolvedProperty = arbitraryNode.getArbitraryProperty()
			.getObjectProperty()
			.getChildPropertiesByResolvedProperty(
				property -> isAssignable(Types.getActualType(property.getType()), value.getClass())
			);

		arbitraryNode.setArbitraryProperty(
			arbitraryNode.getArbitraryProperty()
				.withChildPropertyListsByCandidateProperty(
					Collections.singletonMap(
						childPropertiesByResolvedProperty.getKey(),
						childPropertiesByResolvedProperty.getValue()
					)
				)
		);

		Property resolvedParentProperty = childPropertiesByResolvedProperty.getKey();
		List<Property> childProperties = childPropertiesByResolvedProperty.getValue();
		for (ArbitraryNode child : children) {
			if (childProperties.contains(child.getProperty())
				&& resolvedParentProperty.equals(child.getResolvedParentProperty())) {
				Property childProperty = child.getProperty();
				setValue(child, childProperty.getValue(value));
			}
		}
	}
}
