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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.matcher.TypeMatcher;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class EnumTypeIntrospector implements ArbitraryTypeIntrospector, TypeMatcher {
	@Override
	public boolean match(AnnotatedType type) {
		return Matchers.ENUM_TYPE_MATCHER.match(type);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Type type = context.getType();
		return new ArbitraryIntrospectorResult(Arbitraries.of((Class<Enum>)type));
	}
}
