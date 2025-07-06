package com.navercorp.fixturemonkey.tests.java;

import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary;
import org.junit.jupiter.api.RepeatedTest;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

class DataFakerArbitraryTest {

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
