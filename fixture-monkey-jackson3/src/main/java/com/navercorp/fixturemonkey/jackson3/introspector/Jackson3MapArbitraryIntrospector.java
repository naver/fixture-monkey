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

package com.navercorp.fixturemonkey.jackson3.introspector;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.MapLikeType;
import tools.jackson.databind.type.TypeFactory;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.MapIntrospector;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson3.FixtureMonkeyJackson3;
import com.navercorp.fixturemonkey.jackson3.type.Jackson3TypeReference;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class Jackson3MapArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	public static final Jackson3MapArbitraryIntrospector INSTANCE = new Jackson3MapArbitraryIntrospector(
		FixtureMonkeyJackson3.defaultJsonMapper()
	);
	private static final ArbitraryIntrospector DELEGATOR = new MapIntrospector();

	private final ObjectMapper objectMapper;

	public Jackson3MapArbitraryIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean match(Property property) {
		return Map.class.isAssignableFrom(Types.getActualType(property.getType()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		TypeFactory typeFactory = TypeFactory.createDefaultInstance();

		Property property = context.getResolvedProperty();
		Class<? extends Map<?, ?>> containerType = (Class<? extends Map<?, ?>>)Types.getActualType(property.getType());

		AnnotatedType keyAnnotatedType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
		AnnotatedType valueAnnotatedType = Types.getGenericsTypes(property.getAnnotatedType()).get(1);
		JavaType keyType = typeFactory.constructType(new Jackson3TypeReference<>() {
			@Override
			public Type getType() {
				return keyAnnotatedType.getType();
			}
		});

		JavaType valueType = typeFactory.constructType(new Jackson3TypeReference<Object>() {
			@Override
			public Type getType() {
				return valueAnnotatedType.getType();
			}
		});

		MapLikeType mapType = typeFactory
			.constructMapType(containerType, keyType, valueType);

		return new ArbitraryIntrospectorResult(
			new Jackson3CombinableArbitrary<>(
				DELEGATOR.introspect(context).getValue(),
				list -> objectMapper.convertValue(list, mapType)
			)
		);
	}
}
