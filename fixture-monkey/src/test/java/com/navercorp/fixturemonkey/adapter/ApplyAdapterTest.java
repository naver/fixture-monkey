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

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;

@PropertyDefaults(tries = 10)
class ApplyAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void apply() {
		// when
		String actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 1)
			.thenApply((it, builder) -> builder.set("values[0]", "applied"))
			.sample()
			.getValues()
			.get(0);

		then(actual).isEqualTo("applied");
	}

	@Property
	void applyWithoutAnyManipulators() {
		// when
		StringWrapper actual = SUT.giveMeBuilder(StringWrapper.class)
			.thenApply((it, builder) ->
				builder.set("value", "applied-" + (it.getValue() != null ? it.getValue().length() : 0))
			)
			.sample();

		// then
		then(actual.getValue()).startsWith("applied-");
	}

	@Property
	void applyWithComplexObject() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.thenApply((it, builder) -> builder.size("strList", 1).set("strList[0]", it.getStr()))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void applyNotAffectedManipulatorsAfterApply() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.thenApply((it, builder) -> builder.size("strList", 1).set("strList[0]", it.getStr()))
			.set("str", "afterApply")
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void applySetElementNull() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, builder) -> builder.size("strList", 1).setNull("strList[0]"))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isNull();
	}

	@Property(tries = 100)
	void thenApplySetNullElementShouldAlwaysBeNull() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.thenApply((obj, builder) -> builder.size("strList", 1).setNull("strList[0]"))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isNull();
	}

	@Property
	void applySampleTwiceReturnsDiff() {
		ArbitraryBuilder<SimpleObject> builder = SUT.giveMeBuilder(SimpleObject.class).thenApply((obj, b) -> {
		});

		SimpleObject actual = builder.sample();
		SimpleObject expected = builder.sample();
		then(actual).isNotEqualTo(expected);
	}

	@Property
	void applyFixedSize() {
		String expectedElement = "test";

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.minSize("strList", 1)
			.thenApply((it, builder) -> builder.set("strList[*]", expectedElement))
			.sample()
			.getStrList();

		then(actual).allMatch(expectedElement::equals);
	}

	@Property
	void acceptIfAlwaysTrue() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.acceptIf(it -> true, builder -> builder.set("str", "set"))
			.sample()
			.getStr();

		then(actual).isEqualTo("set");
	}

	@Property
	void acceptIf() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.acceptIf(it -> "set".equals(it.getStr()), builder -> builder.size("strList", 1).set("strList[0]", "set"))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void giveMeListTypeApply() {
		SimpleObject actual = SUT.giveMeBuilder(new TypeReference<List<SimpleObject>>() {
			})
			.size("$", 5)
			.thenApply((it, builder) -> builder.set("$[4].str", it.get(4).getInteger() + ""))
			.sample()
			.get(4);

		then(actual.getStr()).isEqualTo(actual.getInteger() + "");
	}

	@Property
	void acceptIfSetNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.acceptIf(it -> "set".equals(it.getStr()), builder -> builder.setNull("str"))
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void acceptIfSetNotNull() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("object")
			.acceptIf(it -> it.getObject() == null, builder -> builder.setNotNull("object"))
			.sample()
			.getObject();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNullThenAcceptIfSet() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.acceptIf(it -> it.getStr() == null, builder -> builder.set("str", "recovered"))
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("recovered");
	}

	@Property
	void setNotNullThenAcceptIfSetNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.acceptIf(it -> it.getStr() != null, builder -> builder.setNull("str"))
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}
}
