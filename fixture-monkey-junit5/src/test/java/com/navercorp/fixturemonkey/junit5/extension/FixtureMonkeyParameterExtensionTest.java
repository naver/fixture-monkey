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

package com.navercorp.fixturemonkey.junit5.extension;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import lombok.Data;

import com.navercorp.fixturemonkey.junit5.annotation.GiveMe;

@ExtendWith(FixtureMonkeyParameterExtension.class)
class FixtureMonkeyParameterExtensionTest {
	@RepeatedTest(10)
	void giveMeOne(@GiveMe Order order) {
		assertOrderCreatedCorrectly(order);
	}

	@Test
	void giveMeList(@GiveMe(size = 10) List<Order> orderList) {
		then(orderList).hasSize(10).allSatisfy(this::assertOrderCreatedCorrectly);
	}

	@Test
	void giveMeStream(@GiveMe Stream<Order> orderStream) {
		then(orderStream.limit(10)).hasSize(10).allSatisfy(this::assertOrderCreatedCorrectly);
	}

	private void assertOrderCreatedCorrectly(Order order) {
		then(order.getId()).isNotNull();
		then(order.getQuantity()).isBetween(1, 100);
		then(order.items).hasSizeLessThanOrEqualTo(3);
	}

	@Data
	public static class Order {
		@NotNull
		private Long id;

		@Min(1)
		@Max(100)
		private int quantity;

		@Size(max = 3)
		private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();
	}
}
