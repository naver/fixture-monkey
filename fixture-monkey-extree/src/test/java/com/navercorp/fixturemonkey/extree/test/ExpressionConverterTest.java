package com.navercorp.fixturemonkey.extree.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Value;

import com.navercorp.fixturemonkey.extree.ExpressionConverter;
import com.navercorp.fixturemonkey.extree.test.ExpressionConverterTest.Person.Company;

public class ExpressionConverterTest {
	@Test
	void parseField() {
		// when
		String actual = ExpressionConverter.to((Person p) -> p.company);

		then(actual).isEqualTo("company");
	}

	@Test
	void parseGetter() {
		// when
		String actual = ExpressionConverter.to(Person::getCompany);

		then(actual).isEqualTo("company");
	}

	@Test
	void parseFieldField() {
		// when
		String actual = ExpressionConverter.to((Person c) -> c.company.name);

		then(actual).isEqualTo("company.name");
	}

	@Test
	void parseGetterField() {
		// when
		String actual = ExpressionConverter.to((Person c) -> c.getCompany().name);

		then(actual).isEqualTo("company.name");
	}

	@Test
	void parseFieldGetter() {
		// when
		String actual = ExpressionConverter.to((Person c) -> c.company.getName());

		then(actual).isEqualTo("company.name");
	}

	@Test
	void parseGetterGetter() {
		// when
		String actual = ExpressionConverter.to((Person c) -> c.getCompany().getName());

		then(actual).isEqualTo("company.name");
	}

	@Test
	void parseListElement() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.productNames.get(0));

		then(actual).isEqualTo("productNames[0]");
	}

	@Test
	void parseGetterListElementField() {
		// when
		String actual = ExpressionConverter.to((Person p) -> p.getCompany().productNames.get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void parseFieldListElementGetter() {
		// when
		String actual = ExpressionConverter.to((Person p) -> p.company.getProductNames().get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void parseFieldListElementField() {
		// when
		String actual = ExpressionConverter.to((Person p) -> p.company.productNames.get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void parseNestedListElementField() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.names.get(0).get(0));

		then(actual).isEqualTo("names[0][0]");
	}

	@Test
	void parseNestedListElementFieldDiffIndex() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.names.get(3).get(4));

		then(actual).isEqualTo("names[3][4]");
	}

	@Test
	void parseNestedListElementGetter() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.getNames().get(0).get(0));

		then(actual).isEqualTo("names[0][0]");
	}

	@Test
	void parseArrayElementGetter() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.getNameArray()[0]);

		then(actual).isEqualTo("nameArray[0]");
	}

	@Test
	void parseArrayElementField() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.nameArray[0]);

		then(actual).isEqualTo("nameArray[0]");
	}

	@Test
	void parseNestedArrayElementField() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.nameNestedArray[0][0]);

		then(actual).isEqualTo("nameNestedArray[0][0]");
	}

	@Test
	void parseNestedArrayElementGetter() {
		// when
		String actual = ExpressionConverter.to((Company c) -> c.getNameNestedArray()[0][0]);

		then(actual).isEqualTo("nameNestedArray[0][0]");
	}

	@Value
	@Builder
	public static class Person {
		String name;

		Company company;

		@Value
		public static class Company {
			String name;

			List<String> productNames;

			List<List<String>> names;

			String[] nameArray;

			String[][] nameNestedArray;
		}
	}
}
