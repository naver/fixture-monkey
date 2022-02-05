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

package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Field;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;

public class NullArbitraryGenerator implements ArbitraryGenerator {
	public static final NullArbitraryGenerator INSTANCE = new NullArbitraryGenerator();

	private final PropertyNameResolver propertyNameResolver = PropertyNameResolver.IDENTITY;

	@SuppressWarnings("rawtypes")
	@Override
	public <T> Arbitrary<T> generate(ArbitraryType type, List<ArbitraryNode> nodes) {
		return Arbitraries.just(null);
	}

	@Override
	public String resolveFieldName(Field field) {
		return this.propertyNameResolver.resolve(new FieldProperty(field));
	}
}
