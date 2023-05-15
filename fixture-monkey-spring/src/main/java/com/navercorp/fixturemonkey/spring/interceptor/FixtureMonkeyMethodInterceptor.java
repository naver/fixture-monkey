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

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import reactor.core.publisher.Mono;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation.ManipulationObject;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.FixtureMonkeyManipulation.ManipulationType;
import com.navercorp.fixturemonkey.spring.interceptor.MethodInterceptorContext.RequestTarget.RequestMethod;

public final class FixtureMonkeyMethodInterceptor implements MethodInterceptor {
	private final FixtureMonkey fixtureMonkey;
	private final ThreadLocal<Map<MethodCallIdentifier, Map<String, Object>>> initialCachesByMethodCallIdentifier =
		ThreadLocal.withInitial(HashMap::new);

	public FixtureMonkeyMethodInterceptor(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@SuppressWarnings({"rawtypes", "unchecked", "ReactiveStreamsUnusedPublisher"})
	@Nullable
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object methodReturnObject = invocation.proceed();

		Method method = invocation.getMethod();
		if (Modifier.isStatic(method.getModifiers())) {
			return methodReturnObject;
		}

		Class<?> callerType = requireNonNull(invocation.getThis()).getClass();
		Class<?> returnType = getReturnType(method);

		if (AopUtils.isAopProxy(invocation.getThis()) && invocation.getThis() instanceof Advised advised) {
			callerType = advised.getProxiedInterfaces()[0];
		}

		Map<String, ManipulationObject> manipulators = MethodInterceptorContext.getManipulatingObjectsByExpression(
			callerType,
			new RequestMethod(returnType, method.getName())
		);

		MethodCallIdentifier methodCallIdentifier = new MethodCallIdentifier(
			method.getName(),
			method.getReturnType(),
			Arrays.asList(invocation.getArguments())
		);

		Map<String, Object> initialValuesByExpression = initialCachesByMethodCallIdentifier.get().computeIfAbsent(
			methodCallIdentifier,
			identifier -> new HashMap<>()
		);

		if (methodReturnObject instanceof Mono mono) {
			ExperimentalArbitraryBuilder<?> fallbackBuilder = fixtureMonkey.giveMeExperimentalBuilder(returnType);
			applyManipulators(manipulators, fallbackBuilder, initialValuesByExpression);
			return mono.defaultIfEmpty(fallbackBuilder.sample())
				.onErrorResume(throwable -> Mono.just(fallbackBuilder.sample()))
				.map(value -> {
					ExperimentalArbitraryBuilder<?> builder = fixtureMonkey.giveMeExperimentalBuilder(value);
					applyManipulators(manipulators, builder, initialValuesByExpression);
					return builder.sample();
				})
				.onErrorResume(throwable -> Mono.just(fixtureMonkey.giveMeOne(returnType)));
		} else if (methodReturnObject instanceof Optional optional) {
			return optional.map(value -> {
				ExperimentalArbitraryBuilder<?> builder = fixtureMonkey.giveMeExperimentalBuilder(value);
				applyManipulators(manipulators, builder, initialValuesByExpression);
				return builder.sample();
			});
		}

		if (manipulators.isEmpty()) {
			return methodReturnObject;
		}

		ExperimentalArbitraryBuilder<?> builder;
		if (methodReturnObject == null) {
			builder = fixtureMonkey.giveMeExperimentalBuilder(returnType);
		} else {
			builder = fixtureMonkey.giveMeExperimentalBuilder(methodReturnObject);
		}
		applyManipulators(manipulators, builder, initialValuesByExpression);
		return builder.sample();
	}

	private static void applyManipulators(
		Map<String, ManipulationObject> manipulators,
		ExperimentalArbitraryBuilder<?> builder,
		Map<String, Object> initialValuesByExpression
	) {
		manipulators.forEach(
			(expression, value) -> {
				if (value.getManipulationType() == ManipulationType.WITH) {
					builder.customizeProperty(typedString(expression), arbitrary ->
						arbitrary.map(sampled -> {
							Object initialValue = initialValuesByExpression.putIfAbsent(expression, sampled);

							if (initialValue == null || Objects.equals(initialValue, sampled)) {
								return value.getValue();
							}

							return sampled;
						})
					);
				} else {
					builder.set(expression, value.getValue());
				}
			}
		);
	}

	private static Class<?> getReturnType(Method method) {
		Class<?> returnType;
		AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();
		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(annotatedReturnType);
		if (genericsTypes.isEmpty()) {
			returnType = method.getReturnType();
		} else {
			returnType = Types.getActualType(genericsTypes.get(0));
		}
		return returnType;
	}

	public record MethodCallIdentifier(
		String methodName,

		Class<?> returnType,

		List<Object> arguments
	) {
	}
}
