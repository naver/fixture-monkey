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

import java.beans.ConstructorProperties;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.AbstractDomainContextBase;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.BuilderArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ConstructorPropertiesArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldReflectionArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.NullArbitraryGenerator;

class ArbitraryGeneratorTestSpecs extends AbstractDomainContextBase {
	public static final FixtureMonkey SUT = FixtureMonkey.builder()
		.putGenerator(BuilderInteger.class, BuilderArbitraryGenerator.INSTANCE)
		.putGenerator(FieldReflectionInteger.class, FieldReflectionArbitraryGenerator.INSTANCE)
		.putGenerator(NullInt.class, NullArbitraryGenerator.INSTANCE)
		.putGenerator(BeanInteger.class, BeanArbitraryGenerator.INSTANCE)
		.putGenerator(ConstructorPropertiesInteger.class, ConstructorPropertiesArbitraryGenerator.INSTANCE)
		.build();

	ArbitraryGeneratorTestSpecs() {
		registerArbitrary(BuilderInteger.class, builderInteger());
		registerArbitrary(FieldReflectionInteger.class, fieldReflectionInteger());
		registerArbitrary(BeanInteger.class, beanInteger());
		registerArbitrary(BeanInnerBuilder.class, beanInnerBuilder());
		registerArbitrary(NullInt.class, nullInt());
		registerArbitrary(ConstructorPropertiesInteger.class, constructorPropertiesInteger());
		registerArbitrary(FieldReflectionBuilder.class, fieldReflectionBuilder());
		registerArbitrary(ConstructorPropertiesZero.class, constructorPropertiesZero());
		registerArbitrary(ConstructorPropertiesTwice.class, constructorPropertiesTwice());
		registerArbitrary(ConstructorPropertiesWithNoMatchingField.class, constructorPropertiesWithNoMatchingField());
		registerArbitrary(CustomTripleGenericsWrapper.class, customTripleGenericsWrapper());
	}

	@Getter
	@Builder
	public static class BuilderInteger {
		private int value;
	}

	@Provide
	Arbitrary<BuilderInteger> builderInteger() {
		return SUT.giveMeArbitrary(BuilderInteger.class);
	}

	@Getter
	public static class FieldReflectionInteger {
		private int value;
	}

	@Provide
	Arbitrary<FieldReflectionInteger> fieldReflectionInteger() {
		return SUT.giveMeArbitrary(FieldReflectionInteger.class);
	}

	@Data
	public static class BeanInteger {
		private int value;
	}

	@Provide
	Arbitrary<BeanInteger> beanInteger() {
		return SUT.giveMeArbitrary(BeanInteger.class);
	}

	@Data
	public static class BeanInnerBuilder {
		private BuilderInteger value;
	}

	@Provide
	Arbitrary<BeanInnerBuilder> beanInnerBuilder() {
		return SUT.giveMeArbitrary(BeanInnerBuilder.class);
	}

	@Getter
	public static class NullInt {
		private int value;
	}

	@Provide
	Arbitrary<NullInt> nullInt() {
		return SUT.giveMeArbitrary(NullInt.class);
	}

	@Getter
	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesInteger {
		private int value;

		@ConstructorProperties("value")
		public ConstructorPropertiesInteger(int value) {
			this.value = value;
		}
	}

	@Provide
	Arbitrary<ConstructorPropertiesInteger> constructorPropertiesInteger() {
		return SUT.giveMeArbitrary(ConstructorPropertiesInteger.class);
	}

	@Getter
	public static class FieldReflectionBuilder {
		private BuilderInteger value;
	}

	@Provide
	Arbitrary<FieldReflectionBuilder> fieldReflectionBuilder() {
		return SUT.giveMeArbitrary(FieldReflectionBuilder.class);
	}

	@Getter
	public static class ConstructorPropertiesZero {
		private int value;
	}

	@Provide
	Arbitrary<ConstructorPropertiesZero> constructorPropertiesZero() {
		return SUT.giveMeArbitrary(ConstructorPropertiesZero.class);
	}

	@Getter
	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesTwice {
		private int value;
		@SuppressWarnings("FieldCanBeLocal")
		private String stringValue;

		@ConstructorProperties("value")
		public ConstructorPropertiesTwice(int value) {
			this.value = value;
		}

		@ConstructorProperties({"value", "stringValue"})
		public ConstructorPropertiesTwice(int value, String stringValue) {
			this.value = value;
			this.stringValue = stringValue;
		}
	}

	@Provide
	Arbitrary<ConstructorPropertiesTwice> constructorPropertiesTwice() {
		return SUT.giveMeArbitrary(ConstructorPropertiesTwice.class);
	}

	@Getter
	@SuppressWarnings("FieldMayBeFinal")
	public static class ConstructorPropertiesWithNoMatchingField {
		private int value;
		private String value2;

		@ConstructorProperties({"value", "stringValue"})
		public ConstructorPropertiesWithNoMatchingField(int value, String stringValue) {
			this.value = value;
			this.value2 = stringValue;
		}
	}

	@Provide
	Arbitrary<ConstructorPropertiesWithNoMatchingField> constructorPropertiesWithNoMatchingField() {
		return SUT.giveMeArbitrary(ConstructorPropertiesWithNoMatchingField.class);
	}

	@Data
	public static class CustomTripleGenericsWrapper {
		private CustomTripleGenerics<String, Integer, Float> value;
	}

	@Data
	public static class CustomTripleGenerics<T, U, V> {
		private T value1;
		private U value2;
		private V value3;
	}

	@Provide
	Arbitrary<CustomTripleGenericsWrapper> customTripleGenericsWrapper() {
		return SUT.giveMeArbitrary(CustomTripleGenericsWrapper.class);
	}
}
