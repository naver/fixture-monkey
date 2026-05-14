package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.datafaker.arbitrary.DataFakerStringArbitrary;

class DataFakerArbitraryTest {

	@Test
	void fullNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().fullName();
		then(value).isNotBlank();
	}

	@Test
	void firstNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().firstName();
		then(value).isNotBlank();
	}

	@Test
	void lastNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.name().lastName();
		then(value).isNotBlank();
	}

	@Test
	void fullAddressShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().fullAddress();
		then(value).isNotBlank();
	}

	@Test
	void cityShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().city();
		then(value).isNotBlank();
	}

	@Test
	void countryShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().country();
		then(value).isNotBlank();
	}

	@Test
	void zipCodeShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.address().zipCode();
		then(value).isNotBlank();
	}

	@Test
	void emailAddressShouldContainAtSymbol() {
		String value = DataFakerStringArbitrary.internet().emailAddress();
		then(value).contains("@");
	}

	@Test
	void domainNameShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.internet().domainName();
		then(value).isNotBlank();
	}

	@Test
	void urlShouldStartWithHttpOrHttps() {
		String value = DataFakerStringArbitrary.internet().url();
		then(value).startsWith("www");
	}

	@Test
	void phoneNumberShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.phoneNumber().cellPhone();
		then(value).isNotBlank();
	}

	@Test
	void creditCardShouldNotBeBlank() {
		String value = DataFakerStringArbitrary.finance().creditCard();
		then(value).isNotBlank();
	}
}
