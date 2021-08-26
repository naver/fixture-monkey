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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Objects;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

public class ExpressionSpecTest {
	@Property
	void copy() {
		// given
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		// when
		ExpressionSpec expected = actual.copy();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setAfterCopy() {
		// given
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		// when
		ExpressionSpec expected = actual.copy().set("test2", "test");

		then(actual).isNotEqualTo(expected);
	}

	@Property
	void merge() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.set("test", "test");
		ExpressionSpec merged = new ExpressionSpec()
			.set("test", "test2");

		// when
		ExpressionSpec actual = merger.merge(merged);

		then(actual.getBuilderManipulators()).hasSize(2);
	}

	@Property
	void mergeNotOverwrite() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.set("test", "test");
		ExpressionSpec merged = new ExpressionSpec()
			.set("test", "test2");

		// when
		ExpressionSpec actual = merger.merge(merged, false);

		then(actual.getBuilderManipulators()).hasSize(1);
	}

	@Property
	void mergePostCondition() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.setPostCondition("test", String.class, Objects::nonNull);
		ExpressionSpec merged = new ExpressionSpec()
			.setPostCondition("test", String.class, Objects::nonNull)
			.setPostCondition("test", String.class, Objects::nonNull);

		// when
		ExpressionSpec actual = merger.merge(merged);

		then(actual.getBuilderManipulators()).hasSize(2);
	}

	@Property
	void mergePostConditionNotOverwrite() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.setPostCondition("test", String.class, Objects::nonNull)
			.setPostCondition("test", String.class, Objects::nonNull);
		ExpressionSpec merged = new ExpressionSpec()
			.setPostCondition("test", String.class, Objects::nonNull);

		// when
		ExpressionSpec actual = merger.merge(merged, false);

		then(actual.getBuilderManipulators()).hasSize(2);
	}

	@Property
	void mergeNull() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.setNull("test");
		ExpressionSpec merged = new ExpressionSpec()
			.setNull("test");

		// when
		ExpressionSpec actual = merger.merge(merged);

		then(actual.getBuilderManipulators()).hasSize(2);
	}

	@Property
	void mergeNullNotOverwrite() {
		// given
		ExpressionSpec merger = new ExpressionSpec()
			.setNull("test");
		ExpressionSpec merged = new ExpressionSpec()
			.setNull("test");

		// when
		ExpressionSpec actual = merger.merge(merged, false);

		then(actual.getBuilderManipulators()).hasSize(1);
	}

	@Property
	void exclude() {
		// given
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test")
			.set("test2", "test");

		// when
		actual.exclude("test");

		then(actual.getBuilderManipulators()).hasSize(1);
	}

	@Property
	void hasPostCondition() {
		// when
		ExpressionSpec actual = new ExpressionSpec()
			.setPostCondition("test", String.class, Objects::nonNull);

		then(actual.hasSet("test")).isFalse();
		then(actual.hasPostCondition("test")).isTrue();
	}

	@Property
	void hasSet() {
		// when
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		then(actual.hasPostCondition("test")).isFalse();
		then(actual.hasSet("test")).isTrue();
	}

	@Property
	void findSetValue() {
		// when
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		//noinspection OptionalGetWithoutIsPresent
		then(actual.findSetValue("test").get()).isEqualTo("test");
	}

	@Property
	void findSetArbitraryValue() {
		// given
		Arbitrary<String> arbitrary = Arbitraries.of("test");

		// when
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", arbitrary);

		//noinspection OptionalGetWithoutIsPresent
		then(actual.findSetValue("test").get()).isEqualTo(arbitrary);
	}

	@Property
	void findSetArbitraryEmpty() {
		// when
		ExpressionSpec actual = new ExpressionSpec();

		then(actual.findSetValue("test")).isEmpty();
	}
}
