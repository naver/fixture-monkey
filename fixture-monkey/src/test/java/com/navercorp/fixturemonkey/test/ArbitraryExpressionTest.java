package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.expression.ArbitraryExpression;

public class ArbitraryExpressionTest {
	@Test
	void appendLeft() {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("fixturemonkey");

		ArbitraryExpression actual = arbitraryExpression.addFirst("navercorp");

		then(actual.toString()).isEqualTo("navercorp.fixturemonkey");
	}

	@Test
	void appendRight() {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("navercorp");

		ArbitraryExpression actual = arbitraryExpression.addLast("fixturemonkey");

		then(actual.toString()).isEqualTo("navercorp.fixturemonkey");
	}

	@Test
	void popRight() {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("navercorp.fixturemonkey");

		ArbitraryExpression actual = arbitraryExpression.pollLast();

		then(actual.toString()).isEqualTo("navercorp");
	}

	@Test
	void popRightWhenEmpty() {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("");

		ArbitraryExpression actual = arbitraryExpression.pollLast();

		then(actual.toString()).isEqualTo("");
	}

	@Test
	void popRightNotAffectsOrigin() {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from("navercorp.fixturemonkey");

		arbitraryExpression.pollLast();

		then(arbitraryExpression.toString()).isEqualTo("navercorp.fixturemonkey");
	}
}
