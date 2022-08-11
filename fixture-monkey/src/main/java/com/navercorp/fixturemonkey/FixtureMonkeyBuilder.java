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
import java.util.HashSet;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.ManipulateOptionsBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.validator.DefaultArbitraryValidator;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class FixtureMonkeyBuilder {
	private final GenerateOptionsBuilder generateOptionsBuilder = GenerateOptions.builder();
	private final ManipulateOptionsBuilder manipulateOptionsBuilder = ManipulateOptions.builder();
	private ArbitraryValidator arbitraryValidator = new DefaultArbitraryValidator();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private NullInjectGenerator defaultNullInjectGenerator = null;
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;

	public FixtureMonkeyBuilder manipulatorOptimizer(ManipulatorOptimizer manipulatorOptimizer) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		return this;
	}

	public FixtureMonkeyBuilder monkeyExpressionFactory(MonkeyExpressionFactory monkeyExpressionFactory) {
		manipulateOptionsBuilder.monkeyExpressionFactory(monkeyExpressionFactory);
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryPropertyGenerator(ArbitraryPropertyGenerator arbitraryPropertyGenerator) {
		generateOptionsBuilder.defaultArbitraryPropertyGenerator(arbitraryPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeArbitraryPropertyGenerator(
		Class<?> type,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(type, arbitraryPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeArbitraryPropertyGenerator(
		Class<?> type,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, arbitraryPropertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryPropertyGenerator(
		MatcherOperator<ArbitraryPropertyGenerator> arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(arbitraryPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(type, propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(
			MatcherOperator.exactTypeMatchOperator(type, propertyNameResolver)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyNameResolver(PropertyNameResolver propertyNameResolver) {
		generateOptionsBuilder.defaultPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(
			MatcherOperator.exactTypeMatchOperator(type, nullInjectGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(type, nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushNullInjectGenerator(MatcherOperator<NullInjectGenerator> nullInjectGenerator) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultNullInjectGenerator(NullInjectGenerator nullInjectGenerator) {
		this.defaultNullInjectGenerator = nullInjectGenerator;
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerInfoGenerator(arbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryContainerMaxSize(int defaultArbitraryContainerMaxSize) {
		generateOptionsBuilder.defaultArbitraryContainerMaxSize(defaultArbitraryContainerMaxSize);
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryContainerInfo(ArbitraryContainerInfo defaultArbitraryContainerInfo) {
		generateOptionsBuilder.defaultArbitraryContainerInfo(defaultArbitraryContainerInfo);
		return this;
	}

	public FixtureMonkeyBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		generateOptionsBuilder.javaArbitraryResolver(javaArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeArbitraryResolver(JavaTimeArbitraryResolver javaTimeArbitraryResolver) {
		generateOptionsBuilder.javaTimeArbitraryResolver(javaTimeArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.arbitraryValidator = arbitraryValidator;
		return this;
	}

	public FixtureMonkeyBuilder pushExceptGenerateType(Matcher matcher) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(
			new MatcherOperator<>(
				matcher,
				NullArbitraryPropertyGenerator.INSTANCE
			)
		);
		return this;
	}

	public FixtureMonkeyBuilder addExceptGenerateClass(Class<?> type) {
		return pushExceptGenerateType(new AssignableTypeMatcher(type));
	}

	public FixtureMonkeyBuilder addExceptGenerateClassses(Class<?>... types) {
		for (Class<?> type : types) {
			addExceptGenerateClass(type);
		}
		return this;
	}

	public FixtureMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
		return pushExceptGenerateType(
			property -> Types.primitiveToWrapper(Types.getActualType(property.getType()))
				.getPackage()
				.getName()
				.startsWith(exceptGeneratePackage)
		);
	}

	public FixtureMonkeyBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public FixtureMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public FixtureMonkeyBuilder addExceptGeneratePackages(String... exceptGeneratePackages) {
		for (String exceptGeneratePackage : exceptGeneratePackages) {
			addExceptGeneratePackage(exceptGeneratePackage);
		}
		return this;
	}

	public FixtureMonkeyBuilder register(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public FixtureMonkeyBuilder registerExactType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(MatcherOperator.exactTypeMatchOperator(type, registeredArbitraryBuilder));
		return this;
	}

	public FixtureMonkeyBuilder registerAssinableType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public FixtureMonkeyBuilder register(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(registeredArbitraryBuilder);
		return this;
	}

	public FixtureMonkeyBuilder registerGroup(Class<?>... arbitraryBuilderGroups) {
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
					Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registerArbitraryBuilder = (fixtureMonkey) -> {
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

	public FixtureMonkey build() {
		if (defaultNullInjectGenerator != null) {
			generateOptionsBuilder.defaultNullInjectGenerator(defaultNullInjectGenerator);
		} else if (defaultNotNull || nullableContainer) {
			generateOptionsBuilder.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					DEFAULT_NULL_INJECT,
					nullableContainer,
					defaultNotNull,
					new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
					new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
				)
			);
		}

		GenerateOptions generateOptions = generateOptionsBuilder.build();
		ArbitraryTraverser traverser = new ArbitraryTraverser(generateOptions);

		return new FixtureMonkey(
			generateOptions,
			manipulateOptionsBuilder,
			traverser,
			manipulatorOptimizer,
			this.arbitraryValidator
		);
	}

	public FixtureMonkeyBuilder useExpressionStrictMode() {
		this.manipulateOptionsBuilder.expressionStrictMode(true);
		return this;
	}
}
