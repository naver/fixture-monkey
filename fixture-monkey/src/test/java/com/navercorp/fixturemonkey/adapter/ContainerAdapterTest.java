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
import java.util.Set;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.EnumObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MapEntryWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.MapStringListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.OptionalListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.TwoEnum;

@PropertyDefaults(tries = 10)
class ContainerAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void sampleEnumSet() {
		// when
		Set<TwoEnum> actual = SUT.giveMeOne(new TypeReference<Set<TwoEnum>>() {
		});

		then(actual).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumMap() {
		Map<TwoEnum, String> values = SUT.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumSetList() {
		FixtureMonkey sut = FixtureMonkey.builder().defaultNotNull(true).build();

		thenNoException().isThrownBy(() ->
			sut.giveMeBuilder(new TypeReference<List<Set<TwoEnum>>>() {
			}).size("$", 3).sample()
		);
	}

	@Property
	void sampleEnumMapInMap() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(new TypeReference<Map<String, Map<TwoEnum, String>>>() {
		}));
	}

	@Property
	void sampleListWildcardEnum() {
		// when
		List<? extends EnumObject> actual = SUT.giveMeOne(new TypeReference<List<? extends EnumObject>>() {
		});

		then(actual).isNotNull();
	}

	@Property
	void sampleUniqueSet() {
		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).size("$", 200).sample();

		then(actual).hasSize(200);
	}

	@Property
	void sampleUniqueMap() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.size("$", 200)
			.sample();

		then(actual).hasSize(200);
	}

	@Property
	void sampleStandaloneMapEntrySimple() {
		// when
		Map.Entry<String, Integer> actual = SUT.giveMeBuilder(
			new TypeReference<Map.Entry<String, Integer>>() {
			}
		).sample();

		// then
		then(actual).isNotNull();
		then(actual.getKey()).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void sampleStandaloneMapEntryComplex() {
		// when
		Map.Entry<String, SimpleObject> actual = SUT.giveMeBuilder(
			new TypeReference<Map.Entry<String, SimpleObject>>() {
			}
		).sample();

		// then
		then(actual).isNotNull();
		then(actual.getKey()).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void sampleMapEntryWrapper() {
		// when
		MapEntryWrapper actual = SUT.giveMeBuilder(MapEntryWrapper.class).sample();

		// then
		then(actual).isNotNull();
		then(actual.getSimpleEntry()).isNotNull();
		then(actual.getSimpleEntry().getKey()).isNotNull();
		then(actual.getSimpleEntry().getValue()).isNotNull();
		then(actual.getComplexEntry()).isNotNull();
		then(actual.getComplexEntry().getKey()).isNotNull();
		then(actual.getComplexEntry().getValue()).isNotNull();
	}

	@Property
	void sampleNestedListList() {
		// when
		ListListStringObject actual = SUT.giveMeBuilder(ListListStringObject.class)
			.size("values", 2)
			.size("values[*]", 2)
			.sample();

		// then
		then(actual.getValues()).hasSize(2);
		for (List<String> inner : actual.getValues()) {
			then(inner).hasSize(2);
			for (String s : inner) {
				then(s).isNotNull();
			}
		}
	}

	@Property
	void sampleMapWithListValue() {
		// when
		MapStringListObject actual = SUT.giveMeBuilder(MapStringListObject.class)
			.size("values", 2)
			.sample();

		// then
		then(actual.getValues()).hasSize(2);
		for (Map.Entry<String, List<String>> entry : actual.getValues().entrySet()) {
			then(entry.getKey()).isNotNull();
			then(entry.getValue()).isNotNull();
		}
	}

	@Property
	void sampleOptionalWithList() {
		// when
		OptionalListStringObject actual = SUT.giveMeBuilder(OptionalListStringObject.class)
			.size("value", 2)
			.sample();

		// then
		then(actual.getValue()).isPresent();
		then(actual.getValue().get()).hasSize(2);
		for (String s : actual.getValue().get()) {
			then(s).isNotNull();
		}
	}

}
