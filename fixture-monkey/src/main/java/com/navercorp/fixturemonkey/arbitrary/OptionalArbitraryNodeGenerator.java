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
import java.util.Optional;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class OptionalArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final OptionalArbitraryNodeGenerator INSTANCE = new OptionalArbitraryNodeGenerator();

	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> containerNode) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		ArbitraryType<T> arbitraryType = containerNode.getType();
		ArbitraryType<?> elementType = arbitraryType.getGenericArbitraryType(0);
		String propertyName = containerNode.getPropertyName();
		LazyArbitrary<T> lazyValue = containerNode.getValue();

		if (lazyValue != null) {
			T value = lazyValue.getValue();
			if (value == null) {
				containerNode.setArbitrary(Arbitraries.just(null));
				return generatedNodeList;
			}

			Optional<?> optional = ((Optional<?>)value);
			Object nextObject = optional.orElse(null);
			LazyArbitrary<?> nextLazyValue = LazyArbitrary.lazy(() -> nextObject, true);

			if (nextLazyValue.getValue() == null) {
				// can not generate Optional empty by ArbitraryGenerator
				return generatedNodeList;
			}

			@SuppressWarnings("unchecked")
			ArbitraryNode<?> nextNode = ArbitraryNode.builder()
				.type(elementType)
				.value(nextLazyValue)
				.propertyName(propertyName)
				.indexOfIterable(0)
				.build();
			generatedNodeList.add(nextNode);
		}

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
