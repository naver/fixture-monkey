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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = API.Status.MAINTAINED)
public final class BuilderArbitraryIntrospector implements ArbitraryIntrospector {
	public static final BuilderArbitraryIntrospector INSTANCE = new BuilderArbitraryIntrospector();

	private static final Logger LOGGER = LoggerFactory.getLogger(BuilderArbitraryIntrospector.class);
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

	@SuppressWarnings({"argument", "return", "dereference.of.nullable"})
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		List<ArbitraryProperty> childrenProperties = context.getChildren();
		Map<String, CombinableArbitrary<?>> arbitrariesByResolvedName =
			context.getCombinableArbitrariesByResolvedName();

		Class<?> builderType;
		Method buildMethod;
		try {
			builderType = this.getBuilderType(type);
			buildMethod = BUILD_METHOD_CACHE.computeIfAbsent(
				builderType,
				t -> {
					String buildMethodName = typedBuildMethodName.getOrDefault(t, defaultBuildMethodName);
					Method method = Reflections.findMethod(builderType, buildMethodName);
					if (method == null) {
						throw new IllegalStateException(
							"Can not retrieve a build method. type: " + type + " buildMethodName: " + buildMethodName
						);
					}
					method.setAccessible(true);
					return method;
				}
			);
		} catch (Exception ex) {
			ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
			if (loggingContext.isEnableLoggingFail()) {
				LOGGER.warn("Given type {} is failed to generate due to the exception. It may be null.", type, ex);
			}
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}
		Method builderMethod = BUILDER_CACHE.get(type);

		LazyArbitrary<Object> generateArbitrary = LazyArbitrary.lazy(
			() -> {
				Object builder = Reflections.invokeMethod(builderMethod, null);

				for (ArbitraryProperty arbitraryProperty : childrenProperties) {
					String methodName = getFieldName(arbitraryProperty.getObjectProperty().getProperty());
					Class<?> actualType = getActualType(arbitraryProperty.getObjectProperty().getProperty());
					String buildFieldMethodName = builderType.getName() + "#" + methodName;

					String resolvePropertyName =
						arbitraryProperty.getObjectProperty().getResolvedPropertyName();
					CombinableArbitrary<?> combinableArbitrary =
						arbitrariesByResolvedName.get(resolvePropertyName);

					Method method = BUILD_FIELD_METHOD_CACHE.computeIfAbsent(buildFieldMethodName, f -> {
						Method buildFieldMethod = Reflections.findMethod(builderType, methodName, actualType);
						if (buildFieldMethod != null) {
							buildFieldMethod.setAccessible(true);
						}
						return buildFieldMethod;
					});
					if (method != null) {
						Object child = combinableArbitrary.combined();
						if (child != null) {
							Reflections.invokeMethod(method, builder, child);
						}
					}
				}

				return Reflections.invokeMethod(buildMethod, builder);
			}
		);
		return new ArbitraryIntrospectorResult(CombinableArbitrary.from(generateArbitrary));
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

	@SuppressWarnings({"return", "argument"})
	private @Nullable Class<?> getBuilderType(Class<?> objectType) {
		String builderMethodName = typedBuilderMethodName.getOrDefault(objectType, defaultBuilderMethodName);
		Method builderMethod = BUILDER_CACHE.computeIfAbsent(objectType,  t -> {
			Method method = Reflections.findMethod(t, builderMethodName);
			if (method != null) {
				method.setAccessible(true);
			}
			return method;
		});

		if (builderMethod == null) {
			throw new IllegalArgumentException(
				"Class has no builder class. "
					+ "type: " + objectType.getName() + " builderMethodName: " + builderMethodName
			);
		}

		return BUILDER_TYPE_CACHE.computeIfAbsent(objectType, t -> {
			Object builder = Reflections.invokeMethod(builderMethod, null);
			return builder.getClass();
		});
	}

	@SuppressWarnings("return")
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
