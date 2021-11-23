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

import net.jqwik.api.Arbitrary;

public abstract class AbstractAnnotatedArbitraryGenerator<T> implements AnnotatedArbitraryGenerator<T> {
	protected Arbitrary<T> generateDefaultArbitrary(AnnotationSource<T> annotationSource) {
		return annotationSource.getArbitrary();
	}

	@SuppressWarnings("unchecked")
	protected <U extends Arbitrary<T>> U map(Arbitrary<T> arbitrary) {
		return (U)arbitrary;
	}

	protected abstract Arbitrary<T> applyArbitrary(Arbitrary<T> arbitrary, AnnotationSource<T> annotationSource);

	@Override
	public Arbitrary<T> generate(AnnotationSource<T> annotationSource) {
		Arbitrary<T> arbitrary = generateDefaultArbitrary(annotationSource);
		return map(applyArbitrary(arbitrary, annotationSource));
	}
}
