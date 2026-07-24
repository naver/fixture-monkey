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
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericChildTwoValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericChildValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericStringWrapperValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericWrapper;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.GenericWrapperValue;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.SimpleStringObject;

class ValueProjectionGenericTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void generateGenericWrapper() {
		GenericWrapper<String> actual = SUT.giveMeBuilder(
			new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<String>>() {
			}
		).sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Test
	void setGenericWrapperValue() {
		GenericWrapper<String> actual = SUT.giveMeBuilder(
				new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<String>>() {
				}
			)
			.set("value", "generic-value")
			.sample();

		then(actual.getValue()).isEqualTo("generic-value");
	}

	@Test
	void generateGenericListWrapper() {
		GenericWrapper<List<String>> actual = SUT.giveMeBuilder(
			new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<List<String>>>() {
			}
		).sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
		then(actual.getValue()).isInstanceOf(java.util.ArrayList.class);
	}

	@Test
	void setGenericListWrapperValue() {
		List<String> expected = java.util.Arrays.asList("a", "b");

		GenericWrapper<List<String>> actual = SUT.giveMeBuilder(
				new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<List<String>>>() {
				}
			)
			.set("value", expected)
			.sample();

		then(actual.getValue()).isEqualTo(expected);
	}

	@Test
	void generateNestedGeneric() {
		GenericWrapper<GenericWrapper<String>> actual = SUT.giveMeBuilder(
			new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<GenericWrapper<String>>>() {
			}
		).sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
		then(actual.getValue().getValue()).isNotNull();
	}

	@Test
	void setNestedGenericValue() {
		GenericWrapper<GenericWrapper<String>> actual = SUT.giveMeBuilder(
				new com.navercorp.fixturemonkey.api.type.TypeReference<GenericWrapper<GenericWrapper<String>>>() {
				}
			)
			.set("value.value", "nested-generic")
			.sample();

		then(actual.getValue().getValue()).isEqualTo("nested-generic");
	}

	@Test
	void sampleGeneric() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Test
	void sampleGenericWildcardExtends() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<? extends String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Test
	void sampleStringGenericField() {
		GenericStringWrapperValue actual = SUT.giveMeOne(GenericStringWrapperValue.class);

		then(actual).isNotNull();
	}

	@Test
	void sampleGenericField() {
		GenericWrapperValue<String> actual = SUT.giveMeOne(new TypeReference<GenericWrapperValue<String>>() {
		});

		then(actual).isNotNull();
	}

	@Test
	void sampleGenericChild() {
		GenericChildValue actual = SUT.giveMeOne(GenericChildValue.class);

		then(actual).isNotNull();
	}

	@Test
	void sampleTwoGenericChild() {
		GenericChildTwoValue actual = SUT.giveMeOne(GenericChildTwoValue.class);

		then(actual).isNotNull();
	}

	@Test
	void sampleWildcard() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<List<? extends SimpleStringObject>>() {
		}));
	}

	@Test
	void sampleMapValueWildcardListString() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<Map<String, ? extends List<String>>>() {
		}));
	}

	@Test
	void setGenericContainerThenSize_shouldNotLockConcreteType() {
		// given
		GenericWrapper<List<String>> actual = SUT.giveMeBuilder(
				new TypeReference<GenericWrapper<List<String>>>() {
				}
			)
			.set("value", java.util.Collections.singletonList("test"))
			.size("value", 1, 10)
			.sample();

		// then
		then(actual.getValue()).isNotNull();
		then(actual.getValue()).isInstanceOf(List.class);
		then(actual.getValue().size()).isBetween(1, 10);
	}
}
