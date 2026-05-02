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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.BuilderInteger;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ConstructorOnlyInteger;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.CustomBuildMethodInteger;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.CustomBuilderMethodInteger;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.FactoryMethodInteger;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class ValueProjectionInstantiatorTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void generateWithBuilderArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(BuilderInteger.class, BuilderArbitraryIntrospector.INSTANCE)
			.build();

		// when
		BuilderInteger actual = sut.giveMeOne(BuilderInteger.class);

		// then
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void generateWithBuilderArbitraryIntrospectorDefaultBuilderMethod() {
		// given
		BuilderArbitraryIntrospector builderArbitraryIntrospector = new BuilderArbitraryIntrospector();
		builderArbitraryIntrospector.setDefaultBuilderMethodName("customBuilder");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(CustomBuilderMethodInteger.class, builderArbitraryIntrospector)
			.build();

		// when
		CustomBuilderMethodInteger actual = sut.giveMeOne(CustomBuilderMethodInteger.class);

		// then
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void generateWithBuilderArbitraryIntrospectorDefaultBuildMethod() {
		// given
		BuilderArbitraryIntrospector builderArbitraryIntrospector = new BuilderArbitraryIntrospector();
		builderArbitraryIntrospector.setDefaultBuildMethodName("customBuild");

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(CustomBuildMethodInteger.class, builderArbitraryIntrospector)
			.build();

		// when
		CustomBuildMethodInteger actual = sut.giveMeOne(CustomBuildMethodInteger.class);

		// then
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void generateWithConstructorPropertiesArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(
				ConstructorOnlyInteger.class,
				ConstructorPropertiesArbitraryIntrospector.INSTANCE
			)
			.build();

		// when
		ConstructorOnlyInteger actual = sut.giveMeOne(ConstructorOnlyInteger.class);

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void generateWithFactoryMethodArbitraryIntrospector() throws NoSuchMethodException {
		// given
		Method factoryMethod = FactoryMethodInteger.class.getDeclaredMethod("of", int.class);
		factoryMethod.setAccessible(true);

		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(
				FactoryMethodInteger.class,
				new FactoryMethodArbitraryIntrospector(
					new FactoryMethodArbitraryIntrospector.FactoryMethodWithParameterNames(
						factoryMethod,
						Arrays.asList("value")
					)
				)
			)
			.build();

		// when
		FactoryMethodInteger actual = sut.giveMeOne(FactoryMethodInteger.class);

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
}
