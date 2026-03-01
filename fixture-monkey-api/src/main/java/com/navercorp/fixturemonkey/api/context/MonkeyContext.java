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

import java.util.List;
import java.util.TreeMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.ObjectBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * {@code FixtureMonkey} → {@code ArbitraryBuilder} → {@code ObjectTree} → {@link CombinableArbitrary}
 * 						1:N							1:N					1:1
 * <p>
 * It is a context within {@code FixtureMonkey}. It represents a status of the {@code FixtureMonkey}.
 * The {@code FixtureMonkey} should be the same if the {@link MonkeyContext} is the same.
 * <p>
 * It is for internal use only. It can be changed or removed at any time.
 */
@API(since = "0.4.0", status = Status.INTERNAL)
public final class MonkeyContext {
	private final ConcurrentLruCache<Property, CombinableArbitrary<?>> arbitrariesByProperty;
	private final ConcurrentLruCache<Property, CombinableArbitrary<?>> javaArbitrariesByProperty;
	private final ConcurrentLruCache<TreeRootProperty, MonkeyGeneratorContext> generatorContextByRootProperty;
	private final List<PriorityMatcherOperator<? extends ObjectBuilder<?>>> registeredArbitraryBuilders;
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	public MonkeyContext(
		ConcurrentLruCache<Property, CombinableArbitrary<?>> arbitrariesByProperty,
		ConcurrentLruCache<Property, CombinableArbitrary<?>> javaArbitrariesByProperty,
		ConcurrentLruCache<TreeRootProperty, MonkeyGeneratorContext> generatorContextByRootProperty,
		List<PriorityMatcherOperator<? extends ObjectBuilder<?>>> registeredArbitraryBuilders,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		this.arbitrariesByProperty = arbitrariesByProperty;
		this.javaArbitrariesByProperty = javaArbitrariesByProperty;
		this.generatorContextByRootProperty = generatorContextByRootProperty;
		this.registeredArbitraryBuilders = registeredArbitraryBuilders;
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
	}

	public static MonkeyContextBuilder builder(FixtureMonkeyOptions fixtureMonkeyOptions) {
		return new MonkeyContextBuilder(fixtureMonkeyOptions);
	}

	public @Nullable CombinableArbitrary<?> getCachedArbitrary(Property property) {
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

	public MonkeyGeneratorContext newGeneratorContext(
		TreeRootProperty rootProperty
	) {
		return generatorContextByRootProperty.computeIfAbsent(
			rootProperty,
			property -> new MonkeyGeneratorContext(new TreeMap<>())
		);
	}

	public List<PriorityMatcherOperator<? extends ObjectBuilder<?>>> getRegisteredArbitraryBuilders() {
		return registeredArbitraryBuilders;
	}

	public FixtureMonkeyOptions getFixtureMonkeyOptions() {
		return fixtureMonkeyOptions;
	}
}
