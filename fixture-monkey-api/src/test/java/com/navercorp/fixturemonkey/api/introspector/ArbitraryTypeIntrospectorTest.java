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

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class ArbitraryTypeIntrospectorTest {
	@Test
	void introspectEnumType() {
		// given
		Property property = PropertyCache.getReadProperty(SampleEnum.class, "season").get();
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			property,
			ArbitraryTypeIntrospector.INTROSPECTORS
		);

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Season.class);
	}

	static class SampleEnum {
		private Season season;
	}

	enum Season {
		SPRING, SUMMER, FALL, WINTER
	}
}
