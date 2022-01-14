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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class DefaultContainerArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final DefaultContainerArbitraryNodeGenerator INSTANCE = new DefaultContainerArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> containerNode) {
		int currentIndex = 0;

		ArbitraryType<T> clazz = containerNode.getType();
		LazyValue<T> lazyValue = containerNode.getValue();
		String propertyName = containerNode.getPropertyName();
		ArbitraryType<?> elementType = clazz.getGenericArbitraryType(0);

		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		int elementSize = containerNode.getElementSize();

		if (lazyValue != null) {
			if (lazyValue.isEmpty()) {
				containerNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}
			T value = lazyValue.get();

			if (!(value instanceof Collection || value instanceof Iterator || value instanceof Stream)) {
				throw new IllegalArgumentException(
					"Unsupported container type is given. " + value.getClass().getName()
				);
			}

			Iterator<?> iterator = toIterator(value);

			while ((containerNode.isNotSetContainerSize() || (currentIndex < elementSize)) && iterator.hasNext()) {
				Object nextObject = iterator.next();
				@SuppressWarnings("unchecked")
				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(elementType)
					.value(nextObject)
					.propertyName(propertyName)
					.indexOfIterable(currentIndex)
					.build();
				generatedNodeList.add(nextNode);
				currentIndex++;
			}
		}

		if (lazyValue == null || !containerNode.isNotSetContainerSize()) {
			for (int i = currentIndex; i < elementSize; i++) {
				@SuppressWarnings("unchecked")
				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(elementType)
					.propertyName(propertyName)
					.indexOfIterable(i)
					.nullable(false)
					.nullInject(0.f)
					.build();
				generatedNodeList.add(nextNode);
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

	@SuppressWarnings("unchecked")
	private <T, U> Iterator<U> toIterator(T value) {
		if (value instanceof Collection) {
			return ((Collection<U>)value).iterator();
		} else if (value instanceof Stream) {
			return ((Stream<U>)value).iterator();
		} else if (value instanceof Iterable) {
			return ((Iterable<U>)value).iterator();
		} else if (value instanceof ListIterator) {
			ListIterator<U> listIter = (ListIterator<U>)value;
			int listIteratorCursor = 0;
			while (listIter.hasPrevious()) {
				listIter.previous();
				listIteratorCursor++;
			}

			List<U> copied = new ArrayList<>();
			while (listIter.hasNext()) {
				copied.add(listIter.next());
			}

			if (listIter.previousIndex() + 1 == listIteratorCursor) {
				return copied.iterator();
			}

			int prevSize = listIter.previousIndex() + 1 - listIteratorCursor;
			for (int i = 0; i < prevSize; i++) {
				listIter.previous();
			}

			return copied.iterator();
		} else {
			// ListIterator 가 아니면 element 를 복사할 수 없다.
			// 기존 Iterator 의 커서가 이동하는 문제가 있다.
			return (Iterator<U>)value;
		}
	}
}
