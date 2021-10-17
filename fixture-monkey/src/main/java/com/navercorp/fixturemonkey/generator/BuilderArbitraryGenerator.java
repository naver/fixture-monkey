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

package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.property.DefaultPropertyNameResolver;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class BuilderArbitraryGenerator extends AbstractArbitraryGenerator {
	public static final BuilderArbitraryGenerator INSTANCE = new BuilderArbitraryGenerator();
	private static final Map<Class<?>, Method> BUILDER_CACHE = new ConcurrentHashMap<>();
	private static final Map<String, Method> BUILD_FIELD_METHOD_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Method> BUILD_METHOD_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Class<?>> BUILDER_TYPE_CACHE = new ConcurrentHashMap<>();

	private final ArbitraryCustomizers arbitraryCustomizers;
	private String defaultBuildMethodName = "build";
	private String defaultBuilderMethodName = "builder";
	private final Map<Class<?>, String> typedBuilderMethodName = new ConcurrentHashMap<>();
	private final Map<Class<?>, String> typedBuildMethodName = new ConcurrentHashMap<>();

	private final PropertyNameResolver propertyNameResolver = new DefaultPropertyNameResolver();

	public BuilderArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private BuilderArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	private static void clearMethodCache() {
		BUILDER_CACHE.clear();
		BUILD_FIELD_METHOD_CACHE.clear();
		BUILD_METHOD_CACHE.clear();
	}

	@Override
	public <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Map<String, Arbitrary> arbitraryMap =
			toArbitrariesByFieldName(nodes, ArbitraryNode::getFieldName, (node, arbitrary) -> arbitrary);

		Class<T> clazz = type.getType();

		BuilderFieldArbitraries fieldArbitraries =
			BuilderFieldArbitraries.withBuilderType(this.getBuilderType(clazz), arbitraryMap);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);
		this.arbitraryCustomizers.customizeBuilderFields(clazz, fieldArbitraries);

		Method builderMethod = BUILDER_CACHE.get(clazz);
		Class<?> builderType = this.getBuilderType(clazz);
		Combinators.BuilderCombinator builderCombinator = Combinators.withBuilder(() ->
			ReflectionUtils.invokeMethod(builderMethod, null));

		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String methodName = entry.getKey();
			String buildFieldMethodName = builderType.getName() + "#" + methodName;
			Method method = BUILD_FIELD_METHOD_CACHE.computeIfAbsent(buildFieldMethodName, f -> {
				Method buildFieldMethod = ReflectionUtils.findMethods(builderType, m -> m.getName().equals(methodName))
					.stream()
					.filter(Objects::nonNull)
					.filter(m -> m.getParameterCount() == 1)
					.findFirst()
					.orElse(null);
				if (buildFieldMethod != null) {
					buildFieldMethod.setAccessible(true);
				}
				return buildFieldMethod;
			});

			if (method != null) {
				builderCombinator = builderCombinator.use(entry.getValue())
					.in((b, v) -> v != null ? ReflectionUtils.invokeMethod(method, b, v) : b);
			}
		}

		List<Map.Entry<Arbitrary, Combinators.F2<?, ?, ?>>> chains = fieldArbitraries.getCombinationChains();

		for (Map.Entry<Arbitrary, Combinators.F2<?, ?, ?>> entry : chains) {
			builderCombinator = builderCombinator.use(entry.getKey()).in(entry.getValue());
		}

		Method buildMethod = BUILD_METHOD_CACHE.computeIfAbsent(builderType, t -> {
			String buildMethodName = this.getBuildMethodName(t);
			Method method = ReflectionUtils.findMethod(builderType, buildMethodName)
				.orElseThrow(() -> new IllegalStateException(
					"Can not find BuilderCombiner build method for clazz. clazz: " + clazz));
			method.setAccessible(true);
			return method;
		});
		return builderCombinator.build(b -> {
			b = this.arbitraryCustomizers.customizeBuilder(clazz, b);
			if (b == null) {
				return null;
			}
			T fixture = (T)ReflectionUtils.invokeMethod(buildMethod, b);
			return this.arbitraryCustomizers.customizeFixture(clazz, fixture);
		});
	}

	private Class<?> getBuilderType(Class<?> objectType) {
		Method builderMethod = BUILDER_CACHE.computeIfAbsent(objectType, t -> {
			String builderMethodName = this.getBuilderMethodName(t);
			Method method = ReflectionUtils.findMethod(t, builderMethodName).orElse(null);
			if (method != null) {
				method.setAccessible(true);
			}
			return method;
		});

		if (builderMethod == null) {
			throw new IllegalArgumentException("Class has no builder class. " + objectType.getName());
		}

		return BUILDER_TYPE_CACHE.computeIfAbsent(objectType, t -> {
			Object builder = ReflectionUtils.invokeMethod(builderMethod, null);
			return builder.getClass();
		});
	}

	public String getBuildMethodName(Class<?> type) {
		return this.typedBuildMethodName.getOrDefault(type, defaultBuildMethodName);
	}

	public String getBuilderMethodName(Class<?> type) {
		return this.typedBuilderMethodName.getOrDefault(type, defaultBuilderMethodName);
	}

	public void setDefaultBuilderMethodName(String defaultBuilderMethodName) {
		this.defaultBuilderMethodName = defaultBuilderMethodName;
		clearMethodCache();
	}

	public void setDefaultBuildMethodName(String defaultBuildMethodName) {
		this.defaultBuildMethodName = defaultBuildMethodName;
		clearMethodCache();
	}

	public void setBuilderMethodName(Class<?> type, String builderMethodName) {
		this.typedBuilderMethodName.put(type, builderMethodName);
		clearMethodCache();
	}

	public void setBuildMethodName(Class<?> type, String buildMethodName) {
		this.typedBuildMethodName.put(type, buildMethodName);
		clearMethodCache();
	}

	@Override
	public String resolveFieldName(Field field) {
		return this.propertyNameResolver.resolve(new FieldProperty(field));
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}

		return new BuilderArbitraryGenerator(arbitraryCustomizers);
	}
}
