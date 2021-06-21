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
import net.jqwik.api.arbitraries.IntegerArbitrary;

public class IntegerAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Integer> {
	public static final IntegerAnnotatedArbitraryGenerator INSTANCE = new IntegerAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Integer> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Integer.class, annotationSource);
		return generate(constraint);
	}

	private IntegerArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		IntegerArbitrary arbitrary = Arbitraries.integers();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.intValue(), max.intValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.intValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.intValue());
		} else {
			return arbitrary;
		}
	}
}
