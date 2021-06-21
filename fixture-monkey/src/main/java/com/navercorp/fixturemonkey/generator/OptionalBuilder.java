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
import java.util.Optional;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

@SuppressWarnings({"rawtypes", "unchecked"})
final class OptionalBuilder {
	public static final OptionalBuilder INSTANCE = new OptionalBuilder();

	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)Arbitraries.just(Optional.empty());
		}

		if (nodes.size() > 1) {
			throw new IllegalArgumentException("Optional can not have more than one value.");
		}

		return (Arbitrary<T>)nodes.get(0).getArbitrary().optional();
	}
}
