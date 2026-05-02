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

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Order;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleListObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleStringObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringValue;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class ValueProjectionThenApplyTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void thenApplyTransformsValue() {
		StringValue actual = SUT.giveMeBuilder(StringValue.class)
			.set("value", "original")
			.thenApply((it, builder) -> builder.set("value", it.getValue() + "-modified"))
			.sample();

		then(actual.getValue()).isEqualTo("original-modified");
	}

	@Property
	void thenApplyWithNestedObject() {
		Order actual = SUT.giveMeBuilder(Order.class)
			.set("orderId", "ORD-001")
			.set("product.name", "Initial")
			.thenApply((it, builder) -> builder.set("product.name", it.getProduct().getName() + "-Updated"))
			.sample();

		then(actual.getOrderId()).isEqualTo("ORD-001");
		then(actual.getProduct().getName()).isEqualTo("Initial-Updated");
	}

	@Property
	void setNestedAndThenApply() {
		Order actual = SUT.giveMeBuilder(Order.class)
			.set("orderId", "ORD-001")
			.set("quantity", 10)
			.thenApply((it, builder) -> builder.set("product.name", "Product-Q" + it.getQuantity()))
			.sample();

		then(actual.getOrderId()).isEqualTo("ORD-001");
		then(actual.getQuantity()).isEqualTo(10);
		then(actual.getProduct().getName()).isEqualTo("Product-Q10");
	}

	@Property
	void apply() {
		String actual = SUT.giveMeBuilder(SimpleListObject.class)
			.set("str", "set")
			.thenApply((it, builder) -> builder.size("strList", 1).set("strList[0]", it.getStr()))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void applyNotAffectedManipulatorsAfterApply() {
		String actual = SUT.giveMeBuilder(SimpleListObject.class)
			.set("str", "set")
			.thenApply((it, builder) -> builder.size("strList", 1).set("strList[0]", it.getStr()))
			.set("str", "afterApply")
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void acceptIfAlwaysTrue() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.acceptIf(it -> true, builder -> builder.set("str", "set"))
			.sample()
			.getStr();

		then(actual).isEqualTo("set");
	}

	@Property
	void acceptIf() {
		String actual = SUT.giveMeBuilder(SimpleListObject.class)
			.set("str", "set")
			.acceptIf(it -> "set".equals(it.getStr()), builder -> builder.size("strList", 1).set("strList[0]", "set"))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void applySetElementNull() {
		String actual = SUT.giveMeBuilder(SimpleListObject.class)
			.thenApply((obj, builder) -> builder.size("strList", 1).setNull("strList[0]"))
			.sample()
			.getStrList()
			.get(0);

		then(actual).isNull();
	}

	@Property(tries = 1)
	void applySampleTwiceReturnsDiff() {
		ArbitraryBuilder<SimpleStringObject> builder = SUT.giveMeBuilder(SimpleStringObject.class).thenApply(
			(obj, b) -> {
			}
		);

		SimpleStringObject actual = builder.sample();

		SimpleStringObject expected = builder.sample();
		then(actual).isNotEqualTo(expected);
	}
}
