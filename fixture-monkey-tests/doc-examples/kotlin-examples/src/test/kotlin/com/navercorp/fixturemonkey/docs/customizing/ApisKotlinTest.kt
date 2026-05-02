package com.navercorp.fixturemonkey.docs.customizing

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.customizer.InnerSpec
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

class ApisKotlinTest {

	data class Member(
		val name: String,
		val age: Int,
		val email: String?,
		val grade: String,
		val point: Int,
		val joinDate: LocalDate
	)

	data class Order(
		val orderId: String,
		val totalAmount: BigDecimal,
		val orderDate: LocalDate,
		val items: List<OrderItem>,
		val customer: Customer
	)

	data class OrderItem(
		val name: String,
		val price: BigDecimal
	)

	data class Customer(
		val name: String
	)

	data class Cart(
		val items: List<CartItem>
	)

	data class CartItem(
		val name: String,
		val price: BigDecimal,
		val onSale: Boolean
	)

	data class Product(
		val name: String,
		val reviews: List<String>
	)

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.defaultNotNull(true)
		.build()

	@Test
	fun setBasicUsage() {
		val member = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.setExp(Member::name, "John Doe")
			.setExp(Member::age, 25)
			.setExp(Member::email, "john@test.com")
			.sample()

		val order = fixtureMonkey.giveMeKotlinBuilder<Order>()
			.setExp(Order::orderId, "ORDER-001")
			.setExp(Order::totalAmount, BigDecimal.valueOf(15000))
			.sample()

		then(member.name).isEqualTo("John Doe")
		then(order.orderId).isEqualTo("ORDER-001")
	}

	@Test
	fun sizeBasicUsage() {
		val cart = fixtureMonkey.giveMeKotlinBuilder<Cart>()
			.sizeExp(Cart::items, 3)
			.sample()

		val product = fixtureMonkey.giveMeKotlinBuilder<Product>()
			.sizeExp(Product::reviews, 2, 4)
			.sample()

		then(cart.items).hasSize(3)
		then(product.reviews.size).isBetween(2, 4)
	}

	@Test
	fun setNullBasicUsage() {
		val withdrawnMember = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.setExp(Member::name, "John Doe")
			.setNullExp(Member::email)
			.sample()

		val validOrder = fixtureMonkey.giveMeKotlinBuilder<Order>()
			.setNotNullExp(Order::orderId)
			.setNotNullExp(Order::orderDate)
			.sample()

		then(withdrawnMember.email).isNull()
		then(validOrder.orderId).isNotNull
	}

	@Test
	fun setInnerBasicUsage() {
		val vipMemberSpec = InnerSpec()
			.property("grade", "VIP")
			.property("point", 10000)
			.property("joinDate", LocalDate.now().minusYears(1))

		val vipMember = fixtureMonkey.giveMeBuilder<Member>()
			.setInner(vipMemberSpec)
			.sample()

		then(vipMember.grade).isEqualTo("VIP")
		then(vipMember.point).isEqualTo(10000)
	}

	@Test
	fun setLazyBasicUsage() {
		val orderCounter = AtomicInteger(1)
		val order = fixtureMonkey.giveMeBuilder<Order>()
			.setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
			.sample()

		val nextOrder = fixtureMonkey.giveMeBuilder<Order>()
			.setLazy("orderId") { "ORDER-${orderCounter.getAndIncrement()}" }
			.sample()

		then(order.orderId).isEqualTo("ORDER-1")
		then(nextOrder.orderId).isEqualTo("ORDER-2")
	}

	@Test
	fun setPostConditionBasicUsage() {
		val adultMember = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.setPostConditionExp<Int>(Member::age) { it >= 19 }
			.sample()

		val largeOrder = fixtureMonkey.giveMeKotlinBuilder<Order>()
			.setPostConditionExp<BigDecimal>(Order::totalAmount) {
				it >= BigDecimal.valueOf(100000)
			}
			.sample()

		then(adultMember.age).isGreaterThanOrEqualTo(19)
		then(largeOrder.totalAmount).isGreaterThanOrEqualTo(BigDecimal.valueOf(100000))
	}

	@Test
	fun fixedBasicUsage() {
		val memberBuilder = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.setExp(Member::name, "John Doe")
			.setExp(Member::age, 30)
			.fixed()

		val member1 = memberBuilder.sample()
		val member2 = memberBuilder.sample()

		then(member1.name).isEqualTo(member2.name)
		then(member1.age).isEqualTo(member2.age)
	}

	@Test
	fun limitBasicUsage() {
		val cart = fixtureMonkey.giveMeKotlinBuilder<Cart>()
			.sizeExp(Cart::items, 5)
			.set("items[*].onSale", true, 2)
			.sample()

		then(cart.items).hasSize(5)
	}

	@Test
	fun thenApplyBasicUsage() {
		val order = fixtureMonkey.giveMeKotlinBuilder<Order>()
			.sizeExp(Order::items, 3)
			.thenApply { tempOrder, orderBuilder ->
				val total = tempOrder.items
					.map { it.price }
					.fold(BigDecimal.ZERO, BigDecimal::add)
				orderBuilder.set("totalAmount", total)
			}
			.sample()

		then(order.items).hasSize(3)
		then(order.totalAmount).isNotNull
	}

	@Test
	fun customizePropertyTransform() {
		val expected = "test"
		val actual = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.customizeProperty(Member::name) {
				it.map { _ -> expected }
			}
			.sample()
			.name

		then(actual).isEqualTo(expected)
	}

	@Test
	fun customizePropertyFilter() {
		val adult = fixtureMonkey.giveMeKotlinBuilder<Member>()
			.customizeProperty(Member::age) { arb ->
				arb.filter { age -> age >= 18 }
			}
			.sample()

		then(adult.age).isGreaterThanOrEqualTo(18)
	}
}
