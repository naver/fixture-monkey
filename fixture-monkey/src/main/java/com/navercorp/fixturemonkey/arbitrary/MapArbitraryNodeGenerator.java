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

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

@SuppressWarnings("unchecked")
public class MapArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final MapArbitraryNodeGenerator INSTANCE = new MapArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		LazyValue<T> lazyValue = nowNode.getValue();
		if (lazyValue != null) {
			nowNode.setArbitrary(Arbitraries.just(lazyValue.get()));
			return generatedNodeList;
		}

		ArbitraryType<T> clazz = nowNode.getType();
		String fieldName = nowNode.getFieldName();

		ArbitraryType<?> keyType = clazz.getGenericArbitraryType(0);
		ArbitraryType<?> valueType = clazz.getGenericArbitraryType(1);

		nowNode.initializeElementSize();

		int elementSize = nowNode.getContainerSizeConstraint().getArbitraryElementSize();

		if (clazz.isMapEntry()) {
			elementSize = 1;
		}

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<?> keyNode = ArbitraryNode.builder()
				.type(keyType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(keyNode);

			ArbitraryNode<?> valueNode = ArbitraryNode.builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullInject(0.f)
				.build();

			generatedNodeList.add(valueNode);
		}
		return generatedNodeList;
	}
}
