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

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
class ArbitraryIntrospectorTest {
	private final ArbitraryIntrospector sut = new CompositeArbitraryIntrospector(
		Arrays.asList(
			new EnumIntrospector(),
			new BooleanIntrospector(),
			new UuidIntrospector(),
			new JavaArbitraryIntrospector(),
			new JavaTimeArbitraryIntrospector()
		)
	);

	@Test
	void introspectEnumType() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "season").get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isExactlyInstanceOf(Season.class);
	}

	@Test
	void introspectBooleanType() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "bool").get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isIn(true, false);
	}

	@Test
	void introspectBooleanWrapperType() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "bool").get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		then(actual.getValue().sample()).isIn(true, false);
	}

	@Test
	void introspectUuidType() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "uuid").get();
		ArbitraryGeneratorContext context = getArbitraryGeneratorContext(property);

		// when
		ArbitraryIntrospectorResult actual = this.sut.introspect(context);

		String uuid = actual.getValue().sample().toString();
		then(uuid).hasSize(36);
		then(uuid.replaceAll("-", "")).hasSize(32);
	}

	private ArbitraryGeneratorContext getArbitraryGeneratorContext(Property property) {
		return new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				new ObjectProperty(
					property,
					PropertyNameResolver.IDENTITY,
					0.0D,
					null,
					Collections.emptyList()
				),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);
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
