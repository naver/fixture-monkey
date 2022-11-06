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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	private final ManipulateOptions manipulateOptions;
	@Nullable
	private final T value;
	private final boolean forced;

	public NodeSetDecomposedValueManipulator(
		ArbitraryTraverser traverser,
		ManipulateOptions manipulateOptions,
		@Nullable T value,
		boolean forced
	) {
		this.traverser = traverser;
		this.manipulateOptions = manipulateOptions;
		this.value = value;
		this.forced = forced;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (value != null && !isAssignable(value.getClass(), actualType)) {
			throw new IllegalArgumentException(
				"The value is not of the same type as the property."
					+ " node name: " + arbitraryNode.getArbitraryProperty()
					.getObjectProperty()
					.getResolvedPropertyName()
					+ " node type: " + arbitraryNode.getProperty().getType().getTypeName()
					+ " value type: " + value.getClass().getTypeName()
			);
		}
		setValue(arbitraryNode, value);
	}

	private void setValue(ArbitraryNode arbitraryNode, @Nullable Object value) {
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		arbitraryNode.setManipulated(true);
		arbitraryNode.setArbitraryProperty(arbitraryProperty.withNullInject(NOT_NULL_INJECT));
		if (value == null) {
			arbitraryNode.setArbitraryProperty(arbitraryProperty.withNullInject(ALWAYS_NULL_INJECT));
			return;
		}

		ContainerProperty containerProperty = arbitraryProperty.getContainerProperty();
		if (containerProperty != null) {
			DecomposableContainerValue decomposableContainerValue =
				manipulateOptions.getDecomposedContainerValueFactory().from(value);
			value = decomposableContainerValue.getContainer();
			int decomposedContainerSize = decomposableContainerValue.getSize();

			if (forced || !containerProperty.getContainerInfo().isManipulated()) {
				ContainerInfoManipulator containerInfoManipulator = new ContainerInfoManipulator(
					IdentityNodeResolver.INSTANCE,
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize, false)
				);

				ArbitraryNode newNode = traverser.traverse(
					arbitraryNode.getProperty(),
					Collections.singletonList(containerInfoManipulator)
				);
				arbitraryNode.setArbitraryProperty(
					arbitraryNode.getArbitraryProperty()
						.withContainerProperty(newNode.getArbitraryProperty().getContainerProperty())
				);
				arbitraryNode.setChildren(newNode.getChildren());
			}

			List<ArbitraryNode> children = arbitraryNode.getChildren();

			if (arbitraryProperty.getObjectProperty().getProperty() instanceof MapEntryElementProperty) {
				decomposedContainerSize *= 2; // key, value
			}

			int decomposedNodeSize = Math.min(decomposedContainerSize, children.size());

			for (int i = 0; i < decomposedNodeSize; i++) {
				ArbitraryNode child = children.get(i);
				Property childProperty = child.getProperty();
				setValue(child, childProperty.getValue(value));
			}
			return;
		}

		List<ArbitraryNode> children = arbitraryNode.getChildren();

		if (children.isEmpty()) {
			arbitraryNode.setArbitrary(Arbitraries.just(value));
			return;
		}

		for (ArbitraryNode child : children) {
			Property childProperty = child.getProperty();
			setValue(child, childProperty.getValue(value));
		}
	}
}
