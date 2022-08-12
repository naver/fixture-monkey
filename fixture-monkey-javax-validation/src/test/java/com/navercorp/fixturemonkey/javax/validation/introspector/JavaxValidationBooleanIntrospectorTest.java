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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class JavaxValidationBooleanIntrospectorTest {
	private final JavaxValidationBooleanIntrospector sut = new JavaxValidationBooleanIntrospector();

	@Property
	void booleanValue() {
		// given
		TypeReference<BooleanIntrospectorSpec> typeReference = new TypeReference<BooleanIntrospectorSpec>() {
		};
		String propertyName = "boolValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		// then
		then(actual).isNotEqualTo(ArbitraryIntrospectorResult.EMPTY);
		then(actual.getValue().sample()).isNotNull();
	}

	@Property
	void assertTrue() {
		// given
		TypeReference<BooleanIntrospectorSpec> typeReference = new TypeReference<BooleanIntrospectorSpec>() {
		};
		String propertyName = "assertTrue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		// then
		then(actual.getValue().sample()).isEqualTo(true);
	}

	@Property
	void assertFalse() {
		// given
		TypeReference<BooleanIntrospectorSpec> typeReference = new TypeReference<BooleanIntrospectorSpec>() {
		};
		String propertyName = "assertFalse";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		// then
		then(actual.getValue().sample()).isEqualTo(false);
	}
}
