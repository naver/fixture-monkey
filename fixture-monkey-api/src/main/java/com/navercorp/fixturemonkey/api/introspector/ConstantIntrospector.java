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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.ConstantProperty;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "1.0.17", status = Status.EXPERIMENTAL)
public final class ConstantIntrospector implements ArbitraryIntrospector, Matcher {
	public static final ConstantIntrospector INSTANCE = new ConstantIntrospector();

	@Override
	public boolean match(Property property) {
		return property instanceof ConstantProperty;
	}

	@SuppressWarnings("argument")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property constantProperty = context.getResolvedProperty();
		if (!match(constantProperty)) {
			throw new IllegalArgumentException(
				"Given type is not Constant Property. property: " + constantProperty
			);
		}

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.from(() -> constantProperty.getValue(null))
		);
	}
}
