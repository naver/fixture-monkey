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

import static com.navercorp.fixturemonkey.api.matcher.SingleGenericTypeMatcher.SINGLE_GENERIC_TYPE_MATCHER;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * It is deprecated since 1.0.16. Use {@link SingleGenericCollectionIntrospector} instead.
 */
@API(since = "0.4.0", status = Status.MAINTAINED)
@Deprecated
public final class ListIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListIntrospector.class);
	private static final Matcher MATCHER = new AssignableTypeMatcher(List.class);

	@Override
	public boolean match(Property property) {
		return SINGLE_GENERIC_TYPE_MATCHER.match(property) && MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer() || !match(context.getResolvedProperty())) {
			LOGGER.info("Given type {} is not List type.", context.getResolvedType());
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

		Class<?> type = Types.getActualType(context.getResolvedType());
		Constructor<?> declaredConstructor = TypeCache.getDeclaredConstructor(type, Collection.class);
		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(elementCombinableArbitraryList)
				.build(list -> Reflections.newInstance(declaredConstructor, list))
		);
	}
}
