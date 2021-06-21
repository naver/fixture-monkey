package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public final class ArbitrarySetPrefix extends AbstractArbitrarySet<String> {
	private final Arbitrary<String> value;

	public ArbitrarySetPrefix(ArbitraryExpression fixtureExpression, Arbitrary<String> value) {
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
			.as((prefix, fromValue) -> {
					String arbitraryString = (String)fromValue;
					String concatString = prefix + arbitraryString;
					int remainLength = concatString.length() - prefix.length();
					return concatString.substring(0, Math.max(prefix.length(), remainLength));
				}
			);
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
		ArbitrarySetPrefix that = (ArbitrarySetPrefix)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}

	@Override
	public ArbitrarySetPrefix copy(){
		return new ArbitrarySetPrefix(this.getArbitraryExpression(), this.value);
	}
}
