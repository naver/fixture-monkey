package com.navercorp.fixturemonkey.docs.generating;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class GeneratingInterfaceTest {

	public interface StringSupplier {
		String getValue();
	}

	public static class DefaultStringSupplier implements StringSupplier {
		private final String value;

		@ConstructorProperties("value")
		public DefaultStringSupplier(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return "default" + value;
		}
	}

	public interface ObjectValueSupplier<T> {
		T getValue();
	}

	public static class StringValueSupplier implements ObjectValueSupplier<String> {
		private final String value;

		@ConstructorProperties("value")
		public StringValueSupplier(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	public static class IntegerValueSupplier implements ObjectValueSupplier<Integer> {
		private final Integer value;

		@ConstructorProperties("value")
		public IntegerValueSupplier(Integer value) {
			this.value = value;
		}

		@Override
		public Integer getValue() {
			return value;
		}
	}

	private static FixtureMonkey anonymousFixture() {
		return FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(
				new InterfacePlugin()
					.useAnonymousArbitraryIntrospector(true)
			)
			.build();
	}

	@Test
	void quickStart() {
		// given
		FixtureMonkey fixture = anonymousFixture();

		// when
		StringSupplier supplier = fixture.giveMeOne(StringSupplier.class);

		// then
		then(supplier.getValue()).isNotNull();
	}

	@Test
	void testWithAnonymousImplementation() {
		// given
		FixtureMonkey fixture = anonymousFixture();

		// when
		StringSupplier result = fixture.giveMeOne(StringSupplier.class);

		// then
		then(result.getValue()).isNotNull();
		then(result).isNotInstanceOf(DefaultStringSupplier.class);
	}

	@Test
	void testWithCustomizedProperties() {
		// given
		FixtureMonkey fixture = anonymousFixture();

		// when
		StringSupplier result = fixture.giveMeBuilder(StringSupplier.class)
			.set("value", "customValue")
			.sample();

		// then
		then(result.getValue()).isEqualTo("customValue");
	}

	@Test
	void testWithSpecificImplementation() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(StringSupplier.class, Arrays.asList(DefaultStringSupplier.class))
			)
			.build();

		// when
		StringSupplier result = fixture.giveMeOne(StringSupplier.class);

		// then
		then(result).isInstanceOf(DefaultStringSupplier.class);
		then(result.getValue()).startsWith("default");
	}

	@Test
	void testGenericInterfaceWithoutTypeParameters() {
		// given
		FixtureMonkey fixture = anonymousFixture();

		// when
		ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);

		// then
		then(result.getValue()).isNotNull();
	}

	@Test
	void testGenericInterfaceWithTypeParameters() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(ObjectValueSupplier.class,
						Arrays.asList(IntegerValueSupplier.class))
			)
			.build();

		// when
		ObjectValueSupplier<Integer> result =
			fixture.giveMeOne(new TypeReference<ObjectValueSupplier<Integer>>() {});

		// then
		then(result.getValue()).isInstanceOf(Integer.class);
	}

	@Test
	void testGenericInterfaceWithSpecificImplementation() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(ObjectValueSupplier.class,
						Arrays.asList(StringValueSupplier.class, IntegerValueSupplier.class))
			)
			.build();

		// when
		ObjectValueSupplier<?> result = fixture.giveMeOne(ObjectValueSupplier.class);

		// then
		then(result).isNotNull();
		then(result.getValue()).isNotNull();
	}

	@Test
	void testCustomListImplementation() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(List.class, Arrays.asList(LinkedList.class))
			)
			.build();

		// when
		List<String> list = fixture.giveMeOne(new TypeReference<List<String>>() {});

		// then
		then(list).satisfiesAnyOf(
			actual -> then(actual).isInstanceOf(ArrayList.class),
			actual -> then(actual).isInstanceOf(LinkedList.class)
		);
	}

	@Test
	void testInterfaceHierarchy() {
		// given
		FixtureMonkey fixture = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(Collection.class, Arrays.asList(List.class))
			)
			.build();

		// when
		Collection<String> collection = fixture.giveMeOne(new TypeReference<Collection<String>>() {});

		// then
		then(collection).isInstanceOf(List.class);
	}
}
