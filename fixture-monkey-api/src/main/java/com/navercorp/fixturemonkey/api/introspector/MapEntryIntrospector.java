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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ContainerCombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.5.6", status = Status.MAINTAINED)
public final class MapEntryIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Entry.class);

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ContainerProperty containerProperty = property.getContainerProperty();
		if (containerProperty == null) {
			throw new IllegalArgumentException(
				"container property should not null. type : " + property.getObjectProperty().getProperty().getName()
			);
		}
		ArbitraryContainerInfo containerInfo = containerProperty.getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<CombinableArbitrary> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();
		if (elementCombinableArbitraryList.size() != 1) {
			throw new IllegalArgumentException(
				"Map entry node should have only one child, current : " + elementCombinableArbitraryList.size()
			);
		}

		return new ArbitraryIntrospectorResult(
			new ContainerCombinableArbitrary(
				elementCombinableArbitraryList,
				elements -> {
					MapEntryElementType mapEntryElementType = (MapEntryElementType)elements.get(0);
					context.evictUnique(context.getPathProperty());
					return new SimpleEntry<>(mapEntryElementType.getKey(), mapEntryElementType.getValue());
				}
			)
		);
	}
}
