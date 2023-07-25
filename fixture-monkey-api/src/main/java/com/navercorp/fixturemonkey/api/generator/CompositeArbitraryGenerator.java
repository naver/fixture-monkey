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

package com.navercorp.fixturemonkey.api.generator;

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;

/**
 * Generates a {@link CombinableArbitrary} by one or more {@link ArbitraryGenerator}.
 * If a {@link ArbitraryGenerator} returns {@code NOT_GENERATED}, the next {@link ArbitraryGenerator} would be used.
 */
@API(since = "0.6.2", status = Status.EXPERIMENTAL)
public final class CompositeArbitraryGenerator implements ArbitraryGenerator {
	private final List<ArbitraryGenerator> arbitraryGenerators;

	public CompositeArbitraryGenerator(List<ArbitraryGenerator> arbitraryGenerators) {
		this.arbitraryGenerators = arbitraryGenerators;
	}

	@Override
	public CombinableArbitrary<?> generate(ArbitraryGeneratorContext context) {
		for (ArbitraryGenerator arbitraryGenerator : arbitraryGenerators) {
			CombinableArbitrary<?> generated = arbitraryGenerator.generate(context);
			if (generated != NOT_GENERATED) {
				return generated;
			}
		}
		return NOT_GENERATED;
	}
}
