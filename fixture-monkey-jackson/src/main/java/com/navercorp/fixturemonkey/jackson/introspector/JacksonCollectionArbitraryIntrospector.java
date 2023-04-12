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

package com.navercorp.fixturemonkey.jackson.introspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;

@API(since = "0.5.5", status = Status.EXPERIMENTAL)
public final class JacksonCollectionArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	public static final JacksonCollectionArbitraryIntrospector INSTANCE = new JacksonCollectionArbitraryIntrospector(
		FixtureMonkeyJackson.defaultObjectMapper()
	);

	private final ObjectMapper objectMapper;

	public JacksonCollectionArbitraryIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean match(Property property) {
		return Collection.class.isAssignableFrom(Types.getActualType(property.getType()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> containerType = Types.getActualType(property.getType());
		Class<?> elementType = Types.getActualType(Types.getGenericsTypes(property.getAnnotatedType()).get(0));

		CollectionType collectionType = TypeFactory.defaultInstance()
			.constructCollectionType((Class<? extends Collection<?>>)containerType, elementType);

		return new ArbitraryIntrospectorResult(
			new JacksonCombinableArbitrary<>(
				LazyArbitrary.lazy(() -> {
					List<Arbitrary<Object>> arbitraries = context.getElementArbitraries().stream()
						.map(CombinableArbitrary::rawValue)
						.collect(Collectors.toList());

					return Combinators.combine(arbitraries).as(ArrayList::new);
				}),
				list -> objectMapper.convertValue(list, collectionType)
			)
		);
	}
}
