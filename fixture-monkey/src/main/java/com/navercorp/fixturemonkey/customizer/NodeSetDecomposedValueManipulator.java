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
import static com.navercorp.fixturemonkey.api.type.Types.nullSafe;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeDefinition;
import com.navercorp.fixturemonkey.api.tree.TreeNodeManipulator;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.tree.GenerateFixtureContext;
import com.navercorp.fixturemonkey.tree.ObjectNode;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;

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
		GenerateFixtureContext generateFixtureContext = objectNode.getObjectNodeContext();
		if (value == null) {
			generateFixtureContext.addManipulator(
				node -> node.getObjectNodeContext()
					.setArbitrary(CombinableArbitrary.from((Object)null))
			);
			objectNode.setNullInject(ALWAYS_NULL_INJECT);
			return;
		}

		boolean container = objectNode.getArbitraryProperty().isContainer();
		if (container) {
			if (Types.getActualType(objectNode.getResolvedProperty().getType()) == Function.class) {
				generateFixtureContext.setArbitrary(CombinableArbitrary.from(value));
				return;
			}

			DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(value);
			Object containerValue = decomposableJavaContainer.getJavaContainer();
			int decomposedContainerSize = decomposableJavaContainer.getSize();

			TreeNodeManipulator appliedContainerInfoManipulator = objectNode.getAppliedTreeNodeManipulator();
			boolean forced = !(objectNode.getOriginalProperty() instanceof MapEntryElementProperty)
				&& (appliedContainerInfoManipulator == null
				|| sequence > appliedContainerInfoManipulator.getManipulatingSequence());
			if (forced) {
				ArbitraryContainerInfo containerInfo =
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize);
				objectNode.addTreeNodeManipulator(
					new ContainerInfoManipulator(
						Collections.singletonList(StartNodePredicate.INSTANCE),
						containerInfo,
						sequence
					)
				);
				objectNode.forceExpand();
			}

			objectNode.expand();
			List<ObjectNode> children = nullSafe(objectNode.getChildren()).asList();

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

		objectNode.expand();
		List<ObjectNode> children = nullSafe(objectNode.getChildren()).asList();
		if (children.isEmpty() || Types.getActualType(objectNode.getResolvedProperty().getType()).isInterface()) {
			CombinableArbitrary<?> combinableArbitrary = CombinableArbitrary.from(value);
			generateFixtureContext.addManipulator(
				node -> node.getObjectNodeContext().setArbitrary(combinableArbitrary));
			generateFixtureContext.setArbitrary(combinableArbitrary);
			return;
		}

		List<? extends TypeDefinition> typeDefinitions = objectNode.getTreeProperty().getTypeDefinitions();
		for (TypeDefinition typeDefinition : typeDefinitions) {
			Class<?> actualConcreteType = Types.getActualType(typeDefinition.getResolvedProperty().getType());
			if (isAssignable(
				value.getClass(),
				actualConcreteType
			)) {
				if (isAssignable(
					actualConcreteType,
					value.getClass()
				)) {
					objectNode.setResolvedTypeDefinition(typeDefinition);
				}

				objectNode.forceExpand();
				for (ObjectNode child : nullSafe(objectNode.getChildren()).asList()) {
					if (!typeDefinition.getResolvedProperty().equals(child.getResolvedParentProperty())) {
						continue;
					}
					Property childProperty = child.getMetadata().getOriginalProperty();
					setValue(child, childProperty.getValue(value));
				}
			}
		}
	}
}
