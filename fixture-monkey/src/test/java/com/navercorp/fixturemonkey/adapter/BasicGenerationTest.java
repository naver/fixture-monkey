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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.jqwik.api.Arbitrary;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Child;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NullableObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ObjectWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StaticFieldObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;

class BasicGenerationTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.build();

	@Test
	void sampleStringWrapper() {
		// when
		StringWrapper actual = SUT.giveMeBuilder(StringWrapper.class).sample();

		// then
		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Test
	void sampleStringListWrapper() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class).sample();

		// then
		then(actual).isNotNull();
		then(actual.getValues()).isNotNull();
	}

	@Test
	void sampleStringWrapperList() {
		// when
		StringWrapperList actual = SUT.giveMeBuilder(StringWrapperList.class).sample();

		// then
		then(actual).isNotNull();
		then(actual.getValues()).isNotNull();
	}

	@Test
	void sampleWithTypeReference() {
		TypeReference<StringListWrapper> type = new TypeReference<StringListWrapper>() {
		};

		// when
		StringListWrapper actual = SUT.giveMeBuilder(type).sample();

		// then
		then(actual.getValues()).isNotNull();
	}

	@Test
	void sampleListTypeReference() {
		// when
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
		}).sample();

		then(actual).isNotNull();
	}

	@Test
	void sampleWithType() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).sample();

		// then
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

	@Test
	void sampleNotGeneratingStaticField() {
		FixtureMonkey sut = FixtureMonkey.builder().defaultNotNull(true).build();

		thenNoException().isThrownBy(() -> sut.giveMeOne(StaticFieldObject.class));
	}

	@Test
	void generatePrimitiveArray() {
		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class).fixed().sample().getIntArray();

		then(actual).isNotNull();
	}

	@Test
	void sampleObjectField() {
		ObjectWrapper actual = SUT.giveMeOne(ObjectWrapper.class);

		then(actual).isNotNull();
	}

	@Test
	void sampleChild() {
		Child actual = SUT.giveMeOne(Child.class);

		then(actual).isNotNull();
	}

	@Test
	void sampleNullableContainerReturnsNotNull() {
		// when
		List<String> values = SUT.giveMeOne(NullableObject.class).getValues();

		then(values).isNotNull();
	}

	@Test
	void notFixedSampleReturnsDiff() {
		ArbitraryBuilder<SimpleObject> builder = SUT.giveMeBuilder(SimpleObject.class);

		SimpleObject sample1 = builder.sample();
		SimpleObject sample2 = builder.sample();
		then(sample1).isNotEqualTo(sample2);
	}

	@Test
	void sampleSupplier() {
		Supplier<String> actual = SUT.giveMeBuilder(new TypeReference<Supplier<String>>() {
		}).sample();

		then(actual).isNotNull();
		then(actual.get()).isNotNull();
	}

	@Test
	void sampleNestedStrSupplier() {
		Supplier<Supplier<String>> actual = SUT.giveMeBuilder(
			new TypeReference<Supplier<Supplier<String>>>() {
			}
		).sample();

		then(actual).isNotNull();
		then(actual.get()).isNotNull();
		then(actual.get().get()).isNotNull();
	}

	@Test
	void supplierReturnsNew() {
		// given
		TypeReference<Supplier<String>> supplierTypeReference = new TypeReference<Supplier<String>>() {
		};
		ArbitraryBuilder<Supplier<String>> supplierArbitraryBuilder = SUT.giveMeBuilder(supplierTypeReference);

		// when
		Arbitrary<Supplier<String>> supplierArbitrary = supplierArbitraryBuilder.build();

		// then
		Supplier<String> result1 = supplierArbitrary.sample();
		Supplier<String> result2 = supplierArbitrary.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Test
	void sampleWildcard() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<List<? extends SimpleObject>>() {
		}));
	}

	@Test
	void sampleMapValueWildcardListString() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<Map<String, ? extends List<String>>>() {
		}));
	}

	static class StringWrapperListHolder {

		private List<List<String>> nestedValues;

		public List<List<String>> getNestedValues() {
			return nestedValues;
		}

		public void setNestedValues(List<List<String>> nestedValues) {
			this.nestedValues = nestedValues;
		}
	}

	@Test
	void nestedStringListWrapperWorks() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		StringWrapperListHolder wrapped = sut.giveMeOne(StringWrapperListHolder.class);

		then(wrapped).isNotNull();
		then(wrapped.getNestedValues()).isNotNull();
	}

	@Test
	void nestedStringListWrapperInnerElementIsNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		StringWrapperListHolder wrapped = sut.giveMeOne(StringWrapperListHolder.class);

		then(wrapped).isNotNull();
		then(wrapped.getNestedValues()).isNotNull();
		if (!wrapped.getNestedValues().isEmpty()) {
			then(wrapped.getNestedValues().get(0)).isNotNull();
		}
	}
}
