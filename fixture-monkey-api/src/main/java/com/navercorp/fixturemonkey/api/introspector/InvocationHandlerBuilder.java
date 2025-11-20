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
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A builder generates {@link InvocationHandler} for generating an anonymous instance of interface dynamically.
 */
@API(since = "0.6.10", status = Status.MAINTAINED)
final class InvocationHandlerBuilder {
	private static final String HASH_CODE_METHOD = "hashCode";
	private static final String EQUALS_METHOD = "equals";
	private static final String TO_STRING_METHOD = "toString";
	private static final String INVOKE_METHOD = "invoke";

	private final Class<?> type;
	private final Map<String, Object> generatedValuesByMethodName;

	InvocationHandlerBuilder(
		Class<?> type,
		Map<String, Object> generatedValuesByMethodName
	) {
		this.type = type;
		this.generatedValuesByMethodName = generatedValuesByMethodName;
	}

	void put(String methodName, Object value) {
		generatedValuesByMethodName.put(methodName, value);
	}

	@SuppressWarnings("return")
	InvocationHandler build() {
		return (proxy, method, args) -> {
			if (method == null) {
				// invoked by DecomposedContainerValueFactory to decompose the functional interface
				return generatedValuesByMethodName.get(INVOKE_METHOD);
			}

			if (HASH_CODE_METHOD.equals(method.getName()) && args == null) {
				return generatedValuesByMethodName.values().hashCode();
			}

			if (EQUALS_METHOD.equals(method.getName()) && args.length == 1) {
				Object other = args[0];
				return compareAllReturnValues(other);
			}

			if (TO_STRING_METHOD.equals(method.getName()) && args == null) {
				return toString(proxy);
			}

			return generatedValuesByMethodName.get(method.getName());
		};
	}

	boolean isEmpty() {
		return generatedValuesByMethodName.isEmpty();
	}

	private boolean compareAllReturnValues(Object other) {
		if (other == null) {
			return false;
		}

		if (!type.isInstance(other)) {
			return false;
		}

		for (Map.Entry<String, Object> methodNameToValue : generatedValuesByMethodName.entrySet()) {
			String methodName = methodNameToValue.getKey();
			Object returnValue = methodNameToValue.getValue();
			try {
				Method otherMethod = other.getClass().getMethod(methodName);
				Object otherValue = otherMethod.invoke(other);
				if (!returnValue.equals(otherValue)) {
					return false;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Unexpected error in invoking method " + methodName, e);
			}
		}

		return true;
	}

	private String toString(Object proxy) {
		String joined = generatedValuesByMethodName.entrySet().stream()
			.map(e -> e.getKey() + "=" + e.getValue())
			.collect(Collectors.joining(", "));
		return proxy.getClass().getName() + "{" + joined + "}";
	}
}
