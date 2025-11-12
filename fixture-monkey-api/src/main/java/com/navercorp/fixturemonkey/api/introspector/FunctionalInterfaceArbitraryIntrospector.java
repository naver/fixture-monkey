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

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.jspecify.annotations.NonNull;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.21", status = API.Status.EXPERIMENTAL)
public final class FunctionalInterfaceArbitraryIntrospector implements ArbitraryIntrospector {
	private static final Map<Class<?>, String> FUNCTIONAL_INVOKE_METHOD_NAMES_BY_ASSIGNABLE_TYPE = new HashMap<>();
	private static final String INVOKE_METHOD_NAME = "invoke";

	static {
		FUNCTIONAL_INVOKE_METHOD_NAMES_BY_ASSIGNABLE_TYPE.put(Function.class, "apply");
		FUNCTIONAL_INVOKE_METHOD_NAMES_BY_ASSIGNABLE_TYPE.put(Supplier.class, "get");
	}

	@SuppressWarnings("argument")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		if (!property.isContainer()) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();
		int lastElementIndex = elementCombinableArbitraryList.size() - 1;
		CombinableArbitrary<?> result = elementCombinableArbitraryList
			.get(lastElementIndex)
			.map(it -> toFunctionalInterface(Types.getActualType(context.getResolvedType()), it));

		return new ArbitraryIntrospectorResult(result);
	}

	private <T> Object toFunctionalInterface(Class<?> type, @NonNull T value) {
		InvocationHandlerBuilder invocationHandlerBuilder = new InvocationHandlerBuilder(
			type,
			new HashMap<>()
		);

		for (Entry<Class<?>, String> entry : FUNCTIONAL_INVOKE_METHOD_NAMES_BY_ASSIGNABLE_TYPE.entrySet()) {
			if (entry.getKey().isAssignableFrom(type)) {
				invocationHandlerBuilder.put(entry.getValue(), value);
			}
		}

		if (invocationHandlerBuilder.isEmpty()) {
			invocationHandlerBuilder.put(INVOKE_METHOD_NAME, value);
		}

		return Proxy.newProxyInstance(
			type.getClassLoader(),
			new Class[] {type},
			invocationHandlerBuilder.build()
		);
	}
}
