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

import static com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult.NOT_INTROSPECTED;
import static com.navercorp.fixturemonkey.api.matcher.SingleGenericTypeMatcher.SINGLE_GENERIC_TYPE_MATCHER;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class SetIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetIntrospector.class);
	private static final Matcher MATCHER = new AssignableTypeMatcher(Set.class);

	@Override
	public boolean match(Property property) {
		return SINGLE_GENERIC_TYPE_MATCHER.match(property) && MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty arbitraryProperty = context.getArbitraryProperty();
		if (!arbitraryProperty.isContainer() || !match(context.getResolvedProperty())) {
			LOGGER.info("Given type {} is not Set type.", context.getResolvedType());
			return NOT_INTROSPECTED;
		}

		List<CombinableArbitrary<?>> elementArbitraryList = context.getElementCombinableArbitraryList().stream()
			.map(it -> it.filter(
					context.getGenerateUniqueMaxTries(),
					element -> context.isUniqueAndCheck(
						context.getPropertyPath(),
						element
					)
				)
			)
			.collect(Collectors.toList());

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(elementArbitraryList)
				.postBuild(() -> context.evictUnique(context.getPropertyPath()))
				.build(HashSet::new)
		);
	}
}
