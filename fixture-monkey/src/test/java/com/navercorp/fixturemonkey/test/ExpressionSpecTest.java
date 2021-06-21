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

import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;

public class ExpressionSpecTest {
	@Property
	void copy() {
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		ExpressionSpec expected = actual.copy();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setAfterCopy() {
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		ExpressionSpec expected = actual.copy().set("test2", "test");

		then(actual).isNotEqualTo(expected);
	}

	@Property
	void merge() {
		ExpressionSpec merger = new ExpressionSpec()
			.set("test", "test");
		ExpressionSpec merged = new ExpressionSpec()
			.set("test", "test2");

		ExpressionSpec actual = merger.merge(merged);

		then(actual.getPreArbitraryManipulators()).hasSize(2);
	}

	@Property
	void mergeNotOverwrite() {
		ExpressionSpec merger = new ExpressionSpec()
			.set("test", "test");
		ExpressionSpec merged = new ExpressionSpec()
			.set("test", "test2");

		ExpressionSpec actual = merger.merge(merged, false);

		then(actual.getPreArbitraryManipulators()).hasSize(1);
	}

	@Property
	void mergeFilter() {
		ExpressionSpec merger = new ExpressionSpec()
			.filter("test", Objects::nonNull);
		ExpressionSpec merged = new ExpressionSpec()
			.filter("test", Objects::nonNull)
			.filter("test", Objects::nonNull);

		ExpressionSpec actual = merger.merge(merged);

		then(actual.getPostArbitraryManipulators()).hasSize(2);
	}

	@Property
	void mergeFilterNotOverwrite() {
		ExpressionSpec merger = new ExpressionSpec()
			.filter("test", Objects::nonNull)
			.filter("test", Objects::nonNull);
		ExpressionSpec merged = new ExpressionSpec()
			.filter("test", Objects::nonNull);

		ExpressionSpec actual = merger.merge(merged, false);

		then(actual.getPostArbitraryManipulators()).hasSize(2);
	}

	@Property
	void exclude() {
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test")
			.set("test2", "test");

		actual.exclude("test");

		then(actual.getPreArbitraryManipulators()).hasSize(1);
		then(actual.getPreArbitraryManipulators().get(0).getArbitraryExpression())
			.isEqualTo(ArbitraryExpression.from("test2"));
	}

	@Property
	void hasFilter() {
		ExpressionSpec actual = new ExpressionSpec()
			.filter("test", Objects::nonNull);

		then(actual.hasSet("test")).isFalse();
		then(actual.hasFilter("test")).isTrue();
	}

	@Property
	void hasSet() {
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		then(actual.hasFilter("test")).isFalse();
		then(actual.hasSet("test")).isTrue();
	}

	@Property
	void findSetValue() {
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", "test");

		//noinspection OptionalGetWithoutIsPresent
		then(actual.findSetValue("test").get()).isEqualTo("test");
	}

	@Property
	void findSetArbitraryValue() {
		Arbitrary<String> arbitrary = Arbitraries.of("test");
		ExpressionSpec actual = new ExpressionSpec()
			.set("test", arbitrary);

		//noinspection OptionalGetWithoutIsPresent
		then(actual.findSetValue("test").get()).isEqualTo(arbitrary);
	}

	@Property
	void findSetArbitraryEmpty() {
		ExpressionSpec actual = new ExpressionSpec();

		then(actual.findSetValue("test")).isEmpty();
	}
}
