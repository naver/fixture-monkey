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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Map.class);

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty arbitraryProperty = context.getArbitraryProperty();
		ContainerProperty containerProperty = arbitraryProperty.getContainerProperty();
		if (containerProperty == null || containerProperty.getContainerInfo() == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<CombinableArbitrary> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(elementCombinableArbitraryList)
				.build(combine(context))
		);
	}

	private static Function<List<Object>, Object> combine(ArbitraryGeneratorContext context) {
		return elements -> {
			Map<Object, Object> map = new HashMap<>();
			for (Object element : elements) {
				MapEntryElementType mapEntryElement = (MapEntryElementType)element;
				if (mapEntryElement.getKey() == null) {
					throw new IllegalArgumentException("Map key cannot be null.");
				}
				map.put(
					mapEntryElement.getKey(),
					mapEntryElement.getValue()
				);
			}
			context.evictUnique(context.getPathProperty());
			return map;
		};
	}
}
