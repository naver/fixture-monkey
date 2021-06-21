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

import java.math.BigDecimal;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.LongArbitrary;

public class LongAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Long> {
	public static final LongAnnotatedArbitraryGenerator INSTANCE = new LongAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Long> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Long.class, annotationSource);
		return generate(constraint);
	}

	private LongArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		LongArbitrary arbitrary = Arbitraries.longs();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.longValue(), max.longValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.longValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.longValue());
		} else {
			return arbitrary;
		}
	}
}
