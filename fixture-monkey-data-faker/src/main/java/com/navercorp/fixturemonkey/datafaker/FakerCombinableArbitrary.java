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

package com.navercorp.fixturemonkey.datafaker;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.datafaker.Faker;
import net.datafaker.providers.base.AbstractProvider;
import net.datafaker.providers.base.BaseProviders;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public class FakerCombinableArbitrary implements CombinableArbitrary {

	final Faker faker = new Faker();
	final Function<Faker, String> fakerFunction;

	FakerCombinableArbitrary() {
		this.fakerFunction = faker -> faker.name().fullName();
	}

	FakerCombinableArbitrary(Function<Faker, String> fakerFunction) {
		this.fakerFunction = fakerFunction;
	}

	@Override
	public String combined() {
		return fakerFunction.apply(faker);
	}

	@Override
	public String rawValue() {
		return fakerFunction.apply(faker);
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return false;
	}
}
