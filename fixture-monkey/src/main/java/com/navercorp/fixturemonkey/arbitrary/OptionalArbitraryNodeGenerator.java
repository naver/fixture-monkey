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

import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public class OptionalArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
	public static final OptionalArbitraryNodeGenerator INSTANCE = new OptionalArbitraryNodeGenerator();

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
		List<ArbitraryNode<?>> generatedNodeList = new ArrayList<>();

		ArbitraryType<T> arbitraryType = nowNode.getType();
		ArbitraryType<?> elementType = arbitraryType.getGenericArbitraryType(0);
		String fieldName = nowNode.getFieldName();

		LazyValue<?> nextLazyValue = getNextLazyValue(nowNode.getValue());

		if (nextLazyValue != null && nextLazyValue.isEmpty()) {
			// can not generate Optional empty by ArbitraryGenerator
			return generatedNodeList;
		}

		ArbitraryNode<?> nextNode = ArbitraryNode.builder()
			.type(elementType)
			.value(nextLazyValue)
			.fieldName(fieldName)
			.indexOfIterable(0)
			.build();
		generatedNodeList.add(nextNode);
		return generatedNodeList;
	}

	@SuppressWarnings("unchecked")
	private <T, U> LazyValue<U> getNextLazyValue(LazyValue<T> lazyValue) {
		if (lazyValue == null) {
			return null;
		}

		T value = lazyValue.get();

		Optional<U> optional = ((Optional<U>)value);
		U nextObject = optional.orElse(null);
		return new LazyValue<>(() -> nextObject);
	}
}
