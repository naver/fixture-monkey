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

import java.util.HashSet;

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
public class LabMonkeyBuilder {
	private final GenerateOptionsBuilder generateOptionsBuilder = GenerateOptions.builder();
	private final ManipulateOptionsBuilder manipulateOptionsBuilder = ManipulateOptions.builder();
	private ArbitraryValidator arbitraryValidator = new DefaultArbitraryValidator();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private NullInjectGenerator defaultNullInjectGenerator = null;
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;

	public LabMonkeyBuilder manipulatorOptimizer(ManipulatorOptimizer manipulatorOptimizer) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		return this;
	}

	public LabMonkeyBuilder monkeyExpressionFactory(MonkeyExpressionFactory monkeyExpressionFactory) {
		manipulateOptionsBuilder.monkeyExpressionFactory(monkeyExpressionFactory);
		return this;
	}

	public LabMonkeyBuilder defaultArbitraryPropertyGenerator(ArbitraryPropertyGenerator arbitraryPropertyGenerator) {
		generateOptionsBuilder.defaultArbitraryPropertyGenerator(arbitraryPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushAssignableTypeArbitraryPropertyGenerator(
		Class<?> type,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(type, arbitraryPropertyGenerator);
		return this;
	}

	public LabMonkeyBuilder pushExactTypeArbitraryPropertyGenerator(
		Class<?> type,
		ArbitraryPropertyGenerator arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, arbitraryPropertyGenerator)
		);
		return this;
	}

	public LabMonkeyBuilder pushArbitraryPropertyGenerator(
		MatcherOperator<ArbitraryPropertyGenerator> arbitraryPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(arbitraryPropertyGenerator);
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

	public LabMonkeyBuilder addExceptGenerateClass(Class<?> type) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(type, NullArbitraryPropertyGenerator.INSTANCE);
		return this;
	}

	public LabMonkeyBuilder addExceptGenerateClassses(Class<?>... types) {
		for (Class<?> type : types) {
			addExceptGenerateClass(type);
		}
		return this;
	}

	public LabMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
		generateOptionsBuilder.insertFirstArbitraryPropertyGenerator(
			new MatcherOperator<>(
				property -> Types.getActualType(property.getType())
					.getPackage()
					.getName()
					.startsWith(exceptGeneratePackage),
				NullArbitraryPropertyGenerator.INSTANCE
			)
		);
		return this;
	}

	public LabMonkeyBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public LabMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public LabMonkeyBuilder addExceptGeneratePackages(String... exceptGeneratePackages) {
		for (String exceptGeneratePackage : exceptGeneratePackages) {
			addExceptGeneratePackage(exceptGeneratePackage);
		}
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
					new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
					new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
				)
			);
		}

		GenerateOptions generateOptions = generateOptionsBuilder.build();
		ManipulateOptions manipulateOptions = manipulateOptionsBuilder.build();
		ArbitraryTraverser traverser = new ArbitraryTraverser(generateOptions);

		return new LabMonkey(
			generateOptions,
			manipulateOptions,
			traverser,
			manipulatorOptimizer,
			this.arbitraryValidator
		);
	}

	public LabMonkeyBuilder useExpressionStrictMode() {
		this.manipulateOptionsBuilder.expressionStrictMode(true);
		return this;
	}
}
