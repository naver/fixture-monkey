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

package com.navercorp.fixturemonkey.datafaker.plugin;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.datafaker.Faker;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.datafaker.arbitrary.FakerCombinableArbitrary;

@API(since = "0.6.3", status = Status.EXPERIMENTAL)
public final class DataFakerPlugin implements Plugin {

	private final Function<Faker, String> fakerFunction;

	public DataFakerPlugin(Function<Faker, String> fakerFunction) {
		this.fakerFunction = fakerFunction;
	}

	public DataFakerPlugin() {
		this(faker -> faker.name().fullName());
	}

	@Override
	public void accept(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.insertFirstArbitraryIntrospector(
			MatcherOperator.exactTypeMatchOperator(String.class, (context) -> new ArbitraryIntrospectorResult(
				new FakerCombinableArbitrary(fakerFunction)))
		);
	}
}
