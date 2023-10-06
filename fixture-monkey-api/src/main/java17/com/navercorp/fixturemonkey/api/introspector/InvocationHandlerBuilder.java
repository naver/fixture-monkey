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
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A builder generates {@link InvocationHandler} for generating an anonymous instance of interface dynamically.
 */
@SuppressWarnings("unused")
@API(since = "0.6.10", status = Status.EXPERIMENTAL)
final class InvocationHandlerBuilder {
	private final Map<String, Object> generatedValuesByMethodName;

	InvocationHandlerBuilder(Map<String, Object> generatedValuesByMethodName) {
		this.generatedValuesByMethodName = generatedValuesByMethodName;
	}

	void put(String methodName, Object value) {
		generatedValuesByMethodName.put(methodName, value);
	}

	InvocationHandler build() {
		return (proxy, method, args) -> {
			if (method.isDefault()) {
				return InvocationHandler.invokeDefault(proxy, method, args);
			}
			return generatedValuesByMethodName.get(method.getName());
		};
	}

	boolean isEmpty() {
		return generatedValuesByMethodName.isEmpty();
	}
}
