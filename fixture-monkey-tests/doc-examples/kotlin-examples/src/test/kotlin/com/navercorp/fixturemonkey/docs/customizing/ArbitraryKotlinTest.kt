package com.navercorp.fixturemonkey.docs.customizing

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import net.jqwik.api.Arbitraries
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ArbitraryKotlinTest {

	data class Member(
		val name: String,
		val age: Int,
		val email: String
	)

	data class User(
		val username: String
	)

	enum class OrderStatus {
		PENDING, PROCESSING, SHIPPED
	}

	data class Order(
		val status: OrderStatus
	)

	data class Product(
		val id: Long,
		val name: String,
		val price: BigDecimal,
		val category: String
	)

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.defaultNotNull(true)
		.build()

	@Test
	fun basicRange() {
		val member = fixtureMonkey.giveMeBuilder<Member>()
			.setExp(Member::age, Arbitraries.integers().between(20, 30))
			.sample()

		then(member.age).isBetween(20, 30)
	}

	@Test
	fun stringPatterns() {
		val user = fixtureMonkey.giveMeBuilder<User>()
			.setExp(User::username, Arbitraries.strings()
				.withCharRange('a', 'z')
				.ofMinLength(5)
				.ofMaxLength(10))
			.sample()

		then(user.username).hasSizeBetween(5, 10)
	}

	@Test
	fun selectFromOptions() {
		val order = fixtureMonkey.giveMeBuilder<Order>()
			.setExp(Order::status, Arbitraries.of(
				OrderStatus.PENDING,
				OrderStatus.PROCESSING,
				OrderStatus.SHIPPED))
			.sample()

		then(order.status).isIn(OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.SHIPPED)
	}

	@Test
	fun multipleConstraints() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.setExp(Product::id, Arbitraries.longs().greaterOrEqual(1000))
			.setExp(Product::name, Arbitraries.strings().withCharRange('a', 'z').ofMaxLength(10))
			.setExp(Product::price, Arbitraries.bigDecimals()
				.between(BigDecimal.valueOf(10.0), BigDecimal.valueOf(1000.0)))
			.setExp(Product::category, Arbitraries.of("Electronics", "Clothing", "Books"))
			.sample()

		then(product.id).isGreaterThanOrEqualTo(1000)
		then(product.category).isIn("Electronics", "Clothing", "Books")
	}
}
