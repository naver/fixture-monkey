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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;

import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.MethodProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * It generates the anonymous object of interface which has no-argument methods.
 * It is a default fallback {@link ArbitraryIntrospector}, if set none of introspectors in the options.
 */
@API(since = "0.5.5", status = API.Status.EXPERIMENTAL)
public final class AnonymousArbitraryIntrospector implements ArbitraryIntrospector {
	public static final AnonymousArbitraryIntrospector INSTANCE = new AnonymousArbitraryIntrospector();

	/**
	 * Generates the anonymous object of interface which has no-argument methods.
	 *
	 * @param context introspector context
	 * @return an anonymous object of the interface
	 */
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());

		Map<String, CombinableArbitrary> arbitrariesByPropertyName =
			context.getCombinableArbitrariesByPropertyName();

		List<ArbitraryProperty> childrenProperties = context.getChildren();

		BuilderCombinator<InvocationHandlerBuilder> builderCombinator = Builders.withBuilder(
			() -> new InvocationHandlerBuilder(new HashMap<>())
		);

		for (ArbitraryProperty arbitraryProperty : childrenProperties) {
			Property childProperty = arbitraryProperty.getObjectProperty().getProperty();

			if (!(childProperty instanceof MethodProperty)) {
				continue;
			}

			MethodProperty methodProperty = (MethodProperty)childProperty;

			builderCombinator = builderCombinator
				.use(arbitrariesByPropertyName.get(childProperty.getName()).combined())
				.in((builder, value) -> {
					builder.put(methodProperty.getMethodName(), value);
					return builder;
				});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(
				builder -> {
					if (builder.generatedValuesByMethodName.isEmpty()) {
						return null;
					}

					return type.cast(
						Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, builder.build()));
				}
			)
		);
	}

	private static final class InvocationHandlerBuilder {
		private final Map<String, Object> generatedValuesByMethodName;

		private InvocationHandlerBuilder(Map<String, Object> generatedValuesByMethodName) {
			this.generatedValuesByMethodName = generatedValuesByMethodName;
		}

		private void put(String methodName, Object value) {
			generatedValuesByMethodName.put(methodName, value);
		}

		private InvocationHandler build() {
			return (proxy, method, args) -> generatedValuesByMethodName.get(method.getName());
		}
	}
}
