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

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeDefinition;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.IdentityNodeResolver;
import com.navercorp.fixturemonkey.tree.ObjectNode;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final int sequence;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	@Nullable
	private final T value;

	public NodeSetDecomposedValueManipulator(
		int sequence,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		@Nullable T value
	) {
		this.sequence = sequence;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.value = value;
	}

	@Override
	public void manipulate(ObjectNode objectNode) {
		Class<?> actualType = Types.getActualType(objectNode.getOriginalProperty().getType());
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
					objectNode.getOriginalProperty().getType().getTypeName(),
					value.getClass().getTypeName()
				)
			);
		}
		setValue(objectNode, value);
	}

	private void setValue(ObjectNode objectNode, @Nullable Object value) {
		objectNode.setNullInject(NOT_NULL_INJECT);
		if (value == null) {
			objectNode.addManipulator(node -> node.setArbitrary(CombinableArbitrary.from((Object)null)));
			objectNode.setNullInject(ALWAYS_NULL_INJECT);
			return;
		}

		boolean container = objectNode.getArbitraryProperty().isContainer();
		if (container) {
			if (Types.getActualType(objectNode.getResolvedProperty().getType()) == Function.class) {
				objectNode.setArbitrary(CombinableArbitrary.from(value));
				return;
			}

			DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(value);
			Object containerValue = decomposableJavaContainer.getJavaContainer();
			int decomposedContainerSize = decomposableJavaContainer.getSize();

			ContainerInfoManipulator appliedContainerInfoManipulator = objectNode.getAppliedContainerInfoManipulator();
			boolean forced = !(objectNode.getOriginalProperty() instanceof MapEntryElementProperty)
				&& (appliedContainerInfoManipulator == null
				|| sequence > appliedContainerInfoManipulator.getManipulatingSequence());
			if (forced) {
				ArbitraryContainerInfo containerInfo =
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize);
				objectNode.addContainerManipulator(
					new ContainerInfoManipulator(
						IdentityNodeResolver.INSTANCE.toNextNodePredicate(),
						containerInfo,
						sequence
					)
				);
				objectNode.forceExpand();
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
				Property childProperty = child.getOriginalProperty();
				setValue(child, childProperty.getValue(containerValue));
			}
			return;
		}

		List<ObjectNode> children = objectNode.getChildren();
		if (children.isEmpty() || Types.getActualType(objectNode.getResolvedProperty().getType()).isInterface()) {
			CombinableArbitrary<?> combinableArbitrary = CombinableArbitrary.from(value);
			objectNode.addManipulator(node -> node.setArbitrary(combinableArbitrary));
			objectNode.setArbitrary(combinableArbitrary);
			return;
		}

		List<ConcreteTypeDefinition> concreteTypeDefinitions = objectNode.getArbitraryProperty()
			.getConcreteTypeDefinitions();

		for (ConcreteTypeDefinition concreteTypeDefinition : concreteTypeDefinitions) {
			Class<?> actualConcreteType = Types.getActualType(concreteTypeDefinition.getConcreteProperty().getType());
			if (isAssignable(
				value.getClass(),
				actualConcreteType
			)) {
				Property resolvedParentProperty = concreteTypeDefinition.getConcreteProperty();

				if (isAssignable(
					actualConcreteType,
					value.getClass()
				)) {
					objectNode.setResolvedProperty(resolvedParentProperty);
				}

				List<Property> childProperties = concreteTypeDefinition.getChildPropertyLists();
				for (ObjectNode child : children) {
					if (childProperties.contains(child.getOriginalProperty())
						&& resolvedParentProperty.equals(child.getResolvedParentProperty())) {
						Property childProperty = child.getOriginalProperty();
						setValue(child, childProperty.getValue(value));
					}
				}
			}
		}
	}
}
