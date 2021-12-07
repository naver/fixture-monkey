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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.engine.jupiter.extension.FixtureMonkeySessionExtension;

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
}
