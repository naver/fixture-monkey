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

package com.navercorp.fixturemonkey.starter;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;

class FixtureMonkeyStarterTest {
	@Data   // lombok getter, setter
	public static class Order {
		private Long id;

		private String orderNo;

		private String productName;

		private int quantity;

		private long price;

		private List<String> items = new ArrayList<>();

		private Instant orderedAt;

		private String sellerEmail;
	}

	@Test
	void test() {
		// given
		FixtureMonkey sut = FixtureMonkey.create();

		// when
		Order actual = sut.giveMeOne(Order.class);

		// then
		then(actual).isNotNull();
	}
}
