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

import java.util.Collections;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ConstructorOnlyObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MutableJavaTypeObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringIntComposite;

@PropertyDefaults(tries = 10)
class IntrospectorAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void fieldReflectionIntrospectorGeneratesAllFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		MutableJavaTypeObject actual = sut.giveMeOne(MutableJavaTypeObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getString()).isNotNull();
		then(actual.getWrapperInteger()).isNotNull();
	}

	@Property
	void beanIntrospectorGeneratesAllFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		MutableJavaTypeObject actual = sut.giveMeOne(MutableJavaTypeObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getString()).isNotNull();
		then(actual.getWrapperInteger()).isNotNull();
	}

	@Property
	void constructorIntrospectorGeneratesConstructorParams() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		ConstructorOnlyObject actual = sut.giveMeBuilder(ConstructorOnlyObject.class).sample();

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void typeSpecificIntrospectorOverridesDefault() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeArbitraryIntrospector(SimpleObject.class, BeanArbitraryIntrospector.INSTANCE)
			.build();

		// when
		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getStr()).isNotNull();
	}

	@Property
	void instantiateWithConstructorGeneratesParams() {
		// when
		ConstructorOnlyObject actual = SUT.giveMeBuilder(ConstructorOnlyObject.class)
			.instantiate(Instantiator.constructor().parameter(String.class).parameter(int.class))
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void nestedObjectWithDifferentIntrospectors() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		ComplexObject actual = sut.giveMeOne(ComplexObject.class);

		// then
		then(actual).isNotNull();
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

	@Property
	void fieldFilteredPropertyGeneratorWorks() {
		// when
		MutableJavaTypeObject actual = SUT.giveMeBuilder(MutableJavaTypeObject.class)
			.instantiate(Instantiator.constructor().field(it -> it.filter(f -> f.getName().equals("string"))))
			.set("string", "filtered")
			.sample();

		// then
		then(actual).isNotNull();
		then(actual.getString()).isEqualTo("filtered");
	}

	@Property
	void customPropertyGeneratorViaIntrospectorRestrictsFields() {
		// given
		ArbitraryIntrospector customIntrospector = new ArbitraryIntrospector() {
			@Override
			public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
				return FieldReflectionArbitraryIntrospector.INSTANCE.introspect(context);
			}

			@Override
			public PropertyGenerator getRequiredPropertyGenerator(
				com.navercorp.fixturemonkey.api.property.Property property
			) {
				return p -> {
					try {
						return Collections.singletonList(
							new com.navercorp.fixturemonkey.api.property.FieldProperty(
								StringIntComposite.class.getDeclaredField("value1")
							)
						);
					} catch (NoSuchFieldException e) {
						throw new RuntimeException(e);
					}
				};
			}
		};

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(customIntrospector)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		StringIntComposite actual = sut.giveMeOne(StringIntComposite.class);

		// then
		then(actual).isNotNull();
		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNull();
	}

	@Property
	void pushExactTypePropertyGeneratorRestrictsFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.pushExactTypePropertyGenerator(StringIntComposite.class, property -> {
				try {
					return Collections.singletonList(
						new com.navercorp.fixturemonkey.api.property.FieldProperty(
							StringIntComposite.class.getDeclaredField("value1")
						)
					);
				} catch (NoSuchFieldException e) {
					throw new RuntimeException(e);
				}
			})
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		StringIntComposite actual = sut.giveMeOne(StringIntComposite.class);

		// then
		then(actual).isNotNull();
		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNull();
	}
}
