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

package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryTypeIntrospectorTest.Sample;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class ArbitraryTypeIntrospectDelegatorTest {
	@Test
	void matchAndIntrospectEmpty() {
		ArbitraryTypeIntrospectDelegator sut = new ArbitraryTypeIntrospectDelegator(
			ctx -> true,
			ctx -> ArbitraryIntrospectorResult.EMPTY
		);

		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "season").get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				null,
				0.0D,
				null,
				null
			),
			Collections.emptyList(),
			null,
			ctx -> null
		);

		then(sut.match(context.getAnnotatedType())).isTrue();
		then(sut.introspect(context)).isEqualTo(ArbitraryIntrospectorResult.EMPTY);
	}
}
