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

package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs.SimpleContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs;

class InstantiatorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void instantiateParametersInOrder() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("first");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateNoArgsConstructor() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
			)
			.sample()
			.getString();

		then(actual).isEqualTo("second");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateParameterNameHint() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(String.class, "str")
			)
			.set("str", "third")
			.sample()
			.getString();

		then(actual).isEqualTo("third");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorContainer() {
		List<ConstructorSpecs.JavaTypeObject> actual = SUT.giveMeBuilder(SimpleContainerObject.class)
			.instantiate(
				SimpleContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<ConstructorSpecs.JavaTypeObject>>() {
					}, "list")
			)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.size("list", 1)
			.sample()
			.getList();

		then(actual).hasSize(1);
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorGenericContainer() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.ContainerObject.class)
			.instantiate(
				ConstructorSpecs.ContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<String>>() {
					})
					.parameter(new TypeReference<List<ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<java.util.Set<String>>() {
					})
					.parameter(new TypeReference<java.util.Set<ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Map<String, Integer>>() {
					})
					.parameter(new TypeReference<Map<String, ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Entry<String, Integer>>() {
					})
					.parameter(new TypeReference<Entry<String, ConstructorSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<java.util.Optional<String>>() {
					})
					.parameter(new TypeReference<OptionalInt>() {
					})
					.parameter(new TypeReference<OptionalLong>() {
					})
					.parameter(new TypeReference<OptionalDouble>() {
					})
			)
			.instantiate(
				ConstructorSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getArray()[0];

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateGenericObjectByConstructor() {
		ConstructorSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateTwoGenericObjectByConstructor() {
		ConstructorSpecs.TwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.TwoGenericObject<String, Integer>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.TwoGenericObject<String, Integer>>() {
				},
				constructor()
					.parameter(String.class)
					.parameter(Integer.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getTValue()).isNotNull();
		then(actual.getUValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateGenericObjectWithHintByConstructor() {
		ConstructorSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateByFactoryMethod() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateByFactoryMethodWithParameter() {
		String actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateFactoryMethodAndField() {
		Integer actual = SUT.giveMeBuilder(ConstructorSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
					.field()
			)
			.sample()
			.getWrapperInteger();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorField() {
		String actual = SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
			.instantiate(constructor().field())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorJavaBeansProperty() {
		String actual = SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
			.instantiate(constructor().javaBeansProperty())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorFieldFilter() {
		MutableSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.field(it -> it.filter(field -> !Modifier.isPrivate(field.getModifiers())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorJavaBeansPropertyFilter() {
		MutableSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.javaBeansProperty(it -> it.filter(property -> !"string".equals(property.getName())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNotNull();
	}
}
