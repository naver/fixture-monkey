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
import static org.assertj.core.api.BDDAssertions.thenNoException;

import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.PreApproval;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimplePayMethod;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StaticFieldObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringAndInt;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringPair;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringValue;
import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;

class ValueProjectionMiscTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sampleNotGeneratingStaticField() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(StaticFieldObject.class));
	}

	@Property
	void sampleWithMonkeyStringArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new JqwikPlugin().javaTypeArbitraryGenerator(
					new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return new MonkeyStringArbitrary();
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isNotNull();
	}

	@Property
	void filterWithMonkeyStringArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new JqwikPlugin().javaTypeArbitraryGenerator(
					new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return new MonkeyStringArbitrary().filterCharacter(Character::isUpperCase);
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isUpperCase();
	}

	@Property
	void filterIsoControlCharacterWithMonkeyStringArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().plugin(new JavaNodeTreeAdapterPlugin()).build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).matches(value -> {
			for (char c : value.toCharArray()) {
				if (Character.isISOControl(c)) {
					return false;
				}
			}
			return true;
		});
	}

	@Property
	void sampleConstructorBasedTypeWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		SimplePayMethod actual = sut.giveMeOne(SimplePayMethod.class);

		// then
		then(actual).isNotNull();
		then(actual.getFirstPayMethod()).isNotNull();
		then(actual.getSecondPayMethod()).isNotNull();
		then(actual.getFirstPayMethod().getPayMethodType()).isNotNull();
	}

	@Property
	void sampleNullableConstructorParamWithDefaultNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		PreApproval actual = sut.giveMeOne(PreApproval.class);

		// then
		then(actual).isNotNull();
		if (actual.getPayMethod() != null) {
			then(actual.getPayMethod().getFirstPayMethod()).isNotNull();
			then(actual.getPayMethod().getFirstPayMethod().getPayMethodType()).isNotNull();
		}
	}

	@Property
	void multipleFiltersWithMonkeyStringArbitrary() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(
				new JqwikPlugin().javaTypeArbitraryGenerator(
					new JavaTypeArbitraryGenerator() {
						@Override
						public MonkeyStringArbitrary monkeyStrings() {
							return new MonkeyStringArbitrary()
								.filterCharacter(c -> !Character.isISOControl(c))
								.filterCharacter(Character::isUpperCase);
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).matches(value -> {
			for (char c : value.toCharArray()) {
				if (Character.isISOControl(c) || !Character.isUpperCase(c)) {
					return false;
				}
			}
			return true;
		});
	}

	@Property
	void registerLazyShouldPreserveNullFieldsAfterDecompose() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringAndInt.class, fixture -> fixture.giveMeBuilder(StringAndInt.class)
				.setLazy("$", () -> {
					StringAndInt obj = new StringAndInt();
					StringValue sv = new StringValue();
					sv.setValue("fixed");
					obj.setValue1(sv);
					return obj;
				})
			)
			.build();

		for (int i = 0; i < 100; i++) {
			// when
			StringAndInt actual = sut.giveMeOne(StringAndInt.class);

			// then
			then(actual.getValue1()).isNotNull();
			then(actual.getValue1().getValue()).isEqualTo("fixed");
			then(actual.getValue2()).isNull();
		}
	}

	@Property
	void setRootWithNullFieldShouldPreserveNull() {
		// given
		StringAndInt value = new StringAndInt();
		StringValue sv = new StringValue();
		sv.setValue("hello");
		value.setValue1(sv);

		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.set("$", value)
			.sample();

		// then
		then(actual.getValue1()).isNotNull();
		then(actual.getValue1().getValue()).isEqualTo("hello");
		then(actual.getValue2()).isNull();
	}

	@Property
	void setRootWithAllNullFieldsShouldPreserveNulls() {
		// given
		StringAndInt value = new StringAndInt();

		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.set("$", value)
			.sample();

		// then
		then(actual.getValue1()).isNull();
		then(actual.getValue2()).isNull();
	}

	@Property
	void setRootWithNullFieldAndChildOverrideShouldOverrideOnly() {
		// given
		StringAndInt value = new StringAndInt();

		// when
		StringAndInt actual = SUT.giveMeBuilder(StringAndInt.class)
			.set("$", value)
			.set("value2.value", 42)
			.sample();

		// then
		then(actual.getValue1()).isNull();
		then(actual.getValue2()).isNotNull();
		then(actual.getValue2().getValue()).isEqualTo(42);
	}

	@Property
	void registerThenApplyWithFieldLevelSetLazyShouldNotBeBlockedByRecursionGuard() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringPair.class, fixture -> fixture.giveMeBuilder(StringPair.class)
				.thenApply((it, builder) -> builder.set("value2", it.getValue1() + "_suffix"))
				.setLazy("value1", () -> "fixed")
			)
			.build();

		for (int i = 0; i < 100; i++) {
			// when
			StringPair actual = sut.giveMeOne(StringPair.class);

			// then
			then(actual.getValue1()).isEqualTo("fixed");
			then(actual.getValue2()).isEqualTo("fixed_suffix");
		}
	}

	@Property
	void registerNestedTypeBothWithThenApplyAndFieldSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringAndInt.class, fixture -> fixture.giveMeBuilder(StringAndInt.class)
				.thenApply((it, builder) -> {
					if (it.getValue1() != null) {
						builder.set("value2.value", it.getValue1().getValue().length());
					}
				})
				.setLazy("value1.value", () -> "hello")
			)
			.build();

		for (int i = 0; i < 100; i++) {
			// when
			StringAndInt actual = sut.giveMeOne(StringAndInt.class);

			// then
			then(actual.getValue1()).isNotNull();
			then(actual.getValue1().getValue()).isEqualTo("hello");
			then(actual.getValue2()).isNotNull();
			then(actual.getValue2().getValue()).isEqualTo(5);
		}
	}

	@Property
	void registerFieldSetLazyOnlyNoThenApplyShouldWork() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringPair.class, fixture -> fixture.giveMeBuilder(StringPair.class)
				.setLazy("value1", () -> "lazy1")
				.setLazy("value2", () -> "lazy2")
			)
			.build();

		for (int i = 0; i < 100; i++) {
			// when
			StringPair actual = sut.giveMeOne(StringPair.class);

			// then
			then(actual.getValue1()).isEqualTo("lazy1");
			then(actual.getValue2()).isEqualTo("lazy2");
		}
	}

	@Property
	void registerTwoTypesWithCrossReferenceThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(StringValue.class, fixture -> fixture.giveMeBuilder(StringValue.class)
				.setLazy("value", () -> "sv_fixed")
			)
			.register(StringAndInt.class, fixture -> fixture.giveMeBuilder(StringAndInt.class)
				.thenApply((it, builder) -> {
					if (it.getValue1() != null) {
						builder.set("value2.value", it.getValue1().getValue().length());
					}
				})
			)
			.build();

		for (int i = 0; i < 100; i++) {
			// when
			StringAndInt actual = sut.giveMeOne(StringAndInt.class);

			// then
			then(actual.getValue1()).isNotNull();
			then(actual.getValue1().getValue()).isEqualTo("sv_fixed");
			then(actual.getValue2()).isNotNull();
			then(actual.getValue2().getValue()).isEqualTo(8);
		}
	}

	@Property
	void setNestedObjectWithNullFieldShouldPreserveNull() {
		// given
		SimpleObject value = new SimpleObject();
		value.setStr("fixed");

		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("$", value)
			.sample();

		// then
		then(actual.getStr()).isEqualTo("fixed");
		then(actual.getInteger()).isNull();
		then(actual.getInstant()).isNull();
	}

}
