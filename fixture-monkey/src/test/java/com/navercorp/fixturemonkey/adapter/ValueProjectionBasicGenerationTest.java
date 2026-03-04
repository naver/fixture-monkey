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

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ChildValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.IntArrayHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ObjectValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Order;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringListHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringValue;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class ValueProjectionBasicGenerationTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void generateString() {
		String actual = SUT.giveMeOne(String.class);

		then(actual).isNotNull();
	}

	@Property
	void generateInteger() {
		Integer actual = SUT.giveMeOne(Integer.class);

		then(actual).isNotNull();
	}

	@Property
	void generateSimpleObject() {
		StringValue actual = SUT.giveMeOne(StringValue.class);

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@Property
	void generateNestedObject() {
		Order actual = SUT.giveMeOne(Order.class);

		then(actual).isNotNull();
		then(actual.getProduct()).isNotNull();
		then(actual.getProduct().getName()).isNotNull();
	}

	@Property
	void generateListObject() {
		StringListHolder actual = SUT.giveMeOne(StringListHolder.class);

		then(actual).isNotNull();
		then(actual.getValues()).isNotNull();
	}

	@Property
	void generatePrimitiveArray() {
		int[] actual = SUT.giveMeBuilder(IntArrayHolder.class).fixed().sample().getValues();

		then(actual).isNotNull();
	}

	@Property
	void sampleObjectField() {
		ObjectValue actual = SUT.giveMeOne(ObjectValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleChildValue() {
		ChildValue actual = SUT.giveMeOne(ChildValue.class);

		then(actual).isNotNull();
	}
}
