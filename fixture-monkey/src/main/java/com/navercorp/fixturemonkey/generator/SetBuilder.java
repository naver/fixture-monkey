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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class SetBuilder {
	public static SetBuilder INSTANCE = new SetBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		BuilderCombinator<CollectionBuilderFrame> setBuilderCombinator = Builders.withBuilder(SetBuilderFrame::new);
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)setBuilderCombinator.build(CollectionBuilderFrame::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			setBuilderCombinator = setBuilderCombinator.use(node.getArbitrary())
				.in(CollectionBuilderFrame::add);
		}

		return (Arbitrary<T>)setBuilderCombinator.build(CollectionBuilderFrame::build)
			.filter(it -> it.size() == nodes.size());
	}

	private static class SetBuilderFrame extends CollectionBuilderFrame {
		public SetBuilderFrame() {
			super(new HashSet<>());
		}

		@Override
		Collection<Object> build() {
			return new HashSet<>(collection);
		}
	}
}
