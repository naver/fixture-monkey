package com.navercorp.fixturemonkey.docs.generating;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;

import java.util.Arrays;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import lombok.Value;
import org.junit.jupiter.api.Test;

class GeneratingComplexTypesTest {

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@Value
	public static class GenericObject<T> {
		T foo;
	}

	@Value
	public static class GenericArrayObject<T> {
		GenericObject<T>[] foo;
	}

	@Value
	public static class TwoGenericObject<T, U> {
		T foo;
		U bar;
	}

	@Value
	public static class ThreeGenericObject<T, U, V> {
		T foo;
		U bar;
		V baz;
	}

	public interface GenericInterface<T> {
	}

	@Value
	public static class GenericInterfaceImpl<T> implements GenericInterface<T> {
		T foo;
	}

	public interface TwoGenericInterface<T, U> {
	}

	@Value
	public static class TwoGenericImpl<T, U> implements TwoGenericInterface<T, U> {
		T foo;
		U bar;
	}

	public interface PaymentProcessor {
		void processPayment(double amount);
	}

	public static class CreditCardProcessor implements PaymentProcessor {
		@Override
		public void processPayment(double amount) {
		}
	}

	public static class BankTransferProcessor implements PaymentProcessor {
		@Override
		public void processPayment(double amount) {
		}
	}

	@Value
	public static class SelfReference {
		String foo;
		SelfReference bar;
	}

	@Value
	public static class SelfReferenceList {
		String foo;
		List<SelfReferenceList> bar;
	}

	public interface Interface {
		String foo();

		Integer bar();
	}

	public interface InheritedInterface extends Interface {
		String foo();
	}

	public interface InheritedInterfaceWithSameNameMethod extends Interface {
		String foo();
	}

	public interface ContainerInterface {
		List<String> baz();

		Map<String, Integer> qux();
	}

	public interface InheritedTwoInterface extends Interface, ContainerInterface {
	}

	@Test
	void generateGenericTypes() {
		// given
		GenericObject<String> stringGeneric = fixtureMonkey.giveMeOne(
			new TypeReference<GenericObject<String>>() {}
		);

		GenericArrayObject<Integer> arrayGeneric = fixtureMonkey.giveMeOne(
			new TypeReference<GenericArrayObject<Integer>>() {}
		);

		TwoGenericObject<String, Integer> twoParamGeneric = fixtureMonkey.giveMeOne(
			new TypeReference<TwoGenericObject<String, Integer>>() {}
		);

		// then
		then(stringGeneric).isNotNull();
		then(arrayGeneric).isNotNull();
		then(twoParamGeneric).isNotNull();
	}

	@Test
	void generateGenericInterface() {
		// given
		FixtureMonkey fm = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GenericInterface.class, Arrays.asList(GenericInterfaceImpl.class))
			)
			.build();

		// when
		GenericInterface<String> genericInterface = fm.giveMeOne(
			new TypeReference<GenericInterface<String>>() {}
		);

		// then
		then(genericInterface).isNotNull();
	}

	@Test
	void generateSelfReference() {
		// when
		SelfReference selfRef = fixtureMonkey.giveMeOne(SelfReference.class);

		// then
		then(selfRef).isNotNull();
	}

	@Test
	void generateSelfReferenceWithContainerInfo() {
		// given
		FixtureMonkey customFixture = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(2, 2))
			.build();

		// when
		SelfReferenceList refList = customFixture.giveMeOne(SelfReferenceList.class);

		// then
		then(refList).isNotNull();
	}

	@Test
	void generateSelfReferenceList() {
		// when
		SelfReferenceList refList = fixtureMonkey.giveMeOne(SelfReferenceList.class);

		// then
		then(refList).isNotNull();
	}
}
