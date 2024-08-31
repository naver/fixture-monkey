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

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
class FixtureMonkeySeedExtensionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();
	private static ListAppender<ILoggingEvent> EVENT_APPENDER;

	@BeforeAll
	static void setup() {
		LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();

		EVENT_APPENDER = new ListAppender<>();
		EVENT_APPENDER.setContext(loggerContext);
		EVENT_APPENDER.start();

		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(EVENT_APPENDER);
		loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.ERROR);

	}

	@AfterEach
	void tearDown() {
		EVENT_APPENDER.list.clear();
	}

	@Seed(1234L)
	@Test
	void testWithSeed() {
		boolean logFound = false;
		try {
			Product product = SUT.giveMeBuilder(Product.class)
				.set("id", 1000L)
				.set("productName", "Book")
				.sample();

			assertAll(
				() -> assertNotNull(product),
				() -> assertEquals(2000L, (long)product.getId()),
				() -> assertEquals("Computer", product.getProductName())
			);
		} catch (AssertionError e) {
			List<ILoggingEvent> logs = EVENT_APPENDER.list;
			logFound = logs.stream()
				.anyMatch(event -> event.getFormattedMessage()
					.contains("Test Method [testWithSeed] failed with seed: "));
		}
		assertTrue(logFound, "Expected log message found.");
	}

	@Seed(1)
	@RepeatedTest(100)
	void seedReturnsSame() {
		String expected = "섨ꝓ仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨婵얎⽒竻·俌欕悳잸횑ٻ킐結";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void latterValue() {
		String expected = "聩ዡ㘇뵥刲禮ᣮ鎊熇捺셾壍Ꜻꌩ垅凗❉償粐믩࠱哠횛";
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerReturnsSame() {
		List<String> expected = Collections.singletonList("仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨婵얎⽒竻·俌欕悳잸횑ٻ킐結");

		List<String> actual = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerMattersOrder() {
		Set<String> expected = new HashSet<>(Collections.singletonList("仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨婵얎⽒竻·俌欕悳잸횑ٻ킐結"));

		Set<String> actual = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void multipleContainerReturnsDiff() {
		Set<String> firstSet = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		List<String> secondList = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(firstSet).isNotEqualTo(secondList);
	}

	@Data
	private static class Product {
		@NotNull
		private Long id;
		@NotBlank
		private String productName;
	}
}
