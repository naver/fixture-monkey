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

import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.MapIntrospector;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;

@API(since = "0.5.5", status = Status.MAINTAINED)
public final class JacksonMapArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	public static final JacksonMapArbitraryIntrospector INSTANCE = new JacksonMapArbitraryIntrospector(
		FixtureMonkeyJackson.defaultObjectMapper()
	);
	private static final ArbitraryIntrospector DELEGATOR = new MapIntrospector();

	private final ObjectMapper objectMapper;

	public JacksonMapArbitraryIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean match(Property property) {
		return Map.class.isAssignableFrom(Types.getActualType(property.getType()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<? extends Map<?, ?>> containerType = (Class<? extends Map<?, ?>>)Types.getActualType(property.getType());
		Class<?> keyType = Types.getActualType(Types.getGenericsTypes(property.getAnnotatedType()).get(0));
		Class<?> valueType = Types.getActualType(Types.getGenericsTypes(property.getAnnotatedType()).get(1));

		MapLikeType mapType = TypeFactory.defaultInstance()
			.constructMapType(containerType, keyType, valueType);

		return new ArbitraryIntrospectorResult(
			new JacksonCombinableArbitrary(
				DELEGATOR.introspect(context).getValue(),
				list -> objectMapper.convertValue(list, mapType)
			)
		);
	}
}
