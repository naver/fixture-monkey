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
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

class ArbitraryTypeIntrospectorTest {
	@Test
	void introspectEnumType() {
		// given
		Property property = PropertyCache.getProperty(Sample.class, "season").get();
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

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Season.class);
	}

	@Test
	void introspectBooleanType() {
		// given
		Property property = PropertyCache.getProperty(Sample.class, "bool").get();
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

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		then(actual.getValue().sample()).isIn(true, false);
	}

	@Test
	void introspectBooleanWrapperType() {
		// given
		Property property = PropertyCache.getProperty(Sample.class, "bool").get();
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

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		then(actual.getValue().sample()).isIn(true, false);
	}

	@Test
	void introspectUuidType() {
		// given
		Property property = PropertyCache.getProperty(Sample.class, "uuid").get();
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

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		String uuid = actual.getValue().sample().toString();
		then(uuid).hasSize(36);
		then(uuid.replaceAll("-", "")).hasSize(32);
	}

	static class Sample {
		private Season season;
		private boolean bool;
		private UUID uuid;
	}

	enum Season {
		SPRING, SUMMER, FALL, WINTER
	}
}
