package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
public class FixtureMonkeySeedExtensionTest {
	private static FixtureMonkey fixtureMonkey;
	private static ArbitraryBuilder<Product> actual;

	@BeforeAll
	public static void setup() {
		fixtureMonkey = FixtureMonkey.create();

		actual = fixtureMonkey.giveMeBuilder(Product.class)
			.set("id", 1000L)
			.set("productName", "Book");
	}

	@Test
	@Seed
	void testFailWithSeed() {

		Product product = actual.sample();
		assertNotNull(product);
		assertTrue(product.getId().equals(2000L));
		assertTrue(product.getProductName().equals("Computer"));
	}

	@Test
	void testFailWithoutSeed() {
		Product product = actual.sample();
		assertNotNull(product);
		assertTrue(product.getId().equals(1000L));
		assertTrue(product.getProductName().equals("Book"));
	}

	@Data
	public static class Product {
		@NotNull
		private Long id;
		@NotBlank
		private String productName;
	}
}
