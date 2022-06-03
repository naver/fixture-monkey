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

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class ArrayArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final ArrayArbitraryNodeGenerator INSTANCE = new ArrayArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> containerNode) {
		int currentIndex = 0;

		ArbitraryType<T> clazz = containerNode.getType();
		ArbitraryType<?> childType = clazz.getArrayArbitraryType();
		String propertyName = containerNode.getPropertyName();
		LazyArbitrary<T> lazyValue = containerNode.getValue();

		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		int elementSize = containerNode.getElementSize();

		if (lazyValue != null) {
			T value = lazyValue.getValue();

			if (value == null) {
				containerNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}
			int length = Array.getLength(value);

			for (
				currentIndex = 0;
				currentIndex < length && (containerNode.isNotSetContainerSize() || currentIndex < elementSize);
				currentIndex++
			) {
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
		}

		if (lazyValue == null || !containerNode.isNotSetContainerSize()) {
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
		}

		containerNode.setContainerSizeConstraint(null); // clear
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
		return this.generate(nowNode);
	}
}
