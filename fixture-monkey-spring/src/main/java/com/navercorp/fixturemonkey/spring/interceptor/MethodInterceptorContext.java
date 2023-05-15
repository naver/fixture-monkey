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

package com.navercorp.fixturemonkey.spring.interceptor;

import static com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation.ManipulationType.FIX;
import static com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation.ManipulationType.WITH;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation.ManipulationObject;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.RequestMethod;

public final class MethodInterceptorContext {
	private static final ThreadLocal<Map<Class<?>, RequestTarget>> targetsByType =
		ThreadLocal.withInitial(() -> new HashMap<>(2048));

	public static BeanManipulation bean(Class<?> beanType) {
		return new BeanManipulation(beanType);
	}

	static Map<String, ManipulationObject> getManipulatingObjectsByExpression(Class<?> beanType,
		RequestMethod<?> requestMethod) {
		RequestTarget requestTarget = targetsByType.get()
			.computeIfAbsent(beanType, type -> new RequestTarget(beanType, new HashMap<>()));

		FixtureMonkeyManipulation fixtureMonkeyManipulation =
			requestTarget.manipulationsByRequestMethod.get(requestMethod);

		if (fixtureMonkeyManipulation == null) {
			return Map.of();
		}

		return fixtureMonkeyManipulation.getValuesByExpression();
	}

	public static void clear() {
		targetsByType.remove();
	}

	public record BeanManipulation(
		Class<?> beanType
	) {
		public FixtureMonkeyManipulation methodName(String methodName) {
			return method(beanType, new RequestMethod<>(null, methodName));
		}

		public FixtureMonkeyManipulation methodReturnType(Class<?> returnType) {
			return method(beanType, new RequestMethod<>(returnType, null));
		}

		public FixtureMonkeyManipulation method(Class<?> returnType, String methodName) {
			return method(beanType, new RequestMethod<>(returnType, methodName));
		}

		private FixtureMonkeyManipulation method(Class<?> type, RequestMethod<?> requestMethod) {
			RequestTarget requestTarget = targetsByType.get().computeIfAbsent(type, key ->
				new RequestTarget(key, new HashMap<>())
			);

			return requestTarget.manipulationsByRequestMethod.computeIfAbsent(
				requestMethod,
				key -> new FixtureMonkeyManipulation(new LinkedHashMap<>())
			);
		}
	}

	public record RequestTarget(
		Class<?> type,

		Map<RequestMethod<?>, FixtureMonkeyManipulation> manipulationsByRequestMethod
	) {
		public record RequestMethod<T>(
			@Nullable
			Class<T> returnType,

			@Nullable
			String methodName
		) {
			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null || getClass() != obj.getClass()) {
					return false;
				}
				RequestMethod<?> that = (RequestMethod<?>)obj;
				if (methodName == null || that.methodName == null) {
					return Objects.equals(returnType, that.returnType);
				}

				if (returnType == null || that.returnType == null) {
					return Objects.equals(methodName, that.methodName);
				}

				return Objects.equals(returnType, that.returnType) && Objects.equals(methodName, that.methodName);
			}

			@Override
			public int hashCode() {
				return 1004; // The equality only depends on equals.
			}
		}

		public static class FixtureMonkeyManipulation {
			private final Map<String, ManipulationObject> valuesByExpression;

			FixtureMonkeyManipulation(Map<String, ManipulationObject> valuesByExpression) {
				this.valuesByExpression = valuesByExpression;
			}

			public FixtureMonkeyManipulation fix(String expression, Object value) {
				valuesByExpression.put(expression, new ManipulationObject(value, FIX));
				return this;
			}

			public FixtureMonkeyManipulation withInitial(String expression, Object value) {
				valuesByExpression.put(expression, new ManipulationObject(value, WITH));
				return this;
			}

			Map<String, ManipulationObject> getValuesByExpression() {
				return valuesByExpression;
			}

			static class ManipulationObject {
				private final Object value;
				private final ManipulationType manipulationType;

				public ManipulationObject(Object value, ManipulationType manipulationType) {
					this.value = value;
					this.manipulationType = manipulationType;
				}

				public Object getValue() {
					return value;
				}

				public ManipulationType getManipulationType() {
					return manipulationType;
				}
			}

			public enum ManipulationType {
				FIX,
				WITH,
			}
		}
	}
}
