package com.navercorp.fixturemonkey.extree.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Value;

import com.navercorp.fixturemonkey.extree.Ex;
import com.navercorp.fixturemonkey.extree.test.ExTest.Person.Company;

public class ExTest {
	@Test
	void toField() {
		// when
		String actual = Ex.to((Person p) -> p.company);

		then(actual).isEqualTo("company");
	}

	@Test
	void toGetter() {
		// when
		String actual = Ex.to(Person::getCompany);

		then(actual).isEqualTo("company");
	}

	@Test
	void toFieldField() {
		// when
		String actual = Ex.to((Person c) -> c.company.name);

		then(actual).isEqualTo("company.name");
	}

	@Test
	void toGetterField() {
		// when
		String actual = Ex.to((Person c) -> c.getCompany().name);

		then(actual).isEqualTo("company.name");
	}

	@Test
	void toFieldGetter() {
		// when
		String actual = Ex.to((Person c) -> c.company.getName());

		then(actual).isEqualTo("company.name");
	}

	@Test
	void toGetterGetter() {
		// when
		String actual = Ex.to((Person c) -> c.getCompany().getName());

		then(actual).isEqualTo("company.name");
	}

	@Test
	void toListElement() {
		// when
		String actual = Ex.to((Company c) -> c.productNames.get(0));

		then(actual).isEqualTo("productNames[0]");
	}

	@Test
	void toGetterListElementField() {
		// when
		String actual = Ex.to((Person p) -> p.getCompany().productNames.get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void toFieldListElementGetter() {
		// when
		String actual = Ex.to((Person p) -> p.company.getProductNames().get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void toFieldListElementField() {
		// when
		String actual = Ex.to((Person p) -> p.company.productNames.get(0));

		then(actual).isEqualTo("company.productNames[0]");
	}

	@Test
	void toNestedListElementField() {
		// when
		String actual = Ex.to((Company c) -> c.names.get(0).get(0));

		then(actual).isEqualTo("names[0][0]");
	}

	@Test
	void toNestedListElementFieldDiffIndex() {
		// when
		String actual = Ex.to((Company c) -> c.names.get(3).get(4));

		then(actual).isEqualTo("names[3][4]");
	}

	@Test
	void toNestedListElementGetter() {
		// when
		String actual = Ex.to((Company c) -> c.getNames().get(0).get(0));

		then(actual).isEqualTo("names[0][0]");
	}

	@Test
	void toArrayElementGetter() {
		// when
		String actual = Ex.to((Company c) -> c.getNameArray()[0]);

		then(actual).isEqualTo("nameArray[0]");
	}

	@Test
	void toArrayElementField() {
		// when
		String actual = Ex.to((Company c) -> c.nameArray[0]);

		then(actual).isEqualTo("nameArray[0]");
	}

	@Test
	void toNestedArrayElementField() {
		// when
		String actual = Ex.to((Company c) -> c.nameNestedArray[0][0]);

		then(actual).isEqualTo("nameNestedArray[0][0]");
	}

	@Test
	void toNestedArrayElementGetter() {
		// when
		String actual = Ex.to((Company c) -> c.getNameNestedArray()[0][0]);

		then(actual).isEqualTo("nameNestedArray[0][0]");
	}

	@Test
	void toBooleanGetter() {
		// when
		String actual = Ex.to(Company::isCheck);

		then(actual).isEqualTo("check");
	}

	@Test
	void toFieldBooleanGetter() {
		// when
		String actual = Ex.to((Person p) -> p.getCompany().check);

		then(actual).isEqualTo("company.check");
	}

	@Test
	void toGetterBooleanGetter() {
		// when
		String actual = Ex.to((Person p) -> p.getCompany().isCheck());

		then(actual).isEqualTo("company.check");
	}

	@Test
	void toBooleanField() {
		// when
		String actual = Ex.to((Company c) -> c.check);

		then(actual).isEqualTo("check");
	}

	@Test
	void toFieldBooleanField() {
		// when
		String actual = Ex.to((Person p) -> p.company.check);

		then(actual).isEqualTo("company.check");
	}

	@Test
	void toGetterBooleanField() {
		// when
		String actual = Ex.to((Person p) -> p.getCompany().check);

		then(actual).isEqualTo("company.check");
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

			boolean check;
		}
	}
}
