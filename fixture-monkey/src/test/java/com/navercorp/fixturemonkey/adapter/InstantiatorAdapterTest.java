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

import java.lang.reflect.Modifier;
import java.util.List;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ConstructorGenericObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ConstructorJavaTypeObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ConstructorTwoGenericObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MutableJavaTypeObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleContainerObject;

@PropertyDefaults(tries = 10)
class InstantiatorAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void instantiateConstructorField() {
		// when
		String actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(Instantiator.constructor().field())
			.sample()
			.getString();

		// then
		then(actual).isNotNull();
	}

	@Property
	void instantiateConstructorJavaBeansProperty() {
		// when
		String actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(Instantiator.constructor().javaBeansProperty())
			.sample()
			.getString();

		// then
		then(actual).isNotNull();
	}

	@Property
	void instantiateConstructorFieldFilter() {
		// when
		MutableJavaTypeObject actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(
				Instantiator.constructor().field(it -> it.filter(field -> !Modifier.isPrivate(field.getModifiers())))
			)
			.sample();

		// then
		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNull();
	}

	@Property
	void instantiateConstructorJavaBeansPropertyFilter() {
		// when
		MutableJavaTypeObject actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(
				Instantiator.constructor().javaBeansProperty(it ->
					it.filter(property -> !"string".equals(property.getName()))
				)
			)
			.sample();

		// then
		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNotNull();
	}

	@Property
	void instantiateWithFilteredFieldsUsingCustomGenerator() {
		// given

		// when
		MutableJavaTypeObject actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(Instantiator.constructor().field(it -> it.filter(field -> "string".equals(field.getName()))))
			.sample();

		// then
		then(actual.getString()).isNotNull();
		then(actual.getWrapperBoolean()).isNull();
		then(actual.getWrapperInteger()).isNull();
	}

	@Property
	void instantiateParametersInOrder() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(
				ConstructorJavaTypeObject.class,
				Instantiator.constructor()
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

		// then
		then(actual).isEqualTo("first");
	}

	@Property
	void instantiateNoArgsConstructor() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(ConstructorJavaTypeObject.class, Instantiator.constructor())
			.sample()
			.getString();

		// then
		then(actual).isEqualTo("second");
	}

	@Property
	void instantiateParameterNameHint() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(ConstructorJavaTypeObject.class, Instantiator.constructor().parameter(String.class, "str"))
			.set("str", "third")
			.sample()
			.getString();

		// then
		then(actual).isEqualTo("third");
	}

	@Property
	void instantiateConstructorContainer() {
		// when
		List<ConstructorJavaTypeObject> actual = SUT.giveMeBuilder(SimpleContainerObject.class)
			.instantiate(
				SimpleContainerObject.class,
				Instantiator.constructor().parameter(new TypeReference<List<ConstructorJavaTypeObject>>() {
				}, "list")
			)
			.instantiate(
				ConstructorJavaTypeObject.class,
				Instantiator.constructor()
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

		// then
		then(actual).hasSize(1);
	}

	@Property
	void instantiateGenericObjectByConstructor() {
		// when
		ConstructorGenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorGenericObject<String>>() {
				}
			)
			.instantiate(
				new TypeReference<ConstructorGenericObject<String>>() {
				},
				Instantiator.constructor().parameter(String.class)
			)
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void instantiateTwoGenericObjectByConstructor() {
		// when
		ConstructorTwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorTwoGenericObject<String, Integer>>() {
				}
			)
			.instantiate(
				new TypeReference<ConstructorTwoGenericObject<String, Integer>>() {
				},
				Instantiator.constructor().parameter(String.class).parameter(Integer.class)
			)
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getTValue()).isNotNull();
		then(actual.getUValue()).isNotNull();
	}

	@Property
	void instantiateByFactoryMethod() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(Instantiator.factoryMethod("from"))
			.sample()
			.getString();

		// then
		then(actual).isEqualTo("factory");
	}

	@Property
	void instantiateByFactoryMethodWithParameter() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(Instantiator.factoryMethod("from").parameter(String.class))
			.sample()
			.getString();

		// then
		then(actual).isEqualTo("factory");
	}

	@Property
	void instantiateFactoryMethodAndField() {
		// when
		Integer actual = SUT.giveMeBuilder(ConstructorJavaTypeObject.class)
			.instantiate(Instantiator.factoryMethod("from").parameter(String.class).field())
			.sample()
			.getWrapperInteger();

		// then
		then(actual).isNotNull();
	}

}
