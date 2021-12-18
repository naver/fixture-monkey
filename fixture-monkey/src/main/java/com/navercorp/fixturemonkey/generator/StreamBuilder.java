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

import java.util.List;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

@SuppressWarnings({"rawtypes", "unchecked"})
final class StreamBuilder {
	public static final StreamBuilder INSTANCE = new StreamBuilder();

	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		Builders.BuilderCombinator<Stream.Builder> streamBuilderCombinator = Builders.withBuilder(Stream::builder);
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)streamBuilderCombinator.build(Stream.Builder::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			streamBuilderCombinator = streamBuilderCombinator.use(node.getArbitrary()).in(Stream.Builder::add);
		}

		return (Arbitrary<T>)streamBuilderCombinator.build(Stream.Builder::build);
	}
}
