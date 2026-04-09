package com.navercorp.fixturemonkey.docs.customizing;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import lombok.Value;
import org.junit.jupiter.api.Test;

class ApisTest {

	@Value
	public static class Member {
		String name;
		int age;
		String email;
		String grade;
		int point;
		LocalDate joinDate;
	}

	@Value
	public static class Order {
		String orderId;
		BigDecimal totalAmount;
		LocalDate orderDate;
		List<OrderItem> items;
		Customer customer;
	}

	@Value
	public static class OrderItem {
		String name;
		BigDecimal price;
	}

	@Value
	public static class Customer {
		String name;
	}

	@Value
	public static class Cart {
		List<CartItem> items;
	}

	@Value
	public static class CartItem {
		String name;
		BigDecimal price;
		boolean onSale;
	}

	@Value
	public static class Product {
		String name;
		List<String> reviews;
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setBasicUsage() {
		Member member = fixtureMonkey.giveMeBuilder(Member.class)
			.set("name", "John Doe")
			.set("age", 25)
			.set("email", "john@test.com")
			.sample();

		Order order = fixtureMonkey.giveMeBuilder(Order.class)
			.set("orderId", "ORDER-001")
			.set("totalAmount", BigDecimal.valueOf(15000))
			.sample();

		then(member.getName()).isEqualTo("John Doe");
		then(order.getOrderId()).isEqualTo("ORDER-001");
	}

	@Test
	void sizeBasicUsage() {
		Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
			.size("items", 3)
			.sample();

		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.size("reviews", 2, 4)
			.sample();

		then(cart.getItems()).hasSize(3);
		then(product.getReviews().size()).isBetween(2, 4);
	}

	@Test
	void setNullBasicUsage() {
		Member withdrawnMember = fixtureMonkey.giveMeBuilder(Member.class)
			.set("name", "John Doe")
			.setNull("email")
			.sample();

		Order validOrder = fixtureMonkey.giveMeBuilder(Order.class)
			.setNotNull("orderId")
			.setNotNull("orderDate")
			.sample();

		then(withdrawnMember.getEmail()).isNull();
		then(validOrder.getOrderId()).isNotNull();
	}

	@Test
	void setInnerBasicUsage() {
		InnerSpec vipMemberSpec = new InnerSpec()
			.property("grade", "VIP")
			.property("point", 10000)
			.property("joinDate", LocalDate.now().minusYears(1));

		Member vipMember = fixtureMonkey.giveMeBuilder(Member.class)
			.setInner(vipMemberSpec)
			.sample();

		then(vipMember.getGrade()).isEqualTo("VIP");
		then(vipMember.getPoint()).isEqualTo(10000);
	}

	@Test
	void setLazyBasicUsage() {
		AtomicInteger orderCounter = new AtomicInteger(1);
		Order order = fixtureMonkey.giveMeBuilder(Order.class)
			.setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
			.sample();

		Order nextOrder = fixtureMonkey.giveMeBuilder(Order.class)
			.setLazy("orderId", () -> "ORDER-" + orderCounter.getAndIncrement())
			.sample();

		then(order.getOrderId()).isEqualTo("ORDER-1");
		then(nextOrder.getOrderId()).isEqualTo("ORDER-2");
	}

	@Test
	void setPostConditionBasicUsage() {
		Member adultMember = fixtureMonkey.giveMeBuilder(Member.class)
			.setPostCondition("age", Integer.class, age -> age >= 19)
			.sample();

		Order largeOrder = fixtureMonkey.giveMeBuilder(Order.class)
			.setPostCondition("totalAmount", BigDecimal.class,
				amount -> amount.compareTo(BigDecimal.valueOf(100000)) >= 0)
			.sample();

		then(adultMember.getAge()).isGreaterThanOrEqualTo(19);
		then(largeOrder.getTotalAmount()).isGreaterThanOrEqualTo(BigDecimal.valueOf(100000));
	}

	@Test
	void fixedBasicUsage() {
		ArbitraryBuilder<Member> memberBuilder = fixtureMonkey.giveMeBuilder(Member.class)
			.set("name", "John Doe")
			.set("age", 30)
			.fixed();

		Member member1 = memberBuilder.sample();
		Member member2 = memberBuilder.sample();

		then(member1.getName()).isEqualTo(member2.getName());
		then(member1.getAge()).isEqualTo(member2.getAge());
	}

	@Test
	void limitBasicUsage() {
		Cart cart = fixtureMonkey.giveMeBuilder(Cart.class)
			.size("items", 5)
			.set("items[*].onSale", true, 2)
			.sample();

		then(cart.getItems()).hasSize(5);
	}

	@Test
	void thenApplyBasicUsage() {
		Order order = fixtureMonkey.giveMeBuilder(Order.class)
			.size("items", 3)
			.thenApply((tempOrder, orderBuilder) -> {
				BigDecimal total = tempOrder.getItems().stream()
					.map(item -> item.getPrice())
					.reduce(BigDecimal.ZERO, BigDecimal::add);
				orderBuilder.set("totalAmount", total);
			})
			.sample();

		then(order.getItems()).hasSize(3);
		then(order.getTotalAmount()).isNotNull();
	}

	@Test
	void customizePropertyTransform() {
		String expected = "transformed";
		String actual = fixtureMonkey.giveMeBuilder(Member.class)
			.customizeProperty(javaGetter(Member::getName), arb -> arb.map(name -> expected))
			.sample()
			.getName();

		then(actual).isEqualTo(expected);
	}

	@Test
	void customizePropertyFilter() {
		Member adult = fixtureMonkey.giveMeBuilder(Member.class)
			.customizeProperty(javaGetter(Member::getAge), arb -> arb.filter(age -> age >= 18))
			.sample();

		then(adult.getAge()).isGreaterThanOrEqualTo(18);
	}

	@Test
	void customizePropertyNested() {
		String nestedValue = fixtureMonkey.giveMeBuilder(Order.class)
			.customizeProperty(
				javaGetter(Order::getCustomer).into(Customer::getName),
				arb -> arb.map(name -> "Mr. " + name)
			)
			.sample()
			.getCustomer()
			.getName();

		then(nestedValue).startsWith("Mr. ");
	}
}
