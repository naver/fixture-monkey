package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
public class FixtureMonkeyShowMeLogExtensionTest {

	private static ArbitraryBuilder<Product> actual;
	private TestAppender testAppender;

	@BeforeAll
	public static void setup() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.create();
		actual = fixtureMonkey.giveMeBuilder(Product.class)
			.set("id", 1000L)
			.set("productName", "Book");

	}

	@BeforeEach
	void logSetup() {
		Logger logger = (Logger)LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);
		testAppender = new TestAppender();
		logger.addAppender(testAppender);
		logger.setAdditive(true);
	}

	@Seed(1234L)
	@Test
	void testWithShowMeLog() throws Exception {
		boolean logFound = false;
		try {
			assertProductCreatedInCorrectly(actual);
		} catch (AssertionError e) {
			logFound = testAppender.getLogEvents().stream()
				.anyMatch(event -> event.getFormattedMessage()
					.startsWith("Test Method [testMethodShouldFailAndLogSeed] failed with seed: "));
		}
		assertTrue(logFound);
	}

	private static class TestAppender extends AppenderBase<ILoggingEvent> {
		private final List<ILoggingEvent> logEvents = new ArrayList<>();

		@Override
		protected void append(ILoggingEvent eventObject) {
			logEvents.add(eventObject);
		}

		public List<ILoggingEvent> getLogEvents() {
			return new ArrayList<>(logEvents);
		}
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
