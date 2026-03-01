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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.TypeFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson3.FixtureMonkeyJackson3;
import com.navercorp.fixturemonkey.jackson3.type.Jackson3TypeReference;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class Jackson3CollectionArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	public static final Jackson3CollectionArbitraryIntrospector INSTANCE = new Jackson3CollectionArbitraryIntrospector(
		FixtureMonkeyJackson3.defaultJsonMapper()
	);

	private final ObjectMapper objectMapper;

	public Jackson3CollectionArbitraryIntrospector(ObjectMapper objectMapper) {
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
		TypeFactory typeFactory = TypeFactory.createDefaultInstance();
		AnnotatedType elementAnnotatedType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
		JavaType elementType = typeFactory.constructType(new Jackson3TypeReference<>() {
			@Override
			public Type getType() {
				return elementAnnotatedType.getType();
			}
		});

		CollectionType collectionType = typeFactory
			.constructCollectionType((Class<? extends Collection<?>>)containerType, elementType);

		List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

		CombinableArbitrary<?> listCombinableArbitrary = CombinableArbitrary.containerBuilder()
			.elements(elementCombinableArbitraryList)
			.build(ArrayList::new);

		return new ArbitraryIntrospectorResult(
			new Jackson3CombinableArbitrary<>(
				listCombinableArbitrary,
				list -> objectMapper.convertValue(list, collectionType)
			)
		);
	}
}
