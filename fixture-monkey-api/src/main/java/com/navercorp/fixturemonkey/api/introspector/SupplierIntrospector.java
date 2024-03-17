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

import java.util.function.Supplier;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

public final class SupplierIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Supplier.class);

	@Override
	public boolean match(Property property) {
		return SINGLE_GENERIC_TYPE_MATCHER.match(property) && MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		CombinableArbitrary<?> elementArbitrary = context.getElementCombinableArbitraryList()
			.get(0)
			.map(this::toSupplier);

		return new ArbitraryIntrospectorResult(elementArbitrary);
	}

	private <T> Supplier<T> toSupplier(T value) {
		return () -> value;
	}
}
