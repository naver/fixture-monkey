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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NOTNULL_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULLABLE_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULL_INJECT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.PropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.DecomposableContainerValue;
import com.navercorp.fixturemonkey.resolver.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.ManipulateOptionsBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.MonkeyContext;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.validator.DefaultArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class LabMonkeyBuilder {
	private final GenerateOptionsBuilder generateOptionsBuilder = GenerateOptions.builder();
	private final ManipulateOptionsBuilder manipulateOptionsBuilder = ManipulateOptions.builder();
	private ArbitraryValidator arbitraryValidator = new DefaultArbitraryValidator();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private NullInjectGenerator defaultNullInjectGenerator = null;
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;
	private boolean nullableElement = false;
	private DecomposedContainerValueFactory defaultDecomposedContainerValueFactory = (obj) -> {
		throw new IllegalArgumentException(
			"given type is not supported container : " + obj.getClass().getTypeName()
		);
	};
	private final Map<Class<?>, DecomposedContainerValueFactory> decomposableContainerFactoryMap = new HashMap<>();

	public LabMonkeyBuilder pushAssignableTypePropertyGenerator(Class<?> type, PropertyGenerator propertyGenerator) {
		generateOptionsBuilder.insertFirstPropertyGenerator(type, propertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushExactTypePropertyGenerator(Class<?> type, PropertyGenerator propertyGenerator) {
		generateOptionsBuilder.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
		return this;
	}

	public LabMonkeyBuilder pushExactTypePropertyGenerator(MatcherOperator<PropertyGenerator> propertyGenerator) {
		generateOptionsBuilder.insertFirstPropertyGenerator(propertyGenerator);
		return this;
	}

	public LabMonkeyBuilder manipulatorOptimizer(ManipulatorOptimizer manipulatorOptimizer) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		return this;
	}

	public LabMonkeyBuilder monkeyExpressionFactory(MonkeyExpressionFactory monkeyExpressionFactory) {
		manipulateOptionsBuilder.monkeyExpressionFactory(monkeyExpressionFactory);
		return this;
	}

	public LabMonkeyBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.defaultObjectPropertyGenerator(objectPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(type, objectPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushExactTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, objectPropertyGenerator)
		);
		return this;
	}

	public LabMonkeyBuilder pushObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> arbitraryObjectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(arbitraryObjectPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(type, containerPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushExactTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, containerPropertyGenerator)
		);
		return this;
	}

	public LabMonkeyBuilder pushContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(containerPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(type, propertyNameResolver);
		return this;
	}

	public LabMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(
			MatcherOperator.exactTypeMatchOperator(type, propertyNameResolver)
		);
		return this;
	}

	public LabMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public LabMonkeyBuilder defaultPropertyNameResolver(PropertyNameResolver propertyNameResolver) {
		generateOptionsBuilder.defaultPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public LabMonkeyBuilder pushExactTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(
			MatcherOperator.exactTypeMatchOperator(type, nullInjectGenerator)
		);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(type, nullInjectGenerator);
		return this;
	}

	public LabMonkeyBuilder pushNullInjectGenerator(MatcherOperator<NullInjectGenerator> nullInjectGenerator) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(nullInjectGenerator);
		return this;
	}

	public LabMonkeyBuilder defaultNullInjectGenerator(NullInjectGenerator nullInjectGenerator) {
		this.defaultNullInjectGenerator = nullInjectGenerator;
		return this;
	}

	public LabMonkeyBuilder pushArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerInfoGenerator(arbitraryContainerInfoGenerator);
		return this;
	}

	public LabMonkeyBuilder defaultArbitraryContainerMaxSize(int defaultArbitraryContainerMaxSize) {
		generateOptionsBuilder.defaultArbitraryContainerMaxSize(defaultArbitraryContainerMaxSize);
		return this;
	}

	public LabMonkeyBuilder defaultArbitraryContainerInfo(ArbitraryContainerInfo defaultArbitraryContainerInfo) {
		generateOptionsBuilder.defaultArbitraryContainerInfo(defaultArbitraryContainerInfo);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(type, arbitraryIntrospector);
		return this;
	}

	public LabMonkeyBuilder pushExactTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(
			MatcherOperator.exactTypeMatchOperator(type, arbitraryIntrospector)
		);
		return this;
	}

	public LabMonkeyBuilder pushArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(arbitraryIntrospector);
		return this;
	}

	public LabMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector) {
		this.generateOptionsBuilder.objectIntrospector(it -> objectIntrospector);
		return this;
	}

	public LabMonkeyBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator);
		return this;
	}

	public LabMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		generateOptionsBuilder.javaArbitraryResolver(javaArbitraryResolver);
		return this;
	}

	public LabMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator);
		return this;
	}

	public LabMonkeyBuilder javaTimeArbitraryResolver(JavaTimeArbitraryResolver javaTimeArbitraryResolver) {
		generateOptionsBuilder.javaTimeArbitraryResolver(javaTimeArbitraryResolver);
		return this;
	}

	public LabMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.arbitraryValidator = arbitraryValidator;
		return this;
	}

	public LabMonkeyBuilder pushExceptGenerateType(Matcher matcher) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			new MatcherOperator<>(
				matcher,
				NullObjectPropertyGenerator.INSTANCE
			)
		);
		return this;
	}

	public LabMonkeyBuilder addExceptGenerateClass(Class<?> type) {
		return pushExceptGenerateType(new AssignableTypeMatcher(type));
	}

	public LabMonkeyBuilder addExceptGenerateClasses(Class<?>... types) {
		for (Class<?> type : types) {
			addExceptGenerateClass(type);
		}
		return this;
	}

	public LabMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
		return pushExceptGenerateType(
			property -> Types.primitiveToWrapper(Types.getActualType(property.getType()))
				.getPackage()
				.getName()
				.startsWith(exceptGeneratePackage)
		);
	}

	public LabMonkeyBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public LabMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public LabMonkeyBuilder nullableElement(boolean nullableElement) {
		this.nullableElement = nullableElement;
		return this;
	}

	public LabMonkeyBuilder addExceptGeneratePackages(String... exceptGeneratePackages) {
		for (String exceptGeneratePackage : exceptGeneratePackages) {
			addExceptGeneratePackage(exceptGeneratePackage);
		}
		return this;
	}

	public LabMonkeyBuilder register(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public LabMonkeyBuilder registerExactType(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(MatcherOperator.exactTypeMatchOperator(type, registeredArbitraryBuilder));
		return this;
	}

	public LabMonkeyBuilder registerAssignableType(
		Class<?> type,
		Function<LabMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public LabMonkeyBuilder register(
		MatcherOperator<Function<LabMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(registeredArbitraryBuilder);
		return this;
	}

	public LabMonkeyBuilder registerGroup(Class<?>... arbitraryBuilderGroups) {
		for (Class<?> arbitraryBuilderGroup : arbitraryBuilderGroups) {
			Method[] methods = arbitraryBuilderGroup.getMethods();
			for (Method method : methods) {
				int paramCount = method.getParameterCount();
				Class<?> returnType = method.getReturnType();
				if (paramCount != 1 || !ArbitraryBuilder.class.isAssignableFrom(returnType)) {
					continue;
				}
				try {
					Class<?> actualType = Types.getActualType(
						Types.getGenericsTypes(method.getAnnotatedReturnType()).get(0)
					);
					Object noArgsInstance = arbitraryBuilderGroup.getDeclaredConstructor().newInstance();
					Function<LabMonkey, ? extends ArbitraryBuilder<?>> registerArbitraryBuilder = (fixtureMonkey) -> {
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
		}
		return this;
	}

	public <T> LabMonkeyBuilder pushAssignableTypeArbitraryCustomizer(
		Class<T> type,
		FixtureCustomizer<? extends T> fixtureCustomizer
	) {
		generateOptionsBuilder.insertFirstArbitraryCustomizer(type, fixtureCustomizer);
		return this;
	}

	public <T> LabMonkeyBuilder pushExactTypeArbitraryCustomizer(
		Class<T> type,
		FixtureCustomizer<T> fixtureCustomizer
	) {
		generateOptionsBuilder.insertFirstArbitraryCustomizer(
			MatcherOperator.exactTypeMatchOperator(type, fixtureCustomizer)
		);
		return this;
	}

	@SuppressWarnings("rawtypes")
	public LabMonkeyBuilder pushArbitraryCustomizer(MatcherOperator<FixtureCustomizer> arbitraryCustomizer) {
		generateOptionsBuilder.insertFirstArbitraryCustomizer(arbitraryCustomizer);
		return this;
	}

	public LabMonkeyBuilder plugin(Plugin plugin) {
		generateOptionsBuilder.plugin(plugin);
		return this;
	}

	public LabMonkeyBuilder defaultDecomposedContainerValueFactory(
		DecomposedContainerValueFactory defaultDecomposedContainerValueFactory
	) {
		this.defaultDecomposedContainerValueFactory = defaultDecomposedContainerValueFactory;
		return this;
	}

	public LabMonkeyBuilder pushContainerIntrospector(ArbitraryIntrospector containerIntrospector) {
		this.generateOptionsBuilder.containerIntrospector(it ->
			new CompositeArbitraryIntrospector(
				Arrays.asList(
					containerIntrospector,
					it
				)
			)
		);
		return this;
	}

	public LabMonkeyBuilder addContainerType(
		Class<?> type,
		ContainerPropertyGenerator containerObjectPropertyGenerator,
		ArbitraryIntrospector containerArbitraryIntrospector,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.pushAssignableTypeContainerPropertyGenerator(type, containerObjectPropertyGenerator);
		this.pushContainerIntrospector(containerArbitraryIntrospector);
		decomposableContainerFactoryMap.put(type, decomposedContainerValueFactory);
		return this;
	}

	public LabMonkeyBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.generateOptionsBuilder.defaultPropertyGenerator(propertyGenerator);
		return this;
	}

	public LabMonkey build() {
		if (defaultNullInjectGenerator != null) {
			generateOptionsBuilder.defaultNullInjectGenerator(defaultNullInjectGenerator);
		} else if (defaultNotNull || nullableContainer) {
			generateOptionsBuilder.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					DEFAULT_NULL_INJECT,
					nullableContainer,
					defaultNotNull,
					nullableElement,
					new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
					new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
				)
			);
		}

		manipulateOptionsBuilder.additionalDecomposedContainerValueFactory(
			obj -> {
				Class<?> actualType = obj.getClass();
				for (
					Entry<Class<?>, DecomposedContainerValueFactory> entry :
					this.decomposableContainerFactoryMap.entrySet()
				) {
					Class<?> type = entry.getKey();
					DecomposableContainerValue decomposedValue = entry.getValue().from(obj);

					if (actualType.isAssignableFrom(type)) {
						return decomposedValue;
					}
				}
				return this.defaultDecomposedContainerValueFactory.from(obj);
			}
		);

		GenerateOptions generateOptions = generateOptionsBuilder.build();
		ArbitraryTraverser traverser = new ArbitraryTraverser(generateOptions);

		return new LabMonkey(
			generateOptions,
			manipulateOptionsBuilder,
			traverser,
			manipulatorOptimizer,
			this.arbitraryValidator,
			MonkeyContext.builder().build()
		);
	}

	public LabMonkeyBuilder useExpressionStrictMode() {
		this.manipulateOptionsBuilder.expressionStrictMode(true);
		return this;
	}
}
