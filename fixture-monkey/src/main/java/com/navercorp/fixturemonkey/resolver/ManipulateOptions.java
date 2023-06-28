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

package com.navercorp.fixturemonkey.resolver;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ManipulateOptions {
	private final MonkeyExpressionFactory defaultMonkeyExpressionFactory;

	private final List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;

	private final PropertyNameResolver defaultPropertyNameResolver;

	public ManipulateOptions(
		MonkeyExpressionFactory defaultMonkeyExpressionFactory,
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver
	) {
		this.defaultMonkeyExpressionFactory = defaultMonkeyExpressionFactory;
		this.registeredArbitraryBuilders = registeredArbitraryBuilders;
		this.propertyNameResolvers = propertyNameResolvers;
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
	}

	public MonkeyExpressionFactory getDefaultMonkeyExpressionFactory() {
		return defaultMonkeyExpressionFactory;
	}

	public List<MatcherOperator<? extends ArbitraryBuilder<?>>> getRegisteredArbitraryBuilders() {
		return registeredArbitraryBuilders;
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.propertyNameResolvers.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.defaultPropertyNameResolver);
	}

	public static ManipulateOptionsBuilder builder() {
		return new ManipulateOptionsBuilder();
	}
}
