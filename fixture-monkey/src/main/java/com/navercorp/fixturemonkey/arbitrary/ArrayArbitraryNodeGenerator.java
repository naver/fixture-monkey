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

package com.navercorp.fixturemonkey.arbitrary;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class ArrayArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final ArrayArbitraryNodeGenerator INSTANCE = new ArrayArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, PropertyNameResolver propertyNameResolver) {
		int elementSize = Integer.MAX_VALUE;
		int currentIndex = 0;

		ArbitraryType<T> clazz = nowNode.getType();
		ArbitraryType<?> childType = clazz.getArrayArbitraryType();
		String propertyName = nowNode.getPropertyName();
		LazyValue<T> lazyValue = nowNode.getValue();

		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		if (lazyValue != null) {
			T value = lazyValue.get();
			ContainerSizeConstraint containerSizeConstraint = nowNode.getContainerSizeConstraint();

			if (value == null) {
				nowNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}
			int length = Array.getLength(value);

			if (containerSizeConstraint != null) {
				// container size is set.
				elementSize = containerSizeConstraint.getArbitraryElementSize();
			}

			for (currentIndex = 0; currentIndex < length && currentIndex < elementSize; currentIndex++) {
				Object nextValue = Array.get(value, currentIndex);
				@SuppressWarnings("unchecked")
				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(childType)
					.propertyName(propertyName)
					.indexOfIterable(currentIndex)
					.value(nextValue)
					.build();
				generatedNodeList.add(nextNode);
			}

			if (containerSizeConstraint == null) {
				// value exists, container size size is same as value size.
				nowNode.setContainerSizeConstraint(new ContainerSizeConstraint(length, length));
				return generatedNodeList;
			}
		}

		nowNode.initializeElementSize();
		if (isNotInitialized(elementSize)) {
			// value does not exist.
			elementSize = nowNode.getContainerSizeConstraint().getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			@SuppressWarnings("unchecked")
			ArbitraryNode<?> genericFrame = ArbitraryNode.builder()
				.type(childType)
				.propertyName(propertyName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(genericFrame);
		}
		return generatedNodeList;
	}

	/**
	 * Deprecated Use generate instead.
	 */
	@Deprecated
	@Override
	public <T> List<ArbitraryNode<?>> generate(
		ArbitraryNode<T> nowNode,
		FieldNameResolver fieldNameResolver
	) {
		return this.generate(
			nowNode,
			(PropertyNameResolver)property -> fieldNameResolver.resolveFieldName(((FieldProperty)property).getField())
		);
	}

	private boolean isNotInitialized(int elementSize) {
		return elementSize == Integer.MAX_VALUE;
	}
}
