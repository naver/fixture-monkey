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

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;
import net.jqwik.api.Property;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.SourceOfRandomness;

import lombok.Builder;
import lombok.Data;

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

public class ArbitraryGeneratorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.putGenerator(BuilderIntegerWrapperClass.class, BuilderArbitraryGenerator.INSTANCE)
		.putGenerator(FieldReflectionIntegerWrapperClass.class, FieldReflectionArbitraryGenerator.INSTANCE)
		.putGenerator(NullIntegerWrapperClass.class, NullArbitraryGenerator.INSTANCE)
		.putGenerator(BeanIntegerWrapperClass.class, BeanArbitraryGenerator.INSTANCE)
		.putGenerator(ConstructorPropertiesIntegerWrapperClass.class, ConstructorPropertiesArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeWhenPutBuilderArbitraryGenerator() {
		// when
		BuilderIntegerWrapperClass actual = this.sut.giveMeOne(BuilderIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderIntegerWrapperClass actual = sut.giveMeBuilder(BuilderIntegerWrapperClass.class).sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsFieldReflectionArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionIntegerWrapperClass actual =
			sut.giveMeBuilder(FieldReflectionIntegerWrapperClass.class).sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutFieldReflectionArbitraryGenerator() {
		// when
		FieldReflectionIntegerWrapperClass actual = this.sut.giveMeOne(FieldReflectionIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsNullArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(NullArbitraryGenerator.INSTANCE)
			.build();

		// when
		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenPutNullArbitraryGenerator() {
		// when
		NullIntegerWrapperClass actual = this.sut.giveMeOne(NullIntegerWrapperClass.class);

		then(actual).isNull();
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBeanArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanIntegerWrapperClass actual = sut.giveMeOne(BeanIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutBeanArbitraryGenerator() {
		// when
		BeanIntegerWrapperClass actual = this.sut.giveMeOne(BeanIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesIntegerWrapperClass actual = sut.giveMeOne(ConstructorPropertiesIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenPutConstructorPropertiesArbitraryGenerator() {
		// when
		ConstructorPropertiesIntegerWrapperClass actual =
			this.sut.giveMeOne(ConstructorPropertiesIntegerWrapperClass.class);

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsBuilderArbitraryGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BuilderIntegerWrapperClass actual = sut.giveMeBuilder(BuilderIntegerWrapperClass.class)
			.customize(BuilderIntegerWrapperClass.class, new ArbitraryCustomizer<BuilderIntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
				}

				@Nullable
				@Override
				public BuilderIntegerWrapperClass customizeFixture(@Nullable BuilderIntegerWrapperClass fixture) {
					return fixture;
				}
			})
			.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void generatorMapBeanGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putGenerator(BuilderIntegerWrapperClass.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		BeanInnerBuilderClass actual = sut.giveMeOne(BeanInnerBuilderClass.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderIntegerWrapperClass.class, BuilderArbitraryGenerator.INSTANCE)
			.build();

		// when
		FieldReflectionInnerBuilderClass actual = sut.giveMeOne(FieldReflectionInnerBuilderClass.class);

		then(actual).isNotNull();
	}

	@Property
	void generatorMapFieldReflectionGeneratorWithBuilderGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(FieldReflectionArbitraryGenerator.INSTANCE)
			.putGenerator(BuilderIntegerWrapperClass.class, BuilderArbitraryGenerator.INSTANCE)
			.addCustomizer(BuilderIntegerWrapperClass.class, new ArbitraryCustomizer<BuilderIntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderIntegerWrapperClass customizeFixture(BuilderIntegerWrapperClass object) {
					return object;
				}
			})
			.build();

		// when
		FieldReflectionInnerBuilderClass actual = sut.giveMeBuilder(FieldReflectionInnerBuilderClass.class)
			.setNotNull("value")
			.sample();

		then(actual.value.value).isEqualTo(-1);
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithZeroConstructorProperties() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		thenThrownBy(() -> sut.giveMe(ConstructorPropertiesZeroClass.class))
			.hasMessageContaining("doesn't have constructor");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithTwoConstructorProperties() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		thenThrownBy(() -> sut.giveMe(ConstructorPropertiesTwiceClass.class))
			.hasMessageContaining("has more then one constructor");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithNoMatchingFields() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		thenThrownBy(() -> sut.giveMeOne(ConstructorPropertiesWithNoMatchingFieldClass.class))
			.hasMessageContaining("No field for the corresponding constructor argument");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithNoMatchingFieldsUsingCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesWithNoMatchingFieldClass actual = sut.giveMeBuilder(
			ConstructorPropertiesWithNoMatchingFieldClass.class)
			.customize(ConstructorPropertiesWithNoMatchingFieldClass.class,
				new ArbitraryCustomizer<ConstructorPropertiesWithNoMatchingFieldClass>() {
					@Override
					public void customizeFields(Class<ConstructorPropertiesWithNoMatchingFieldClass> type,
						FieldArbitraries fieldArbitraries) {
						fieldArbitraries.putArbitrary("stringValue", Arbitraries.just("test"));
					}

					@Nullable
					@Override
					public ConstructorPropertiesWithNoMatchingFieldClass customizeFixture(
						@Nullable ConstructorPropertiesWithNoMatchingFieldClass fixture) {
						return fixture;
					}
				})
			.sample();

		then(actual.value).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
		then(actual.value2).isEqualTo("test");
	}

	@Property
	void giveMeWhenDefaultGeneratorIsConstructorPropertiesArbitraryGeneratorWithCustomizer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(ConstructorPropertiesArbitraryGenerator.INSTANCE)
			.build();

		// when
		ConstructorPropertiesIntegerWrapperClass actual =
			sut.giveMeBuilder(ConstructorPropertiesIntegerWrapperClass.class)
				.customize(ConstructorPropertiesIntegerWrapperClass.class,
					new ArbitraryCustomizer<ConstructorPropertiesIntegerWrapperClass>() {
						@Override
						public void customizeFields(Class<ConstructorPropertiesIntegerWrapperClass> type,
							FieldArbitraries fieldArbitraries) {
							fieldArbitraries.putArbitrary("value", Arbitraries.just(1));
						}

						@Nullable
						@Override
						public ConstructorPropertiesIntegerWrapperClass customizeFixture(
							@Nullable ConstructorPropertiesIntegerWrapperClass fixture) {
							return fixture;
						}
					})
				.sample();

		then(actual.value).isEqualTo(1);
	}

	@Property
	void overwriteCustomize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addCustomizer(BuilderIntegerWrapperClass.class, new ArbitraryCustomizer<BuilderIntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-1));
				}

				@Override
				public BuilderIntegerWrapperClass customizeFixture(BuilderIntegerWrapperClass object) {
					return object;
				}
			})
			.addCustomizer(BuilderIntegerWrapperClass.class, new ArbitraryCustomizer<BuilderIntegerWrapperClass>() {
				@Override
				public void customizeFields(Class<BuilderIntegerWrapperClass> type, FieldArbitraries fieldArbitraries) {
					fieldArbitraries.replaceArbitrary("value", Arbitraries.just(-2));
				}

				@Override
				public BuilderIntegerWrapperClass customizeFixture(BuilderIntegerWrapperClass object) {
					return object;
				}
			})
			.build();

		// when
		BuilderIntegerWrapperClass actual = sut.giveMeBuilder(BuilderIntegerWrapperClass.class)
			.generator(BuilderArbitraryGenerator.INSTANCE)
			.sample();

		then(actual.value).isEqualTo(-2);
	}

	@Example
	void test22() {
		Arbitraries.strings().ascii()
			.map(it -> it + "abc")
			.withoutEdgeCases()
			.map(it -> it + "abc")
			.sample();
	}

	@Property
	void giveMeWithCustomAnnotatedArbitraryGenerator() {
		// given
		IntegerWrapperClass value = new IntegerWrapperClass();
		value.setValue(1);

		ArbitraryOption customOption = ArbitraryOption.builder()
			.addAnnotatedArbitraryGenerator(
				IntegerWrapperClass.class,
				annotationSource -> Arbitraries.just(value)
			)
			.build();
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.options(customOption)
			.build();

		// when
		IntegerWrapperClass actual = sut.giveMeOne(IntegerWrapperClass.class);

		then(actual.value).isEqualTo(1);
	}

	@Property
	void customContainerArbitraryNode() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.putContainerArbitraryNodeGenerator(
				CustomTripleGenericClass.class,
				CustomTripleArbitraryNodeGenerator.INSTANCE
			)
			.defaultNotNull(true)
			.build();

		// when
		CustomTripleGenericWrapperClass actual = sut.giveMeOne(CustomTripleGenericWrapperClass.class);

		then(actual.value.value1).isNotNull();
		then(actual.value.value2).isNotNull();
		then(actual.value.value3).isNotNull();
	}

	@Data
	public static class CustomTripleGenericWrapperClass {
		CustomTripleGenericClass<String, Integer, Float> value;
	}

	@Data
	public static class CustomTripleGenericClass<T, U, V> {
		T value1;
		U value2;
		V value3;
	}

	@Builder
	public static class BuilderIntegerWrapperClass {
		int value;
	}

	public static class FieldReflectionIntegerWrapperClass {
		private int value;
	}

	public static class NullIntegerWrapperClass {
		int value;
	}

	@Data
	public static class BeanIntegerWrapperClass {
		private int value;
	}

	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesIntegerWrapperClass {
		private int value;

		@ConstructorProperties("value")
		public ConstructorPropertiesIntegerWrapperClass(int value) {
			this.value = value;
		}
	}

	@Data
	public static class BeanInnerBuilderClass {
		BuilderIntegerWrapperClass value;
	}

	public static class FieldReflectionInnerBuilderClass {
		BuilderIntegerWrapperClass value;
	}

	@Data
	public static class IntegerWrapperClass {
		int value;
	}

	public static class ConstructorPropertiesZeroClass {
		private int value;
	}

	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesTwiceClass {
		private int value;
		@SuppressWarnings("FieldCanBeLocal")
		private String stringValue;

		@ConstructorProperties("value")
		public ConstructorPropertiesTwiceClass(int value) {
			this.value = value;
		}

		@ConstructorProperties({"value", "stringValue"})
		public ConstructorPropertiesTwiceClass(int value, String stringValue) {
			this.value = value;
			this.stringValue = stringValue;
		}
	}

	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesWithNoMatchingFieldClass {
		private int value;
		private String value2;

		@ConstructorProperties({"value", "stringValue"})
		public ConstructorPropertiesWithNoMatchingFieldClass(int value, String stringValue) {
			this.value = value;
			this.value2 = stringValue;
		}
	}

	public static class CustomTripleArbitraryNodeGenerator implements ContainerArbitraryNodeGenerator {
		public static final CustomTripleArbitraryNodeGenerator INSTANCE = new CustomTripleArbitraryNodeGenerator();

		@SuppressWarnings("unchecked")
		@Override
		public <T> List<ArbitraryNode<?>> generate(ArbitraryNode<T> nowNode, FieldNameResolver fieldNameResolver) {
			List<ArbitraryNode<?>> generatedArbitraryNodes = new ArrayList<>();
			ArbitraryType<T> type = nowNode.getType();
			ArbitraryType<?> firstChildType = type.getGenericArbitraryType(0);
			ArbitraryType<?> secondChildType = type.getGenericArbitraryType(1);
			ArbitraryType<?> thirdChildType = type.getGenericArbitraryType(2);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(firstChildType)
					.fieldName("value1")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(secondChildType)
					.fieldName("value2")
					.build()
			);
			generatedArbitraryNodes.add(
				ArbitraryNode.builder()
					.type(thirdChildType)
					.fieldName("value3")
					.build()
			);
			return generatedArbitraryNodes;
		}
	}
}
