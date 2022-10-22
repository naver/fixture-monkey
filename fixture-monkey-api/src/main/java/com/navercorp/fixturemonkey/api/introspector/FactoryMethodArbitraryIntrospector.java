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

import static com.navercorp.fixturemonkey.api.property.PropertyCache.getParametersByFactoryMethods;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.2", status = Status.EXPERIMENTAL)
public final class FactoryMethodArbitraryIntrospector implements ArbitraryIntrospector {
	public static final FactoryMethodArbitraryIntrospector INSTANCE =
		new FactoryMethodArbitraryIntrospector();

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		Map<String, Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts()
			.getArbitrariesByResolvedName();

		Map<Method, Parameter[]> parametersByFactoryMethods = getParametersByFactoryMethods(type);
		if (parametersByFactoryMethods.isEmpty()) {
			throw new IllegalArgumentException("factory method does not exist. type " + type.getSimpleName());
		}

		List<Entry<Method, Parameter[]>> entries = new ArrayList<>(parametersByFactoryMethods.entrySet());
		Entry<Method, Parameter[]> parametersByMethod = entries.get(Randoms.nextInt(entries.size()));

		Method factoryMethod = parametersByMethod.getKey();
		Parameter[] parameters = parametersByMethod.getValue();

		int parameterSize = parameters.length;

		Builders.BuilderCombinator<List<Object>> builderCombinator =
			Builders.withBuilder(() -> new ArrayList<>(parameterSize));

		for (Parameter parameter : parameters) {
			Arbitrary<?> arbitrary = childrenArbitraries.get(parameter.getName());

			builderCombinator = builderCombinator.use(arbitrary).in((list, value) -> {
				list.add(value);
				return list;
			});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(
				list -> ReflectionUtils.invokeMethod(factoryMethod, null, list.toArray())
			)
		);
	}
}
