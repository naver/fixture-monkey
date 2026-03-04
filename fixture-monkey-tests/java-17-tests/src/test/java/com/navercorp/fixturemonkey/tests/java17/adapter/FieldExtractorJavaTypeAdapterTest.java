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

package com.navercorp.fixturemonkey.tests.java17.adapter;

import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;

/**
 * Tests that FieldExtractor does not call setAccessible on Java standard library types
 * (e.g., LocalDate) which throws InaccessibleObjectException on Java 17+.
 *
 * <p>This reproduces the issue where hasContainerFieldExpandedInTree calls
 * FieldExtractor.reflection().extractFields() with a decomposed LocalDate value,
 * triggering extractFieldsRecursively(LocalDate.class, ...) which attempts
 * setAccessible(true) on LocalDate's private fields.
 */
class FieldExtractorJavaTypeAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	/**
	 * Reproduces: thenApply sets root object AND expands a container field.
	 * During assembly, the root is decomposed into field values (including LocalDate).
	 * When assembling the LocalDate child node, hasContainerFieldExpandedInTree is called
	 * with the LocalDate value, which triggers extractFieldsRecursively(LocalDate.class).
	 * On Java 17+, this throws InaccessibleObjectException.
	 */
	@RepeatedTest(10)
	void thenApplyWithJavaStandardTypeFieldShouldNotThrow() {
		thenNoException().isThrownBy(() ->
			SUT.giveMeBuilder(ObjectWithLocalDateAndList.class)
				.thenApply((obj, builder) -> {
					builder.set("$", obj);
					builder.size("values", obj.getValues().size() + 1);
				})
				.sample()
		);
	}

	public static class ObjectWithLocalDateAndList {

		private LocalDate localDate;
		private List<String> values;

		public LocalDate getLocalDate() {
			return localDate;
		}

		public void setLocalDate(LocalDate localDate) {
			this.localDate = localDate;
		}

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}
}
