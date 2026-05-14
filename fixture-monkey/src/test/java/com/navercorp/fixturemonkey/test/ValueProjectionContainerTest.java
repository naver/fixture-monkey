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

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.MapHolder;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.NestedListHolder;

class ValueProjectionContainerTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void generateNestedList() {
		NestedListHolder actual = SUT.giveMeOne(NestedListHolder.class);

		then(actual).isNotNull();
		then(actual.getNestedValues()).isNotNull();
	}

	@Test
	void generateMapField() {
		MapHolder actual = SUT.giveMeOne(MapHolder.class);

		then(actual).isNotNull();
		then(actual.getMapping()).isNotNull();
	}
}
