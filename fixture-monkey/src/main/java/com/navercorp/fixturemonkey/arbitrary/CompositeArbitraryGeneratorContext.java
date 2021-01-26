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

import java.util.Arrays;
import java.util.List;

public class CompositeArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	private final List<ArbitraryGeneratorContext> contexts;

	public CompositeArbitraryGeneratorContext(ArbitraryGeneratorContext... contexts) {
		this(Arrays.asList(contexts));
	}

	public CompositeArbitraryGeneratorContext(List<ArbitraryGeneratorContext> contexts) {
		this.contexts = contexts;
	}

	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		return this.contexts.stream()
			.map(it -> it.get(clazz))
			.filter(it -> !it.equals(EmptyArbitraryGenerator.getInstance()))
			.findFirst()
			.orElse(EmptyArbitraryGenerator.getInstance());
	}
}
