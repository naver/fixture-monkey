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

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;

@PropertyDefaults(tries = 10)
class PluginAdapterTest {

	@Property
	void plugin() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.plugin(optionsBuilder -> optionsBuilder.insertFirstNullInjectGenerators(String.class, context -> 1.0d))
			.build();

		String actual = sut.giveMeBuilder(SimpleObject.class).sample().getStr();

		then(actual).isEqualTo(null);
	}

	@Property(tries = 1)
	void alterArbitraryValidator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.arbitraryValidator(obj -> {
				throw new ValidationFailedException("thrown by test ArbitraryValidator", new HashSet<>());
			})
			.build();

		thenThrownBy(() -> sut.giveMeOne(String.class)).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Property
	void customizePropertyUnique() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultNotNull(true)
			.build();

		// when
		List<Integer> actual = sut
			.giveMeExperimentalBuilder(new TypeReference<List<Integer>>() {
			})
			.<Integer>customizeProperty(typedString("$[*]"), it -> it.filter(integer -> 0 <= integer && integer < 4))
			.<List<Integer>>customizeProperty(typedRoot(), CombinableArbitrary::unique)
			.size("$", 3)
			.sample();

		// then
		Set<Integer> expected = new HashSet<>(actual);
		then(actual).hasSize(expected.size());
	}

}
