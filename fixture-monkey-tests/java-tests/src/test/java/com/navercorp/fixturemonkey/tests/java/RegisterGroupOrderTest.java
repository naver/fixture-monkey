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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.annotation.Order;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;

class RegisterGroupOrderTest {
	@Test
	void registerGroupClassWithLowerOrderWinsOverOneRegisteredLater() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(OrderedGroup.class, PlainGroup.class)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("ordered");
	}

	@Test
	void registerGroupClassWithDefaultOrderIsLikeOneWithoutOrder() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(PlainGroup.class, DefaultOrderGroup.class)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("defaultOrder");
	}

	@Test
	void registerGroupInstanceWithLowerOrderWinsOverOneRegisteredLater() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(new OrderedBuilderGroup(), new PlainBuilderGroup())
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("ordered");
	}

	@Test
	void registerGroupWithLowerOrderWinsOverBuilderRegisteredLaterWithDefaultPriority() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(OrderedGroup.class)
			.register(String.class, fm -> fm.giveMeBuilder("registered"))
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("ordered");
	}

	@Order(1)
	public static class OrderedGroup {
		public ArbitraryBuilder<String> string(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder("ordered");
		}
	}

	public static class PlainGroup {
		public ArbitraryBuilder<String> string(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder("plain");
		}
	}

	@Order
	public static class DefaultOrderGroup {
		public ArbitraryBuilder<String> string(FixtureMonkey fixtureMonkey) {
			return fixtureMonkey.giveMeBuilder("defaultOrder");
		}
	}

	@Order(1)
	public static class OrderedBuilderGroup implements ArbitraryBuilderGroup {
		@Override
		public ArbitraryBuilderCandidateList generateCandidateList() {
			return ArbitraryBuilderCandidateList.create()
				.add(ArbitraryBuilderCandidateFactory.of(String.class).value("ordered"));
		}
	}

	public static class PlainBuilderGroup implements ArbitraryBuilderGroup {
		@Override
		public ArbitraryBuilderCandidateList generateCandidateList() {
			return ArbitraryBuilderCandidateList.create()
				.add(ArbitraryBuilderCandidateFactory.of(String.class).value("plain"));
		}
	}
}
