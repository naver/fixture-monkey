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

import static com.navercorp.fixturemonkey.generator.AnnotatedGeneratorConstraints.generateDateMillisArbitrary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import net.jqwik.api.Arbitrary;

public class LocalDateAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<LocalDate> {
	public static final LocalDateAnnotatedArbitraryGenerator INSTANCE = new LocalDateAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<LocalDate> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(LocalDate.class, annotationSource);

		return generateDateMillisArbitrary(constraint)
			.map(it -> Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate());
	}
}
