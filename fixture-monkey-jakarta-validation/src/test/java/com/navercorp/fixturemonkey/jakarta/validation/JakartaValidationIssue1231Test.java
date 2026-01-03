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

package com.navercorp.fixturemonkey.jakarta.validation;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;

import net.jqwik.api.Property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;

class JakartaValidationIssue1231Test {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JakartaValidationPlugin())
		.nullableContainer(false)
		.nullableElement(false)
		.build();

	@Property(tries = 1)
	void testElementNotEmpty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(SimpleObject.class)
				.size("notEmptyElement", 1)
				.setNull("notEmptyElement[0]")
				.sample()
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@Property(tries = 1)
	void notNullElementShouldFailWhenNullIsForced() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(SimpleObject.class)
				.size("notNullElement", 1)
				.setNull("notNullElement[0]")
				.sample()
		).getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@Property(tries = 1)
	void notBlankElementShouldFailWhenNullIsForced() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(SimpleObject.class)
				.size("notBlankElement", 1)
				.setNull("notBlankElement[0]")
				.sample()
		).getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@Property(tries = 1)
	void notEmptyElementShouldFailWhenNullIsForced() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(SimpleObject.class)
				.size("notEmptyElement", 1)
				.setNull("notEmptyElement[0]")
				.sample()
		).getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@Property(tries = 1)
	void retryableExceptionHasValidationRootCause() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(SimpleObject.class)
				.size("notNullElement", 1)
				.setNull("notNullElement[0]")
				.sample()
		)
			.hasCauseInstanceOf(RetryableFilterMissException.class);
//			.hasMessage("Given properties \"notNullElement[0]\" is not validated by annotations.");
	}

	@Property(tries = 1)
	void notBlankFieldConstraintFailsOnBlankValue() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(FieldConstraintObject.class)
				.set("notBlankField", "   ")
				.sample()
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class)
			.hasMessageContaining("notBlankField")
			.hasRootCauseInstanceOf(ValidationFailedException.class);
	}

	@Property(tries = 1)
	void notEmptyFieldConstraintFailsOnEmptyValue() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(FieldConstraintObject.class)
				.set("notEmptyField", "")
				.sample()
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class)
			.hasMessageContaining("notEmptyField")
			.hasRootCauseInstanceOf(ValidationFailedException.class);
		//			.hasRootCauseMessage("Given value is empty but annotated with @NotEmpty.");
	}

	public static class SimpleObject {
		@NotEmpty
		private List<@NotEmpty String> notEmptyElement;

		@NotEmpty
		private List<@NotNull String> notNullElement;

		@NotEmpty
		private List<@NotBlank String> notBlankElement;

		public List<String> getNotEmptyElement() {
			return notEmptyElement;
		}

		public void setNotEmptyElement(List<String> notEmptyElement) {
			this.notEmptyElement = notEmptyElement;
		}

		public List<String> getNotNullElement() {
			return notNullElement;
		}

		public void setNotNullElement(List<String> notNullElement) {
			this.notNullElement = notNullElement;
		}

		public List<String> getNotBlankElement() {
			return notBlankElement;
		}

		public void setNotBlankElement(List<String> notBlankElement) {
			this.notBlankElement = notBlankElement;
		}
	}

	public static class FieldConstraintObject {
		@NotBlank
		private String notBlankField;

		@NotEmpty
		private String notEmptyField;

		public String getNotBlankField() {
			return notBlankField;
		}

		public void setNotBlankField(String notBlankField) {
			this.notBlankField = notBlankField;
		}

		public String getNotEmptyField() {
			return notEmptyField;
		}

		public void setNotEmptyField(String notEmptyField) {
			this.notEmptyField = notEmptyField;
		}
	}
}
