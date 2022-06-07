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
import java.util.List;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

@SuppressWarnings("unchecked")
public class MapArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final MapArbitraryNodeGenerator INSTANCE = new MapArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> containerNode) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		LazyArbitrary<T> lazyValue = containerNode.getValue();
		if (lazyValue != null) {
			containerNode.setArbitrary(Arbitraries.just(lazyValue.getValue()));
			return generatedNodeList;
		}

		ArbitraryType<T> clazz = containerNode.getType();
		String propertyName = containerNode.getPropertyName();

		ArbitraryType<?> keyType = clazz.getGenericArbitraryType(0);
		ArbitraryType<?> valueType = clazz.getGenericArbitraryType(1);

		int elementSize = containerNode.getElementSize();

		if (clazz.isMapEntry()) {
			elementSize = 1;
		}

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<?> keyNode = ArbitraryNode.builder()
				.type(keyType)
				.propertyName(propertyName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(keyNode);

			ArbitraryNode<?> valueNode = ArbitraryNode.builder()
				.type(valueType)
				.propertyName(propertyName)
				.indexOfIterable(i)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(valueNode);
		}

		containerNode.setContainerSizeConstraint(null); // clear
		return generatedNodeList;
	}

	/**
	 * Deprecated Use generate instead.
	 */
	@Deprecated
	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		return this.generate(nowNode);
	}
}
