package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.ShowMeLog;

@ExtendWith(FixtureMonkeySeedExtension.class)
public class FixtureMonkeyShowMeLogExtensionTest {

	private static ArbitraryBuilder<Product> actual;
	private static final Logger logger = (Logger) LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);
	private ListAppender<ILoggingEvent> listAppender;

	@BeforeAll
	public static void setup() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.create();
		actual = fixtureMonkey.giveMeBuilder(Product.class)
			.set("id", 1000L)
			.set("productName", "Book");
	}

	@BeforeEach
	public void beforeEach() {
		listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);
	}

	@AfterEach
	public void afterEach() {
		logger.detachAppender(listAppender);
	}

	@ShowMeLog
	@Test
	void testWithShowMeLog() {
		boolean logFound = false;
		try {
			assertProductCreatedInCorrectly(actual);
		} catch (AssertionError e) {
			logFound = isLogFound();
		}
		assertTrue(logFound);
	}

	@Test
	void testWithoutShowMeLog() {
		boolean logFound = false;
		try {
			assertProductCreatedInCorrectly(actual);
		} catch (AssertionError e) {
			logFound = isLogFound();
		}
		assertFalse(logFound);
	}

	private boolean isLogFound() {
		return listAppender.list.stream()
			.anyMatch(event -> event.getMessage().startsWith("Test Method"));
	}

	private void assertProductCreatedInCorrectly(ArbitraryBuilder<Product> actual) {
		Product product = actual.sample();
		assertAll(
			() -> assertNotNull(product),
			() -> assertTrue(product.getId().equals(2000L)),
			() -> assertTrue(product.getProductName().equals("Computer"))
		);
	}

	@Data
	public static class Product {
		@NotNull
		private Long id;
		@NotBlank
		private String productName;
	}
}
