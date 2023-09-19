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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MapEntryElementIntrospector implements ArbitraryIntrospector, Matcher {
	@Override
	public boolean match(Property property) {
		return property.getClass() == MapEntryElementProperty.class;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

		if (elementCombinableArbitraryList.size() != 2) {
			throw new IllegalArgumentException("Key and Value should be exist for MapEntryElementType.");
		}

		List<CombinableArbitrary<?>> entryCombinableArbitraryList = new ArrayList<>();
		CombinableArbitrary<?> keyCombinableArbitrary = elementCombinableArbitraryList.get(0).filter(
			context.getGenerateUniqueMaxTries(),
			obj -> context.isUniqueAndCheck(
				Objects.requireNonNull(context.getOwnerContext()).getPropertyPath(),
				obj
			)
		);
		CombinableArbitrary<?> valueCombinableArbitrary = elementCombinableArbitraryList.get(1);
		entryCombinableArbitraryList.add(keyCombinableArbitrary);
		entryCombinableArbitraryList.add(valueCombinableArbitrary);

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.containerBuilder()
				.elements(entryCombinableArbitraryList)
				.build(this::combine)
		);
	}

	private Object combine(List<Object> elements) {
		MapEntryElementType elementType = new MapEntryElementType();
		elementType.setKey(elements.get(0));
		elementType.setValue(elements.get(1));
		return elementType;
	}
}
