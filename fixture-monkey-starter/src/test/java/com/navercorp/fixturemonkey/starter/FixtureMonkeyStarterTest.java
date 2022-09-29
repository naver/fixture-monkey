package com.navercorp.fixturemonkey.starter;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.engine.jupiter.extension.FixtureMonkeySessionExtension;
import com.navercorp.fixturemonkey.report.DebugInfoObserver;

@ExtendWith(FixtureMonkeySessionExtension.class)
class FixtureMonkeyStarterTest {
	@Data   // lombok getter, setter
	public static class Order {
		@NotNull
		private Long id;

		@NotBlank
		private String orderNo;

		@Size(min = 2, max = 10)
		private String productName;

		@Min(1)
		@Max(100)
		private int quantity;

		@Min(0)
		private long price;

		@Size(max = 3)
		private List<@NotBlank @Size(max = 10) String> items = new ArrayList<>();

		@PastOrPresent
		private Instant orderedAt;

		@Email
		private String sellerEmail;
	}

	@Test
	void test() {
		// given
		FixtureMonkey sut = FixtureMonkey.create();

		// when
		Order actual = sut.giveMeOne(Order.class);

		// then
		then(actual.getId()).isNotNull();
		then(actual.getOrderNo()).isNotBlank();
		then(actual.getQuantity()).isBetween(1, 100);
		then(actual.getPrice()).isGreaterThanOrEqualTo(0L);
	}

	private DebugInfoObserver debugInfoObserver;

	@BeforeEach
	void beforeEach() {
		debugInfoObserver = new DebugInfoObserver();
		DebugInfoObserver.INSTANCE = debugInfoObserver;
	}

	@Test
	void test2() {
		// given
		LabMonkey sut = LabMonkey.create();
		ArbitraryBuilder<Order> builder = sut.giveMeBuilder(Order.class);

		// when
		Order actual = builder
			.set("productName", "actual1")
			.size("items", 2)
			.sample();

		// Order actual2 = sut.giveMeBuilder(Order.class)
		// 	.set("productName", "actual2")
		// 	.size("items", 1)
		// 	.sample();
		//
		// Order actual3 = builder
		// 	.set("productName", "actual3")
		// 	.size("items", 1)
		// 	.sample();
	}

	@AfterEach
	void afterEach() {
		debugInfoObserver.reportResult();
	}
}
