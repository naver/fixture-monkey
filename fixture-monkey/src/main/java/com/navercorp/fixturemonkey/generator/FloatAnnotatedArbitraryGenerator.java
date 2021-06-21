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
import net.jqwik.api.arbitraries.FloatArbitrary;

public class FloatAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<Float> {
	public static final FloatAnnotatedArbitraryGenerator INSTANCE = new FloatAnnotatedArbitraryGenerator();

	@Override
	public Arbitrary<Float> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(Float.class, annotationSource);
		return generate(constraint);
	}

	private FloatArbitrary generate(AnnotatedGeneratorConstraint constraint) {
		FloatArbitrary arbitrary = Arbitraries.floats();
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();

		if (min != null && max != null) {
			return arbitrary.between(min.floatValue(), max.floatValue());
		} else if (max != null) {
			return arbitrary.lessOrEqual(max.floatValue());
		} else if (min != null) {
			return arbitrary.greaterOrEqual(min.floatValue());
		} else {
			return arbitrary;
		}
	}
}
