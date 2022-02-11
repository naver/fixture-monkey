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

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationBooleanIntrospectorTest {
	private final JavaxValidationBooleanIntrospector sut = new JavaxValidationBooleanIntrospector();

	@Property
	void booleanValue() {
		// given
		String propertyName = "boolValue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(BooleanIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
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
		String propertyName = "assertTrue";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(BooleanIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
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
		String propertyName = "assertFalse";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(BooleanIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		// then
		then(actual.getValue().sample()).isEqualTo(false);
	}
}
