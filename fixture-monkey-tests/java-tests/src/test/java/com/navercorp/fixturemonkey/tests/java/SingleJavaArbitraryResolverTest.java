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

import java.util.Date;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;

public class SingleJavaArbitraryResolverTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JavaxValidationPlugin())
		.plugin(new JakartaValidationPlugin())
		.defaultNotNull(true)
		.build();

	@Property(tries = 100)
	void sampleInt() {
		SingleJavaArbitraryResolverTestSpecs.IntObject actual = SUT.giveMeOne(
			SingleJavaArbitraryResolverTestSpecs.IntObject.class);

		then(actual.getJavaxMinValue()).isGreaterThanOrEqualTo(100);
		then(actual.getJakartaMaxValue()).isLessThanOrEqualTo(100);
	}

	@Property(tries = 100)
	void sampleDate() {
		SingleJavaArbitraryResolverTestSpecs.DateObject actual = SUT.giveMeOne(
			SingleJavaArbitraryResolverTestSpecs.DateObject.class);

		Date now = new Date();
		then(actual.getJavaxDatePast().getTime()).isLessThan(now.getTime());
		then(actual.getJakartaDateFuture().getTime()).isGreaterThan(now.getTime());
	}
}
