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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.domains.Domain;

import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.arbitrary.ContainerArbitraryNodeGenerator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ConstructorPropertiesArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.NullArbitraryGenerator;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.BeanInnerBuilder;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.BeanInteger;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.BuilderInteger;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.ConstructorPropertiesInteger;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.ConstructorPropertiesTwice;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.ConstructorPropertiesWithNoMatchingField;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.ConstructorPropertiesZero;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.CustomTripleGenerics;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.CustomTripleGenericsWrapper;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.FieldReflectionBuilder;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.FieldReflectionInteger;
import com.navercorp.fixturemonkey.test.ArbitraryGeneratorTestSpecs.NullInt;

class ArbitraryGeneratorTest {
	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderInteger actual = sut.giveMeOne(BuilderInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(ArbitraryGeneratorTestSpecs.class)
	void giveMeWhenPutBuilderArbitraryGenerator(@ForAll BuilderInteger actual) {
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsFieldReflectionArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionInteger actual = sut.giveMeBuilder(FieldReflectionInteger.class).sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(ArbitraryGeneratorTestSpecs.class)
	void giveMeWhenPutFieldReflectionArbitraryGenerator(@ForAll FieldReflectionInteger actual) {
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsNullArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(NullArbitraryGenerator.INSTANCE)
			.build();

		// when
		NullInt actual = sut.giveMeOne(NullInt.class);

		then(actual).isNull();
	}

	@Property
	@Domain(ArbitraryGeneratorTestSpecs.class)
	void giveMeWhenPutNullArbitraryGenerator(@ForAll NullInt actual) {
		then(actual).isNull();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBeanArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanInteger actual = sut.giveMeOne(BeanInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(ArbitraryGeneratorTestSpecs.class)
	void giveMeWhenPutBeanArbitraryGenerator(@ForAll BeanInteger actual) {
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesInteger actual = sut.giveMeOne(ConstructorPropertiesInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	@Domain(ArbitraryGeneratorTestSpecs.class)
	void giveMeWhenPutConstructorPropertiesArbitraryGenerator(@ForAll ConstructorPropertiesInteger actual) {
		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderInteger actual = sut.giveMeBuilder(BuilderInteger.class)
			.customize(BuilderInteger.class, new ArbitraryCustomizer<BuilderInteger>() {
				@Override
				public void customizeFields(Class<BuilderInteger> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public BuilderInteger customizeFixture(@Nullable BuilderInteger fixture) {
					return fixture;
				}
			})
			.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void generatorMapBeanGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putGenerator(BuilderInteger.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanInnerBuilder actual = sut.giveMeOne(BeanInnerBuilder.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderInteger.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionBuilder actual = sut.giveMeOne(FieldReflectionBuilder.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderInteger.class, BuilderArbitraryGenerator.INSTANCE)
			.addCustomizer(BuilderInteger.class, new ArbitraryCustomizer<BuilderInteger>() {
				@Override
				public void customizeFields(Class<BuilderInteger> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderInteger customizeFixture(BuilderInteger object) {
					return object;
				}
			})
			.build();

		// when
		FieldReflectionBuilder actual = sut.giveMeBuilder(FieldReflectionBuilder.class)
			.setNotNull("value")
			.sample();

		then(actual.getValue().getValue()).isEqualTo(-1);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithZeroConstructorProperties() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		thenThrownBy(() -> sut.giveMeOne(ConstructorPropertiesZero.class))
			.hasMessageContaining("doesn't have constructor");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithTwoConstructorProperties() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		thenThrownBy(() -> sut.giveMeOne(ConstructorPropertiesTwice.class))
			.hasMessageContaining("has more then one constructor");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithNoMatchingFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		thenThrownBy(() -> sut.giveMeOne(ConstructorPropertiesWithNoMatchingField.class))
			.hasMessageContaining("No field for the corresponding constructor argument");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithNoMatchingFieldsUsingCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesWithNoMatchingField actual = sut.giveMeBuilder(
				ConstructorPropertiesWithNoMatchingField.class)
			.customize(ConstructorPropertiesWithNoMatchingField.class,
				new ArbitraryCustomizer<ConstructorPropertiesWithNoMatchingField>() {
					@Override
					public void customizeFields(Class<ConstructorPropertiesWithNoMatchingField> type,
						FieldArbitraries fieldArbitraries) {
						fieldArbitraries.putArbitrary("stringValue", Arbitraries.just("test"));
					}

					@Nullable
					@Override
					public ConstructorPropertiesWithNoMatchingField customizeFixture(
						@Nullable ConstructorPropertiesWithNoMatchingField fixture) {
						return fixture;
					}
				})
			.sample();

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
		then(actual.getValue2()).isEqualTo("test");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesInteger actual =
			sut.giveMeBuilder(ConstructorPropertiesInteger.class)
				.customize(ConstructorPropertiesInteger.class,
					new ArbitraryCustomizer<ConstructorPropertiesInteger>() {
						@Override
						public void customizeFields(
							Class<ConstructorPropertiesInteger> type,
							FieldArbitraries fieldArbitraries
						) {
							fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
						}

						@Nullable
						@Override
						public ConstructorPropertiesInteger customizeFixture(
							@Nullable ConstructorPropertiesInteger fixture
						) {
							return fixture;
						}
					})
				.sample();

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void overwriteCustomize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(BuilderInteger.class, new ArbitraryCustomizer<BuilderInteger>() {
				@Override
				public void customizeFields(Class<BuilderInteger> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderInteger customizeFixture(BuilderInteger object) {
					return object;
				}
			})
			.addCustomizer(BuilderInteger.class, new ArbitraryCustomizer<BuilderInteger>() {
				@Override
				public void customizeFields(Class<BuilderInteger> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-2));
				}

				@Override
				public BuilderInteger customizeFixture(BuilderInteger object) {
					return object;
				}
			})
			.build();

		// when
		BuilderInteger actual = sut.giveMeBuilder(BuilderInteger.class)
			.generator(BuilderArbitraryGenerator.INSTANCE)
			.sample();

		then(actual.getValue()).isEqualTo(-2);
	}

	@Property
	void giveMeWithCustomAnnotatedArbitraryGenerator() {
		// given
		BeanInteger value = new BeanInteger();
		value.setValue(1);

		ArbitraryOption customOption = ArbitraryOption.builder()
			.addAnnotatedArbitraryGenerator(
				BeanInteger.class,
				annotationSource -> Arbitraries.just(value)
			)
			.build();
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.options(customOption)
			.build();

		// when
		BeanInteger actual = sut.giveMeOne(BeanInteger.class);

		then(actual.getValue()).isEqualTo(1);
	}

	@Property
	void customContainerArbitraryNode() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putContainerArbitraryNodeGenerator(
				CustomTripleGenerics.class,
				CustomTripleArbitraryNodeGenerator.INSTANCE
			)
			.defaultNotNull(true)
			.build();

		// when
		CustomTripleGenericsWrapper actual = sut.giveMeOne(CustomTripleGenericsWrapper.class);

		then(actual.getValue().getValue1()).isNotNull();
		then(actual.getValue().getValue2()).isNotNull();
		then(actual.getValue().getValue3()).isNotNull();
	}

	public static class CustomTripleArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
		public static final CustomTripleArbitraryNodeGenerator INSTANCE = new CustomTripleArbitraryNodeGenerator();

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> containerNode) {
			List<ArbitraryNode<?>> generatedArbitraryNodes = new ArrayList<>();
			ArbitraryType<T> type = containerNode.getType();
			ArbitraryType<?> firstChildType = type.getGenericArbitraryType(0);
			ArbitraryType<?> secondChildType = type.getGenericArbitraryType(1);
			ArbitraryType<?> thirdChildType = type.getGenericArbitraryType(2);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(firstChildType)
					.propertyName("value1")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(secondChildType)
					.propertyName("value2")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(thirdChildType)
					.propertyName("value3")
					.build()
			);
			return generatedArbitraryNodes;
		}

		@Deprecated
		@Override
		public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
			return this.generate(nowNode);
		}
	}
}
