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

import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class EnumIntrospector implements ArbitraryIntrospector, Matcher {
	@Override
	public boolean match(Property property) {
		return Matchers.ENUM_TYPE_MATCHER.match(property);
	}

	@Override
	@SuppressWarnings("argument")
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getResolvedType());
		if (!type.isEnum()) {
			throw new IllegalArgumentException("Given type is not enum. type: " + type);
		}

		List<Object> enums = Arrays.asList(type.getEnumConstants());
		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.from(() -> enums.get(Randoms.nextInt(enums.size())))
		);
	}
}
