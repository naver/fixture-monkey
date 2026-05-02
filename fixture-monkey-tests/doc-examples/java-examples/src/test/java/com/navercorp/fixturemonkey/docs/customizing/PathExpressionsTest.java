package com.navercorp.fixturemonkey.docs.customizing;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.Value;
import org.junit.jupiter.api.Test;

class PathExpressionsTest {

	@Value
	public static class JavaClass {
		String field;
		String[] array;
		List<String> list;
		Nested object;
		List<Nested> objectList;

		@Value
		public static class Nested {
			String nestedField;
		}
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void rootObject() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.set("$", null)
			.sample();

		then(javaClass).isNull();
	}

	@Test
	void directField() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.set("field", "hello")
			.sample();

		then(javaClass.getField()).isEqualTo("hello");
	}

	@Test
	void nestedField() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.set("object.nestedField", "nestedValue")
			.sample();

		then(javaClass.getObject().getNestedField()).isEqualTo("nestedValue");
	}

	@Test
	void listElement() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.size("list", 3)
			.set("list[0]", "first")
			.sample();

		then(javaClass.getList()).hasSize(3);
		then(javaClass.getList().get(0)).isEqualTo("first");
	}

	@Test
	void allListElements() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.size("list", 3)
			.set("list[*]", "same")
			.sample();

		then(javaClass.getList()).hasSize(3);
		then(javaClass.getList()).allSatisfy(item -> then(item).isEqualTo("same"));
	}

	@Test
	void arrayElement() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.size("array", 2)
			.set("array[0]", "first")
			.sample();

		then(javaClass.getArray()[0]).isEqualTo("first");
	}

	@Test
	void nestedObjectInList() {
		JavaClass javaClass = fixtureMonkey.giveMeBuilder(JavaClass.class)
			.size("objectList", 2)
			.set("objectList[0].nestedField", "value")
			.sample();

		then(javaClass.getObjectList().get(0).getNestedField()).isEqualTo("value");
	}
}
