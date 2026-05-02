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

package com.navercorp.fixturemonkey.tests.java.adapter;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary;

class DataFakerArbitraryAdapterTest {

	@RepeatedTest(TEST_COUNT)
	void fullNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().fullName();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void firstNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().firstName();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void lastNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().lastName();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void fullAddressShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().fullAddress();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void cityShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().city();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void countryShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().country();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void zipCodeShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().zipCode();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void emailAddressShouldContainAtSymbol() {
		String value = DataFakerStringArbitrary.internet().emailAddress();
		then(value).contains("@");
	}

	@RepeatedTest(TEST_COUNT)
	void domainNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.internet().domainName();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void urlShouldStartWithHttpOrHttps() {
		String value = DataFakerStringArbitrary.internet().url();
		then(value).startsWith("www");
	}

	@RepeatedTest(TEST_COUNT)
	void phoneNumberShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.phoneNumber().cellPhone();
		then(value).isNotBlank();
	}

	@RepeatedTest(TEST_COUNT)
	void creditCardShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.finance().creditCard();
		then(value).isNotBlank();
	}
}
