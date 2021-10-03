package com.navercorp.fixturemonkey.extree;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Value;

import com.navercorp.fixturemonkey.extree.ExTest.Person.Company;

class ExTest {
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

	@Test
	void toNoneGetterPublicFieldClass() {
		// when
		String actual = Ex.to((NoneGetterPublicClass c) -> c.value);

		then(actual).isEqualTo("value");
	}

	@Test
	void toOnlyGetterClass() {
		// when
		String actual = Ex.to(OnlyGetterClass::getValue);

		then(actual).isEqualTo("value");
	}

	@Test
	void toFieldNoneGetterClass() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.noneGetterPublicClass.value);

		then(actual).isEqualTo("noneGetterPublicClass.value");
	}

	@Test
	void toGetterNoneGetterClass() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.getNoneGetterPublicClass().value);

		then(actual).isEqualTo("noneGetterPublicClass.value");
	}

	@Test
	void toGetterOnlyGetterClass() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.getOnlyGetterClass().getValue());

		then(actual).isEqualTo("onlyGetterClass.value");
	}

	@Test
	void toFieldOnlyGetterClass() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.onlyGetterClass.getValue());

		then(actual).isEqualTo("onlyGetterClass.value");
	}

	@Test
	void toFieldOnlyGetterClasses() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.onlyGetterClasses.get(1).getValue());

		then(actual).isEqualTo("onlyGetterClasses[1].value");
	}

	@Test
	void toGetterOnlyGetterClasses() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.getOnlyGetterClasses().get(1).getValue());

		then(actual).isEqualTo("onlyGetterClasses[1].value");
	}

	@Test
	void toFieldNoneGetterClasses() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.noneGetterPublicClasses.get(1).value);

		then(actual).isEqualTo("noneGetterPublicClasses[1].value");
	}

	@Test
	void toGetterNoneGetterClasses() {
		// when
		String actual = Ex.to((NestedExceptCase c) -> c.noneGetterPublicClasses.get(1).value);

		then(actual).isEqualTo("noneGetterPublicClasses[1].value");
	}

	@Test
	void toGetterFakeList() {
		thenThrownBy(() -> Ex.to(FakeList::get))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.satisfies(it -> then(it.getMessage()).contains("Given method is not getter."));
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

	public static class NoneGetterPublicClass {
		public String value;
	}

	public static class OnlyGetterClass {
		public String getValue() {
			return "value";
		}
	}

	@Value
	public static class NestedExceptCase {
		NoneGetterPublicClass noneGetterPublicClass;

		OnlyGetterClass onlyGetterClass;

		List<NoneGetterPublicClass> noneGetterPublicClasses;

		List<OnlyGetterClass> onlyGetterClasses;
	}

	@Value
	public static class FakeList {
		public String get() {
			return "value";
		}
	}
}
