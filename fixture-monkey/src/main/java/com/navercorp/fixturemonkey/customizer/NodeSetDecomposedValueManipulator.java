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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.type.Types.isAssignable;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;
import com.navercorp.fixturemonkey.tree.IdentityNodeResolver;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final int sequence;
	private final ArbitraryTraverser traverser;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	@Nullable
	private final T value;

	public NodeSetDecomposedValueManipulator(
		int sequence,
		ArbitraryTraverser traverser,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		@Nullable T value
	) {
		this.sequence = sequence;
		this.traverser = traverser;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.value = value;
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		Class<?> actualType = Types.getActualType(objectNode.getProperty().getType());
		if (value != null && !isAssignable(value.getClass(), actualType)) {
			String parentNodeLogMessage = objectNode.getResolvedParentProperty() != null
				? String.format(
				"parent node type : %s",
				objectNode.getResolvedParentProperty().getType().getTypeName()
			)
				: "";
			throw new IllegalArgumentException(
				String.format(
					"The value is not of the same type as the property.\n"
						+ "%s node name: %s, node type: %s, value type: %s",
					parentNodeLogMessage,
					objectNode.getArbitraryProperty()
						.getObjectProperty()
						.getResolvedPropertyName(),
					objectNode.getProperty().getType().getTypeName(),
					value.getClass().getTypeName()
				)
			);
		}
		setValue(objectNode, value);
	}

	private void setValue(ObjectNode objectNode, @Nullable Object value) {
		objectNode.setManipulated(true);
		objectNode.setArbitraryProperty(objectNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		if (value == null) {
			objectNode.setArbitraryProperty(objectNode.getArbitraryProperty().withNullInject(ALWAYS_NULL_INJECT));
			return;
		}

		boolean container = objectNode.getArbitraryProperty().isContainer();
		if (container) {
			DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(value);
			Object containerValue = decomposableJavaContainer.getJavaContainer();
			int decomposedContainerSize = decomposableJavaContainer.getSize();

			ContainerInfoManipulator appliedContainerInfoManipulator = objectNode.getAppliedContainerInfoManipulator();
			boolean forced = appliedContainerInfoManipulator == null
				|| sequence > appliedContainerInfoManipulator.getManipulatingSequence();
			if (forced) {
				ContainerInfoManipulator containerInfoManipulator = new ContainerInfoManipulator(
					IdentityNodeResolver.INSTANCE.toNextNodePredicate(),
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize),
					sequence
				);

				ObjectNode newNode = traverser.traverse(
					objectNode.getProperty(),
					Collections.singletonList(containerInfoManipulator),
					Collections.emptyList()
				);
				objectNode.setChildren(newNode.getChildren());
			}

			List<ObjectNode> children = objectNode.getChildren();

			if (objectNode.getArbitraryProperty()
				.getObjectProperty()
				.getProperty() instanceof MapEntryElementProperty) {
				decomposedContainerSize *= 2; // key, value
			}

			int decomposedNodeSize = Math.min(decomposedContainerSize, children.size());

			for (int i = 0; i < decomposedNodeSize; i++) {
				ObjectNode child = children.get(i);
				Property childProperty = child.getProperty();
				setValue(child, childProperty.getValue(containerValue));
			}
			return;
		}

		List<ObjectNode> children = objectNode.getChildren();
		if (children.isEmpty() || Types.getActualType(objectNode.getProperty().getType()).isInterface()) {
			objectNode.setArbitrary(CombinableArbitrary.from(value));
			return;
		}

		Entry<Property, List<Property>> childPropertiesByResolvedProperty = objectNode.getArbitraryProperty()
			.getObjectProperty()
			.getChildPropertiesByResolvedProperty(
				property -> isAssignable(Types.getActualType(property.getType()), value.getClass())
			);

		Property resolvedParentProperty = childPropertiesByResolvedProperty.getKey();
		objectNode.setResolvedProperty(resolvedParentProperty);
		List<Property> childProperties = childPropertiesByResolvedProperty.getValue();
		for (ObjectNode child : children) {
			if (childProperties.contains(child.getProperty())
				&& resolvedParentProperty.equals(child.getResolvedParentProperty())) {
				Property childProperty = child.getProperty();
				setValue(child, childProperty.getValue(value));
			}
		}
	}
}
