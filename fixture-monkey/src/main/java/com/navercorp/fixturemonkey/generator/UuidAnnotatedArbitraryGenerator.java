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

import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class UuidAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<UUID> {
	public static final UuidAnnotatedArbitraryGenerator INSTANCE = new UuidAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<UUID> generate(AnnotationSource annotationSource) {
		return Arbitraries.create(UUID::randomUUID);
	}
}
