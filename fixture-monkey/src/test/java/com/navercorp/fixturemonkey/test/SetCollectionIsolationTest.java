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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class SetCollectionIsolationTest {
	private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void eachSampleOwnsTheListPassedToSet() {
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = FIXTURE_MONKEY.giveMeBuilder(Holder.class)
			.set("items", supplied);

		Holder first = builder.sample();
		Holder second = builder.sample();
		first.items.add("c");

		assertThat(first.items).isNotSameAs(supplied);
		assertThat(second.items).isNotSameAs(first.items).containsExactly("a", "b");
		assertThat(supplied).containsExactly("a", "b");
	}

	@Test
	void fixedSizeInputDoesNotRestrictTheSampledList() {
		Holder sampled = FIXTURE_MONKEY.giveMeBuilder(Holder.class)
			.set("items", Arrays.asList("a", "b"))
			.sample();

		sampled.items.remove(0);
		assertThat(sampled.items).containsExactly("b");
	}

	private static class Holder {
		private List<String> items;
	}
}
