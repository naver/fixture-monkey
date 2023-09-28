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

package com.navercorp.fixturemonkey.api.context;

import static com.navercorp.fixturemonkey.api.type.Types.isJavaType;

import java.util.TreeMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MonkeyContext {
	private final ConcurrentLruCache<Property, CombinableArbitrary<?>> arbitrariesByProperty;
	private final ConcurrentLruCache<Property, CombinableArbitrary<?>> javaArbitrariesByProperty;
	private final ConcurrentLruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty;

	public MonkeyContext(
		ConcurrentLruCache<Property, CombinableArbitrary<?>> arbitrariesByProperty,
		ConcurrentLruCache<Property, CombinableArbitrary<?>> javaArbitrariesByProperty,
		ConcurrentLruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty
	) {
		this.arbitrariesByProperty = arbitrariesByProperty;
		this.javaArbitrariesByProperty = javaArbitrariesByProperty;
		this.generatorContextByRootProperty = generatorContextByRootProperty;
	}

	public static MonkeyContextBuilder builder() {
		return new MonkeyContextBuilder();
	}

	public CombinableArbitrary<?> getCachedArbitrary(Property property) {
		CombinableArbitrary<?> javaTypeCombinableArbitrary = javaArbitrariesByProperty.get(property);
		if (javaTypeCombinableArbitrary != null) {
			return javaTypeCombinableArbitrary;
		}
		return arbitrariesByProperty.get(property);
	}

	public void putCachedArbitrary(Property property, CombinableArbitrary<?> combinableArbitrary) {
		Class<?> type = Types.getActualType(property.getType());
		if (isJavaType(type)) {
			javaArbitrariesByProperty.putIfAbsent(property, combinableArbitrary);
			return;
		}

		arbitrariesByProperty.put(property, combinableArbitrary);
	}

	public MonkeyGeneratorContext retrieveGeneratorContext(RootProperty rootProperty) {
		return generatorContextByRootProperty.computeIfAbsent(
			rootProperty,
			property -> new MonkeyGeneratorContext(new TreeMap<>())
		);
	}
}
