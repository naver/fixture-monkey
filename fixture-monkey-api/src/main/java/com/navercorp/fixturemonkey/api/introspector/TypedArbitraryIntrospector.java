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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * Introspects specific properties matched by {@link Matcher}.
 */
@API(since = "0.6.2", status = Status.MAINTAINED)
public final class TypedArbitraryIntrospector implements ArbitraryIntrospector, Matcher {
	private final MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector;

	public TypedArbitraryIntrospector(MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector) {
		this.arbitraryIntrospector = arbitraryIntrospector;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		return arbitraryIntrospector.getOperator().introspect(context);
	}

	@Override
	public boolean match(Property property) {
		return arbitraryIntrospector.match(property);
	}

	@Nullable
	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		if (arbitraryIntrospector.match(property)) {
			return arbitraryIntrospector.getOperator().getRequiredPropertyGenerator(property);
		}

		return null;
	}
}
