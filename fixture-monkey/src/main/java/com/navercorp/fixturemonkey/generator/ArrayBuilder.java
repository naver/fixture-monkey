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

package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class ArrayBuilder {
	public static ArrayBuilder INSTANCE = new ArrayBuilder();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> Arbitrary<T> build(Class<T> clazz, List<ArbitraryNode> nodes) {
		Builders.BuilderCombinator<ArrayBuilderFrame> builder =
			Builders.withBuilder(() -> new ArrayBuilderFrame(clazz, nodes.size()));

		if (nodes.isEmpty()) {
			return (Arbitrary<T>)builder.build(ArrayBuilderFrame::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			builder = builder.use(node.getArbitrary()).in(ArrayBuilderFrame::add);
		}

		return (Arbitrary<T>)builder.build(ArrayBuilderFrame::build);
	}

	private static final class ArrayBuilderFrame {
		private final List<Object> array;
		private final Class<?> componentType;
		private final int size;

		public ArrayBuilderFrame(Class<?> componentType, int size) {
			this.array = new ArrayList<>();
			this.componentType = componentType;
			this.size = size;
		}

		ArrayBuilderFrame add(Object value) {
			if (array.size() >= size) {
				return this;
			}

			array.add(value);
			return this;
		}

		// primitive 타입일 때, ClassCastException 이 발생하기 때문에 Object 로 반환한다.
		Object build() {
			Object array = Array.newInstance(componentType, size);
			for (int i = 0; i < this.array.size(); i++) {
				Array.set(array, i, this.array.get(i));
			}

			return array;
		}
	}
}
