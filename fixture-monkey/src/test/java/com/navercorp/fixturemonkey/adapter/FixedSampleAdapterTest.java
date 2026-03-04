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

import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;

@PropertyDefaults(tries = 10)
class FixedSampleAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void fixedSampleReturnsSame() {
		ArbitraryBuilder<SimpleObject> fixedBuilder = SUT.giveMeBuilder(SimpleObject.class).fixed();

		SimpleObject sample1 = fixedBuilder.sample();
		SimpleObject sample2 = fixedBuilder.sample();
		then(sample1.getStr()).isEqualTo(sample2.getStr());
	}

	@Property
	void arbitraryFixedSampleReturnsSame() {
		ArbitraryBuilder<SimpleObject> fixedBuilder = SUT.giveMeBuilder(SimpleObject.class)
			.set("str", Arbitraries.of("value1", "value2"))
			.fixed();

		SimpleObject sample1 = fixedBuilder.sample();
		SimpleObject sample2 = fixedBuilder.sample();
		then(sample1.getStr()).isEqualTo(sample2.getStr());
	}

	@Property
	void setNullFixedReturnsNull() {
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class).setNull("$").fixed().sample();

		then(actual).isNull();
	}

	@Property
	void fixedRangedSizeReturnsSameSize() {
		ArbitraryBuilder<StringListWrapper> fixedBuilder = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 1, 5)
			.fixed();

		List<String> actual = fixedBuilder.sample().getValues();
		List<String> expected = fixedBuilder.sample().getValues();
		then(actual).isEqualTo(expected);
	}

	@Property
	void setNullFixed() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("object")
			.fixed()
			.set("object.str", expected)
			.sample()
			.getObject()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void fixedOnDifferentBuilderCopiesProducesSameContainerSize() {
		// given
		ArbitraryBuilder<StringListWrapper> base = SUT.giveMeBuilder(StringListWrapper.class).fixed();

		// when
		StringListWrapper obj1 = base.copy().sample();
		StringListWrapper obj2 = base.copy().sample();
		StringListWrapper obj3 = base.copy().sample();
		int size1 = obj1.getValues().size();
		int size2 = obj2.getValues().size();
		int size3 = obj3.getValues().size();
		// then
		then(size1).isEqualTo(size2);
		then(size2).isEqualTo(size3);
	}

	@Property
	void fixedOnDifferentBuilderCopiesProducesSameNestedContainerSize() {
		// given
		ArbitraryBuilder<ComplexObject> base = SUT.giveMeBuilder(ComplexObject.class).fixed();

		// when
		ComplexObject obj1 = base.copy().sample();
		ComplexObject obj2 = base.copy().sample();

		// then
		then(obj1.getStrList()).hasSameSizeAs(obj2.getStrList());
		then(obj1.getList()).hasSameSizeAs(obj2.getList());
	}
}
