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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.unique.FilteredMonkeyArbitrary;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapEntryElementIntrospector implements ArbitraryIntrospector, Matcher {
	private static final int MAX_TRIES = 10000;

	@Override
	public boolean match(Property property) {
		return property.getClass() == MapEntryElementProperty.class;
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

		List<Arbitrary<?>> arbitraries = context.getElementArbitraries().stream()
			.map(CombinableArbitrary::combined)
			.collect(Collectors.toList());

		if (arbitraries.size() != 2) {
			throw new IllegalArgumentException("Key and Value should be exist for MapEntryElementType.");
		}

		Property mapEntryProperty =
			((MapEntryElementProperty)property.getObjectProperty().getProperty()).getMapEntryProperty();

		if (Types.getActualType(mapEntryProperty.getType()) != Map.Entry.class) {
			arbitraries.set(
				0,
				new FilteredMonkeyArbitrary<>(
					arbitraries.get(0),
					it -> context.isUniqueAndCheck(
						context.getOwnerContext().getPathProperty(),
						it
					),
					MAX_TRIES
				)
			);
		}

		Arbitrary<MapEntryElementType> arbitrary = Builders.withBuilder(MapEntryElementType::new)
			.use(arbitraries.get(0)).in((element, key) -> {
				element.setKey(key);
				return element;
			})
			.use(arbitraries.get(1)).in((element, value) -> {
				element.setValue(value);
				return element;
			})
			.build();
		return new ArbitraryIntrospectorResult(arbitrary);
	}
}
