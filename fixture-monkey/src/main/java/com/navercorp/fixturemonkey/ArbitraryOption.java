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

package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import com.navercorp.fixturemonkey.arbitrary.ContainerArbitraryNodeGenerator;
import com.navercorp.fixturemonkey.arbitrary.InterfaceSupplier;
import com.navercorp.fixturemonkey.arbitrary.NullableArbitraryEvaluator;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BigDecimalAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BigIntegerAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BooleanAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ByteAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.CalendarAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.CharacterAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.DateAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.DoubleAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.DoubleStreamAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FloatAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.InstantAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.IntStreamAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.IntegerAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.LocalDateAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.LocalDateTimeAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.LocalTimeAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.LongAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.LongStreamAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.OptionalDoubleAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.OptionalIntAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.OptionalLongAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ShortAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.StringAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.UuidAnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ZonedDateTimeAnnotatedArbitraryGenerator;

public final class ArbitraryOption {
	public static final ArbitraryOption DEFAULT_FIXTURE_OPTIONS = ArbitraryOption.builder().build();

	private final Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap;
	private final Map<Class<?>, InterfaceSupplier<?>> interfaceSupplierMap;
	private final Map<Class<?>, Function<FixtureMonkey, ArbitraryBuilder<?>>> arbitraryBuildingSupplierMap;
	private final Map<Class<?>, ArbitraryBuilder<?>> defaultArbitraryBuilderMap;
	private final Map<Class<?>, ContainerArbitraryNodeGenerator> containerArbitraryNodeGeneratorMap;
	private final Set<String> exceptGeneratePackages;
	private final Set<Class<?>> exceptGenerateClasses;
	private final Set<String> nonNullAnnotationNames;
	private final NullableArbitraryEvaluator nullableArbitraryEvaluator;
	private final InterfaceSupplier<?> defaultInterfaceSupplier;
	private final double nullInject;
	private final boolean nullableContainer;
	private final boolean defaultNotNull;

	public ArbitraryOption(
		Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap,
		Map<Class<?>, InterfaceSupplier<?>> interfaceSupplierMap,
		Map<Class<?>, Function<FixtureMonkey, ArbitraryBuilder<?>>> arbitraryBuildingSupplierMap,
		Map<Class<?>, ContainerArbitraryNodeGenerator> containerArbitraryNodeGeneratorMap,
		Set<String> exceptGeneratePackages,
		Set<Class<?>> exceptGenerateClasses,
		Set<String> nonNullAnnotationNames,
		InterfaceSupplier<?> defaultInterfaceSupplier,
		NullableArbitraryEvaluator nullableArbitraryEvaluator,
		double nullInject,
		boolean nullableContainer,
		boolean defaultNotNull
	) {
		this.annotatedArbitraryMap = annotatedArbitraryMap;
		this.interfaceSupplierMap = interfaceSupplierMap;
		this.arbitraryBuildingSupplierMap = arbitraryBuildingSupplierMap;
		this.containerArbitraryNodeGeneratorMap = containerArbitraryNodeGeneratorMap;
		this.defaultArbitraryBuilderMap = new HashMap<>();
		this.exceptGeneratePackages = exceptGeneratePackages;
		this.exceptGenerateClasses = exceptGenerateClasses;
		this.nonNullAnnotationNames = nonNullAnnotationNames;
		this.defaultInterfaceSupplier = defaultInterfaceSupplier;
		this.nullableArbitraryEvaluator = nullableArbitraryEvaluator;
		this.nullInject = nullInject;
		this.nullableContainer = nullableContainer;
		this.defaultNotNull = defaultNotNull;
	}

	public Set<String> getExceptGeneratePackages() {
		return exceptGeneratePackages;
	}

	public Set<Class<?>> getExceptGenerateClasses() {
		return exceptGenerateClasses;
	}

	public NullableArbitraryEvaluator getNullableArbitraryEvaluator() {
		return nullableArbitraryEvaluator;
	}

	public double getNullInject() {
		return nullInject;
	}

	public boolean isNullableContainer() {
		return nullableContainer;
	}

	public Map<Class<?>, AnnotatedArbitraryGenerator<?>> getAnnotatedArbitraryMap() {
		return annotatedArbitraryMap;
	}

	public <T> boolean isDefaultArbitraryType(Class<T> clazz) {
		return annotatedArbitraryMap.containsKey(clazz);
	}

	public <T> boolean isExceptGeneratablePackage(Class<T> clazz) {
		if (clazz.getPackage() == null) {
			return false; // primitive
		}
		String packageName = clazz.getPackage().getName();
		return exceptGeneratePackages.stream()
			.noneMatch(packageName::startsWith);
	}

	public <T> boolean isGeneratableClass(Class<T> clazz) {
		return !exceptGenerateClasses.contains(clazz);
	}

	public Map<Class<?>, InterfaceSupplier<?>> getInterfaceSupplierMap() {
		return interfaceSupplierMap;
	}

	@SuppressWarnings("unchecked")
	public <T> InterfaceSupplier<T> getInterfaceSupplierOrDefault(Class<T> clazz) {
		return (InterfaceSupplier<T>)interfaceSupplierMap.getOrDefault(clazz, defaultInterfaceSupplier);
	}

	public <T> Function<FixtureMonkey, ArbitraryBuilder<?>> getArbitraryBuildingSupplier(Class<T> clazz) {
		return arbitraryBuildingSupplierMap.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> ArbitraryBuilder<T> getDefaultArbitraryBuilder(Class<T> clazz) {
		ArbitraryBuilder<T> arbitraryBuilder = (ArbitraryBuilder<T>)defaultArbitraryBuilderMap.get(clazz);
		if (arbitraryBuilder != null) {
			return arbitraryBuilder.copy();
		}
		return null;
	}

	public void applyArbitraryBuilders(FixtureMonkey fixtureMonkey) {
		defaultArbitraryBuilderMap.putAll(arbitraryBuildingSupplierMap.entrySet().stream()
			.collect(toMap(Entry::getKey, it -> it.getValue().apply(fixtureMonkey))));
	}

	public boolean isNonNullAnnotation(Annotation annotation) {
		return nonNullAnnotationNames.contains(annotation.annotationType().getName());
	}

	public boolean isDefaultNotNull() {
		return defaultNotNull;
	}

	public <T> ContainerArbitraryNodeGenerator getContainerArbitraryNodeGenerator(Class<T> clazz) {
		return containerArbitraryNodeGeneratorMap.get(clazz);
	}

	public static FixtureOptionsBuilder builder() {
		return new FixtureOptionsBuilder();
	}

	public static final class FixtureOptionsBuilder {
		private static final Map<Class<?>, AnnotatedArbitraryGenerator<?>> DEFAULT_TYPE_ARBITRARY_SPECS;
		private static final Set<String> DEFAULT_EXCEPT_GENERATE_PACKAGE;
		private static final Set<String> DEFAULT_NONNULL_ANNOTATIONS;

		static {
			Map<Class<?>, AnnotatedArbitraryGenerator<?>> map = new HashMap<>();
			map.put(String.class, StringAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Integer.class, IntegerAnnotatedArbitraryGenerator.INSTANCE);
			map.put(int.class, IntegerAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Float.class, FloatAnnotatedArbitraryGenerator.INSTANCE);
			map.put(float.class, FloatAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Long.class, LongAnnotatedArbitraryGenerator.INSTANCE);
			map.put(long.class, LongAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Short.class, ShortAnnotatedArbitraryGenerator.INSTANCE);
			map.put(short.class, ShortAnnotatedArbitraryGenerator.INSTANCE);
			map.put(BigInteger.class, BigIntegerAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Boolean.class, BooleanAnnotatedArbitraryGenerator.INSTANCE);
			map.put(boolean.class, BooleanAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Double.class, DoubleAnnotatedArbitraryGenerator.INSTANCE);
			map.put(double.class, DoubleAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Byte.class, ByteAnnotatedArbitraryGenerator.INSTANCE);
			map.put(byte.class, ByteAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Character.class, CharacterAnnotatedArbitraryGenerator.INSTANCE);
			map.put(char.class, CharacterAnnotatedArbitraryGenerator.INSTANCE);
			map.put(UUID.class, UuidAnnotatedArbitraryGenerator.INSTANCE);
			map.put(OptionalInt.class, OptionalIntAnnotatedArbitraryGenerator.INSTANCE);
			map.put(OptionalLong.class, OptionalLongAnnotatedArbitraryGenerator.INSTANCE);
			map.put(OptionalDouble.class, OptionalDoubleAnnotatedArbitraryGenerator.INSTANCE);
			map.put(BigDecimal.class, BigDecimalAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Instant.class, InstantAnnotatedArbitraryGenerator.INSTANCE);
			map.put(IntStream.class, IntStreamAnnotatedArbitraryGenerator.INSTANCE);
			map.put(LongStream.class, LongStreamAnnotatedArbitraryGenerator.INSTANCE);
			map.put(DoubleStream.class, DoubleStreamAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Date.class, DateAnnotatedArbitraryGenerator.INSTANCE);
			map.put(LocalDate.class, LocalDateAnnotatedArbitraryGenerator.INSTANCE);
			map.put(LocalDateTime.class, LocalDateTimeAnnotatedArbitraryGenerator.INSTANCE);
			map.put(LocalTime.class, LocalTimeAnnotatedArbitraryGenerator.INSTANCE);
			map.put(ZonedDateTime.class, ZonedDateTimeAnnotatedArbitraryGenerator.INSTANCE);
			map.put(Calendar.class, CalendarAnnotatedArbitraryGenerator.INSTANCE);

			DEFAULT_TYPE_ARBITRARY_SPECS = Collections.unmodifiableMap(map);

			Set<String> defaultExceptGeneratePackages = new HashSet<>();
			defaultExceptGeneratePackages.add("java.lang");
			defaultExceptGeneratePackages.add("java.net");
			defaultExceptGeneratePackages.add("java.lang.reflect");
			defaultExceptGeneratePackages.add("jdk.internal.reflect");
			defaultExceptGeneratePackages.add("sun.reflect");
			defaultExceptGeneratePackages.add("com.naver.denma.domain.entity.AggregateMetaModel");
			DEFAULT_EXCEPT_GENERATE_PACKAGE = Collections.unmodifiableSet(defaultExceptGeneratePackages);

			List<String> nonNullAnnotations = Arrays.asList(
				"androidx.annotation.NonNull",
				"android.support.annotation.NonNull",
				"com.sun.istack.internal.NotNull",
				"edu.umd.cs.findbugs.annotations.NonNull",
				"javax.annotation.Nonnull",
				"javax.validation.constraints.NotNull",
				"lombok.NonNull",
				"org.checkerframework.checker.nullness.qual.NonNull",
				"org.eclipse.jdt.annotation.NonNull",
				"org.eclipse.jgit.annotations.NonNull",
				"org.jetbrains.annotations.NotNull",
				"org.jmlspecs.annotation.NonNull",
				"org.netbeans.api.annotations.common.NonNull",
				"org.springframework.lang.NonNull");

			DEFAULT_NONNULL_ANNOTATIONS = Collections.unmodifiableSet(new HashSet<>(nonNullAnnotations));
		}

		private final Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap = new HashMap<>(
			DEFAULT_TYPE_ARBITRARY_SPECS);
		private final Map<Class<?>, InterfaceSupplier<?>> interfaceSupplierMap = new HashMap<>();
		private final Map<Class<?>, Function<FixtureMonkey, ArbitraryBuilder<?>>> arbitraryBuildingSupplierMap =
			new HashMap<>();
		private final Map<Class<?>, ContainerArbitraryNodeGenerator> containerArbitraryNodeGeneratorMap =
			new HashMap<>();
		private Set<String> exceptGeneratePackages = new HashSet<>(DEFAULT_EXCEPT_GENERATE_PACKAGE);
		private Set<Class<?>> exceptGenerateClasses = new HashSet<>();
		private final Set<String> nonNullAnnotationNames = new HashSet<>(DEFAULT_NONNULL_ANNOTATIONS);
		private NullableArbitraryEvaluator nullableArbitraryEvaluator = new NullableArbitraryEvaluator() {
		};
		private InterfaceSupplier<?> defaultInterfaceSupplier = (type) -> null;
		private double nullInject = 0.2;
		private boolean nullableContainer = false;
		private boolean defaultNotNull = false;

		public FixtureOptionsBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
			this.exceptGeneratePackages.add(exceptGeneratePackage);
			return this;
		}

		public <T> FixtureOptionsBuilder addExceptGenerateClass(Class<T> clazz) {
			this.exceptGenerateClasses.add(clazz);
			return this;
		}

		public FixtureOptionsBuilder exceptGeneratePackages(Set<String> exceptGeneratePackages) {
			this.exceptGeneratePackages = exceptGeneratePackages;
			return this;
		}

		public FixtureOptionsBuilder exceptGenerateClasses(Set<Class<?>> exceptGenerateClasses) {
			this.exceptGenerateClasses = exceptGenerateClasses;
			return this;
		}

		public FixtureOptionsBuilder addInterfaceSupplier(Class<?> clazz, InterfaceSupplier<?> interfaceSupplier) {
			this.interfaceSupplierMap.put(clazz, interfaceSupplier);
			return this;
		}

		public FixtureOptionsBuilder defaultInterfaceSupplier(InterfaceSupplier<?> interfaceSupplier) {
			this.defaultInterfaceSupplier = interfaceSupplier;
			return this;
		}

		public FixtureOptionsBuilder nullableArbitraryEvaluator(NullableArbitraryEvaluator nullableArbitraryEvaluator) {
			this.nullableArbitraryEvaluator = nullableArbitraryEvaluator;
			return this;
		}

		public FixtureOptionsBuilder nullInject(double nullInject) {
			this.nullInject = nullInject;
			return this;
		}

		public FixtureOptionsBuilder nullableContainer(boolean nullableContainer) {
			this.nullableContainer = nullableContainer;
			return this;
		}

		public FixtureOptionsBuilder addAnnotatedArbitraryGenerator(
			Class<?> clazz, AnnotatedArbitraryGenerator<?> generator
		) {
			this.annotatedArbitraryMap.put(clazz, generator);
			return this;
		}

		public FixtureOptionsBuilder addNonNullAnnotationName(String annotationName) {
			this.nonNullAnnotationNames.add(annotationName);
			return this;
		}

		public FixtureOptionsBuilder defaultNotNull(boolean defaultNotNull) {
			this.defaultNotNull = defaultNotNull;
			return this;
		}

		public FixtureOptionsBuilder register(
			Class<?> clazz,
			Function<FixtureMonkey, ArbitraryBuilder<?>> arbitraryBuildingSupplier
		) {
			if (this.arbitraryBuildingSupplierMap.containsKey(clazz)) {
				throw new IllegalArgumentException("can not register same classes twice. " + clazz.getName());
			}
			this.arbitraryBuildingSupplierMap.put(clazz, arbitraryBuildingSupplier);
			return this;
		}

		public FixtureOptionsBuilder registerGroup(Class<?> arbitraryBuilderGroup) {
			Method[] methods = arbitraryBuilderGroup.getMethods();
			for (Method method : methods) {
				int paramCount = method.getParameterCount();
				Class<?> returnType = method.getReturnType();
				if (paramCount != 1 || returnType != ArbitraryBuilder.class) {
					continue;
				}
				try {
					Class<?> actualType = getReturnGenericType(method);
					Object noArgsInstance = arbitraryBuilderGroup.getDeclaredConstructor().newInstance();
					Function<FixtureMonkey, ArbitraryBuilder<?>> registerArbitraryBuilder = (fixtureMonkey) -> {
						try {
							return (ArbitraryBuilder<?>)method.invoke(noArgsInstance, fixtureMonkey);
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					};
					this.register(actualType, registerArbitraryBuilder);
				} catch (InvocationTargetException
					| InstantiationException
					| IllegalAccessException
					| NoSuchMethodException e) {
					// ignored
				}
			}
			return this;
		}

		public FixtureOptionsBuilder putContainerArbitraryNodeGenerator(
			Class<?> clazz,
			ContainerArbitraryNodeGenerator containerArbitraryNodeGenerator
		) {
			containerArbitraryNodeGeneratorMap.put(clazz, containerArbitraryNodeGenerator);
			return this;
		}

		private Class<?> getReturnGenericType(Method method) {
			AnnotatedType annotatedReturnType = method.getAnnotatedReturnType();
			AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)annotatedReturnType;
			AnnotatedType annotatedActualTypeArgument = parameterizedType.getAnnotatedActualTypeArguments()[0];

			if (annotatedActualTypeArgument instanceof AnnotatedParameterizedType) {
				ParameterizedType parameterType = (ParameterizedType)annotatedActualTypeArgument.getType();
				return (Class<?>)parameterType.getRawType();
			}
			return (Class<?>)annotatedActualTypeArgument.getType();
		}

		public ArbitraryOption build() {
			return new ArbitraryOption(
				Collections.unmodifiableMap(annotatedArbitraryMap),
				Collections.unmodifiableMap(interfaceSupplierMap),
				Collections.unmodifiableMap(arbitraryBuildingSupplierMap),
				Collections.unmodifiableMap(containerArbitraryNodeGeneratorMap),
				Collections.unmodifiableSet(exceptGeneratePackages),
				Collections.unmodifiableSet(exceptGenerateClasses),
				Collections.unmodifiableSet(nonNullAnnotationNames),
				defaultInterfaceSupplier,
				nullableArbitraryEvaluator,
				nullInject,
				nullableContainer,
				defaultNotNull
			);
		}
	}
}
