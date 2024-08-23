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

package com.navercorp.fixturemonkey.api.arbitrary;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;

public class ArbitraryTest {
	@Test
	void containerMappingNotUnique() {
		List<Object> actual = CombinableArbitrary.containerBuilder()
			.element(CombinableArbitrary.from("unique"))
			.element(CombinableArbitrary.from("notUnique"))
			.build(ArrayList::new)
			.map(list -> {
				list.set(1, "unique");
				return list;
			})
			.unique()
			.combined();

		then(actual).hasSize(2);
	}

	@Test
	void containerUnique() {
		thenThrownBy(() -> CombinableArbitrary.containerBuilder()
			.element(CombinableArbitrary.from("unique"))
			.element(CombinableArbitrary.from("unique"))
			.build(ArrayList::new)
			.unique()
			.combined())
			.isExactlyInstanceOf(FixedValueFilterMissException.class);
	}
}
