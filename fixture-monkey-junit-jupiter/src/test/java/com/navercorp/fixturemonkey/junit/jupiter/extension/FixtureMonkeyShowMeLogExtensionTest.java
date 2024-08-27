/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.Data;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
class FixtureMonkeyShowMeLogExtensionTest {

	private static ArbitraryBuilder<Product> actual;
	private static ListAppender<ILoggingEvent> listAppender;

	@BeforeAll
	static void setup() {
		FixtureMonkey fixtureMonkey = FixtureMonkey.create();
		actual = fixtureMonkey.giveMeBuilder(Product.class)
			.set("id", 1000L)
			.set("productName", "Book");

		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();

		listAppender = new ListAppender<>();
		listAppender.setContext(loggerContext);
		listAppender.start();

		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(listAppender);
		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.ERROR);

	}

	@AfterEach
	void tearDown() {
		listAppender.list.clear();
	}

	@Seed(1234L)
	@Test
	void testWithSeed() {
		boolean logFound = false;
		try {
			assertProductCreatedInCorrectly(actual);
		} catch (AssertionError e) {
			List<ILoggingEvent> logs = listAppender.list;
			logFound = logs.stream()
				.anyMatch(event -> event.getFormattedMessage()
					.contains("Test Method [testWithSeed] failed with seed: "));
		}
		assertTrue(logFound, "Expected log message found.");
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
	private static class Product {
		@NotNull
		private Long id;
		@NotBlank
		private String productName;
	}
}
