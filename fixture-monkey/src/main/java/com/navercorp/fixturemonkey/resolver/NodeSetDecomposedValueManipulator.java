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
import static com.navercorp.fixturemonkey.api.type.Types.isAssignable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSetDecomposedValueManipulator<T> implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	@Nullable
	private final T value;

	public NodeSetDecomposedValueManipulator(ArbitraryTraverser traverser, @Nullable T value) {
		this.traverser = traverser;
		this.value = value;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (value != null && !isAssignable(value.getClass(), actualType)) {
			throw new IllegalArgumentException(
				"The value is not of the same type as the property."
					+ " node type: " + arbitraryNode.getProperty().getType().getTypeName()
					+ " value type: " + value.getClass().getTypeName()
			);
		}
		setValue(arbitraryNode, value);
	}

	private void setValue(ArbitraryNode arbitraryNode, @Nullable Object value) {
		if (value == null) {
			arbitraryNode.setArbitrary(Arbitraries.just(null));
			return;
		}

		if (arbitraryNode.getArbitraryProperty().isContainer()) {
			DecomposedContainerValue<?> decomposedContainerValue = DecomposedContainerValue.from(value);
			value = decomposedContainerValue.container;
			int decomposedContainerSize = decomposedContainerValue.size;

			if (decomposedContainerSize != arbitraryNode.getChildren().size()) {
				NodeSizeManipulator nodeSizeManipulator =
					new NodeSizeManipulator(traverser, decomposedContainerSize, decomposedContainerSize);
				nodeSizeManipulator.manipulate(arbitraryNode);
			}
		}

		List<ArbitraryNode> children = arbitraryNode.getChildren();
		arbitraryNode.setArbitraryProperty(arbitraryNode.getArbitraryProperty().withNullInject(NOT_NULL_INJECT));
		if (children.isEmpty() && !arbitraryNode.getArbitraryProperty().isContainer()) {
			arbitraryNode.setArbitrary(Arbitraries.just(value));
			return;
		}

		for (ArbitraryNode child : children) {
			Property childProperty = child.getProperty();
			setValue(child, childProperty.getValue(value));
		}
	}

	private static final class DecomposedContainerValue<T> {
		private final T container;
		private final int size;

		DecomposedContainerValue(T container, int size) {
			this.container = container;
			this.size = size;
		}

		private static DecomposedContainerValue<?> from(Object value) {
			Class<?> actualType = value.getClass();

			if (Collection.class.isAssignableFrom(actualType)) {
				Collection<?> container = (Collection<?>)value;
				return new DecomposedContainerValue<>(container, container.size());
			} else if (Iterable.class.isAssignableFrom(actualType)) {
				Iterator<?> iterator = ((Iterable<?>)value).iterator();
				List<?> list = IteratorCache.getList(iterator);
				return new DecomposedContainerValue<>(list, list.size());
			} else if (Iterator.class.isAssignableFrom(actualType)) {
				Iterator<?> iterator = ((Iterator<?>)value);
				List<?> list = IteratorCache.getList(iterator);
				return new DecomposedContainerValue<>(list, list.size());
			} else if (Stream.class.isAssignableFrom(actualType)) {
				List<?> container = StreamCache.getList((Stream<?>)value);
				return new DecomposedContainerValue<>(container, container.size());
			} else if (actualType.isArray()) {
				return new DecomposedContainerValue<>(value, Array.getLength(value));
			} else if (Map.class.isAssignableFrom(actualType)) {
				Map<?, ?> map = (Map<?, ?>)value;
				return new DecomposedContainerValue<>(value, map.size());
			} else if (Map.Entry.class.isAssignableFrom(actualType)) {
				return new DecomposedContainerValue<>(value, 1);
			} else if (isOptional(actualType)) {
				return new DecomposedContainerValue<>(value, 1);
			}

			throw new IllegalArgumentException("given type is not supported container : " + actualType.getTypeName());
		}

		private static boolean isOptional(Class<?> type) {
			return Optional.class.isAssignableFrom(type)
				|| OptionalInt.class.isAssignableFrom(type)
				|| OptionalLong.class.isAssignableFrom(type)
				|| OptionalDouble.class.isAssignableFrom(type);
		}
	}
}
