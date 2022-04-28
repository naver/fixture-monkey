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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.validator.DefaultArbitraryValidator;

class FixtureMonkeyV04Test {
	@Property
	void sample() {
		// given
		GenerateOptions generateOptions = GenerateOptions.DEFAULT_GENERATE_OPTIONS;
		ArbitraryResolver resolver = new ArbitraryResolver(
			new ArbitraryTraverser(generateOptions),
			new ManipulatorOptimizer(),
			generateOptions
		);
		TypeReference<ComplexObject> typeReference = new TypeReference<ComplexObject>() {
		};

		ArbitraryBuilder<ComplexObject> arbitraryBuilder = new ArbitraryBuilder<>(
			PropertyCache.getRootProperty(typeReference.getAnnotatedType()),
			Collections.emptyList(),
			resolver,
			new DefaultArbitraryValidator()
		);

		Arbitrary<ComplexObject> sut = arbitraryBuilder.build();

		// when
		ComplexObject actual = sut.sample();

		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
	}
}
