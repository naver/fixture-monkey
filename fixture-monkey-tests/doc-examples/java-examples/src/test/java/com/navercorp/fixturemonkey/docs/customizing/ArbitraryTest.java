package com.navercorp.fixturemonkey.docs.customizing;

import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.Value;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.Test;

class ArbitraryTest {

	@Value
	public static class Member {
		String name;
		int age;
		String email;
	}

	@Value
	public static class User {
		String username;
	}

	public enum OrderStatus {
		PENDING, PROCESSING, SHIPPED
	}

	@Value
	public static class Order {
		OrderStatus status;
	}

	@Value
	public static class Product {
		long id;
		String name;
		BigDecimal price;
		String category;
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void basicRange() {
		Member member = fixtureMonkey.giveMeBuilder(Member.class)
			.set("age", Arbitraries.integers().between(20, 30))
			.sample();

		then(member.getAge()).isBetween(20, 30);
	}

	@Test
	void stringPatterns() {
		User user = fixtureMonkey.giveMeBuilder(User.class)
			.set("username", Arbitraries.strings()
				.withCharRange('a', 'z')
				.ofMinLength(5)
				.ofMaxLength(10))
			.sample();

		then(user.getUsername()).hasSizeBetween(5, 10);
	}

	@Test
	void selectFromOptions() {
		Order order = fixtureMonkey.giveMeBuilder(Order.class)
			.set("status", Arbitraries.of(
				OrderStatus.PENDING,
				OrderStatus.PROCESSING,
				OrderStatus.SHIPPED))
			.sample();

		then(order.getStatus()).isIn(OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.SHIPPED);
	}

	@Test
	void multipleConstraints() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.set("id", Arbitraries.longs().greaterOrEqual(1000))
			.set("name", Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))
			.set("price", Arbitraries.bigDecimals()
				.between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))
			.set("category", Arbitraries.of("Electronics", "Clothing", "Books"))
			.sample();

		then(product.getId()).isGreaterThanOrEqualTo(1000);
		then(product.getCategory()).isIn("Electronics", "Clothing", "Books");
	}
}
