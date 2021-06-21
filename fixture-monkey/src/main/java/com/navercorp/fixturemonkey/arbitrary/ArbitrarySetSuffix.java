package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public final class ArbitrarySetSuffix extends AbstractArbitrarySet<String> {
	private final Arbitrary<String> value;

	public ArbitrarySetSuffix(ArbitraryExpression fixtureExpression, Arbitrary<String> value) {
		super(fixtureExpression);
		this.value = value;
	}

	@Override
	public Arbitrary<String> getValue() {
		return value;
	}

	@Override
	public Arbitrary<String> apply(Arbitrary<?> from) {
		return Combinators.combine(value, from)
			.as((suffix, fromValue) -> {
				String arbitraryString = (String)fromValue;
				String concatString = arbitraryString + suffix;
				int remainLength = concatString.length() - suffix.length();
				return concatString.substring(Math.min(remainLength, suffix.length()));
			});
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		ArbitrarySetSuffix that = (ArbitrarySetSuffix)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}

	@Override
	public ArbitrarySetSuffix copy() {
		return new ArbitrarySetSuffix(this.getArbitraryExpression(), this.value);
	}
}
