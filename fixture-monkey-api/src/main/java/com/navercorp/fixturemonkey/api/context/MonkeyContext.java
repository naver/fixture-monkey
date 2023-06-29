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

import java.util.TreeMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.LruCache;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class MonkeyContext {
	private final LruCache<Property, CombinableArbitrary> arbitrariesByProperty;
	private final LruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty;

	public MonkeyContext(
		LruCache<Property, CombinableArbitrary> arbitrariesByProperty,
		LruCache<RootProperty, MonkeyGeneratorContext> generatorContextByRootProperty
	) {
		this.arbitrariesByProperty = arbitrariesByProperty;
		this.generatorContextByRootProperty = generatorContextByRootProperty;
	}

	public static MonkeyContextBuilder builder() {
		return new MonkeyContextBuilder();
	}

	public CombinableArbitrary getCachedArbitrary(Property property) {
		return arbitrariesByProperty.get(property);
	}

	public void putCachedArbitrary(Property property, CombinableArbitrary combinableArbitrary) {
		arbitrariesByProperty.put(property, combinableArbitrary);
	}

	public MonkeyGeneratorContext retrieveGeneratorContext(RootProperty rootProperty) {
		return generatorContextByRootProperty.computeIfAbsent(
			rootProperty,
			property -> new MonkeyGeneratorContext(new TreeMap<>())
		);
	}
}
