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

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.collection.IteratorCache;
import com.navercorp.fixturemonkey.api.collection.StreamCache;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	@Nullable
	private final T value;
	private final boolean forced;

	public NodeSetDecomposedValueManipulator(
		ArbitraryTraverser traverser,
		@Nullable T value,
		boolean forced
	) {
		this.traverser = traverser;
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
		arbitraryNode.setManipulated(true);
		arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		if (value == null) {
			arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(ALWAYS_NULL_INJECT));
			return;
		}

		ContainerProperty containerProperty = arbitraryNode.getArbitraryProperty().getContainerProperty();
		if (containerProperty != null) {
			DecomposableContainerValue decomposableContainerValue = decomposeContainerValue(
				arbitraryNode.getArbitraryProperty(),
				value
			);
			Object containerValue = decomposableContainerValue.getContainer();
			int decomposedContainerSize = decomposableContainerValue.getSize();

			if (forced || !containerProperty.getContainerInfo().isManipulated()) {
				ContainerInfoManipulator containerInfoManipulator = new ContainerInfoManipulator(
					IdentityNodeResolver.INSTANCE.toNextNodePredicate(),
					new ArbitraryContainerInfo(decomposedContainerSize, decomposedContainerSize, false)
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

	private DecomposableContainerValue decomposeContainerValue(
		ArbitraryProperty arbitraryProperty,
		Object value
	) {
		Class<?> actualType = value.getClass();

		if (Iterable.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterable<?>)value).iterator();
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableContainerValue(list, list.size());
		} else if (Iterator.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterator<?>)value);
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableContainerValue(list, list.size());
		} else if (Stream.class.isAssignableFrom(actualType)) {
			List<?> container = StreamCache.getList((Stream<?>)value);
			return new DecomposableContainerValue(container, container.size());
		} else if (actualType.isArray()) {
			return new DecomposableContainerValue(value, Array.getLength(value));
		} else if (Map.class.isAssignableFrom(actualType)) {
			Map<?, ?> map = (Map<?, ?>)value;
			return new DecomposableContainerValue(value, map.size());
		} else if (Map.Entry.class.isAssignableFrom(actualType)) {
			return new DecomposableContainerValue(value, 1);
		} else if (isOptional(actualType)) {
			return new DecomposableContainerValue(value, 1);
		}

		return new DecomposableContainerValue(
			value,
			arbitraryProperty.getContainerProperty().getElementProperties().size()
		);
	}

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}
}
