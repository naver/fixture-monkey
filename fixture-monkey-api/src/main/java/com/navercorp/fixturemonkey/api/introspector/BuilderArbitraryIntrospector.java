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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.LazyCombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = API.Status.EXPERIMENTAL)
public final class BuilderArbitraryIntrospector implements ArbitraryIntrospector {
	public static final BuilderArbitraryIntrospector INSTANCE = new BuilderArbitraryIntrospector();
	private static final Map<Class<?>, Method> BUILDER_CACHE = new ConcurrentHashMap<>(2048);
	private static final Map<String, Method> BUILD_FIELD_METHOD_CACHE = new ConcurrentHashMap<>(2048);
	private static final Map<Class<?>, Method> BUILD_METHOD_CACHE = new ConcurrentHashMap<>(2048);
	private static final Map<Class<?>, Class<?>> BUILDER_TYPE_CACHE = new ConcurrentHashMap<>(2048);

	private final Map<Class<?>, String> typedBuilderMethodName = new ConcurrentHashMap<>(2048);
	private final Map<Class<?>, String> typedBuildMethodName = new ConcurrentHashMap<>(2048);
	private String defaultBuildMethodName = "build";
	private String defaultBuilderMethodName = "builder";

	private static void clearMethodCache() {
		BUILDER_CACHE.clear();
		BUILD_FIELD_METHOD_CACHE.clear();
		BUILD_METHOD_CACHE.clear();
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<ArbitraryProperty> childrenProperties = context.getChildren();
		Map<String, CombinableArbitrary> arbitrariesByResolvedName =
			context.getCombinableArbitrariesByResolvedName();

		LazyArbitrary<Arbitrary<Object>> generateArbitrary = LazyArbitrary.lazy(
			() -> {
				Class<?> builderType = this.getBuilderType(type);
				Method builderMethod = BUILDER_CACHE.get(type);

				BuilderCombinator<Object> builderCombinator = Builders.withBuilder(() ->
					Reflections.invokeMethod(builderMethod, null));

				for (ArbitraryProperty arbitraryProperty : childrenProperties) {
					String methodName = getFieldName(arbitraryProperty.getObjectProperty().getProperty());
					Class<?> actualType = getActualType(arbitraryProperty.getObjectProperty().getProperty());
					String buildFieldMethodName = builderType.getName() + "#" + methodName;

					String resolvePropertyName =
						arbitraryProperty.getObjectProperty().getResolvedPropertyName();
					CombinableArbitrary combinableArbitrary =
						arbitrariesByResolvedName.get(resolvePropertyName);

					Method method = BUILD_FIELD_METHOD_CACHE.computeIfAbsent(buildFieldMethodName, f -> {
						Method buildFieldMethod = Reflections.findMethod(builderType, methodName, actualType);
						if (buildFieldMethod != null) {
							buildFieldMethod.setAccessible(true);
						}
						return buildFieldMethod;
					});
					if (method != null) {
						builderCombinator = builderCombinator.use(combinableArbitrary.combined())
							.in((b, v) -> v != null ? Reflections.invokeMethod(method, b, v) : b);
					}
				}

				Method buildMethod = BUILD_METHOD_CACHE.computeIfAbsent(builderType, t -> {
					String buildMethodName = typedBuildMethodName.getOrDefault(t, defaultBuildMethodName);
					Method method = Reflections.findMethod(builderType, buildMethodName);
					if (method == null) {
						throw new IllegalStateException(
							"Can not find BuilderCombiner build method for clazz. clazz: " + type
						);
					}
					method.setAccessible(true);
					return method;
				});
				return builderCombinator.build(b -> {
					if (b == null) {
						return null;
					}
					return Reflections.invokeMethod(buildMethod, b);
				});
			}
		);
		return new ArbitraryIntrospectorResult(new LazyCombinableArbitrary(generateArbitrary));
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

	private Class<?> getBuilderType(Class<?> objectType) {
		Method builderMethod = BUILDER_CACHE.computeIfAbsent(objectType, t -> {
			String builderMethodName = typedBuilderMethodName.getOrDefault(t, defaultBuilderMethodName);
			Method method = Reflections.findMethod(t, builderMethodName);
			if (method != null) {
				method.setAccessible(true);
			}
			return method;
		});

		if (builderMethod == null) {
			throw new IllegalArgumentException("Class has no builder class. " + objectType.getName());
		}

		return BUILDER_TYPE_CACHE.computeIfAbsent(objectType, t -> {
			Object builder = Reflections.invokeMethod(builderMethod, null);
			return builder.getClass();
		});
	}

	private String getFieldName(Property property) {
		return getActualProperty(property).getName();
	}

	private Class<?> getActualType(Property property) {
		return Types.getActualType(getActualProperty(property).getType());
	}

	private Property getActualProperty(Property property) {
		if (property instanceof CompositeProperty) {
			CompositeProperty compositeProperty = (CompositeProperty)property;
			if (compositeProperty.getPrimaryProperty() instanceof FieldProperty) {
				return ((CompositeProperty)property).getPrimaryProperty();
			} else if (compositeProperty.getSecondaryProperty() instanceof FieldProperty) {
				return ((CompositeProperty)property).getSecondaryProperty();
			}
		}
		return property;
	}
}
