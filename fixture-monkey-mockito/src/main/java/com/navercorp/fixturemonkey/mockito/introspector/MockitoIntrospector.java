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

package com.navercorp.fixturemonkey.mockito.introspector;

import java.lang.reflect.Modifier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.mockito.Mockito;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MockitoIntrospector implements ArbitraryIntrospector, Matcher {
	public static final MockitoIntrospector INSTANCE = new MockitoIntrospector();

	@Override
	public boolean match(Property property) {
		Class<?> actualType = Types.getActualType(property.getType());
		return Modifier.isAbstract(actualType.getModifiers());
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> actualType = Types.getActualType(context.getResolvedType());
		return new ArbitraryIntrospectorResult(Arbitraries.of(Mockito.mock(actualType)));
	}
}
