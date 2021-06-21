package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public final class ArbitrarySet<T> extends AbstractArbitrarySet<T> {
	private final T value;
	private long limit;

	public ArbitrarySet(ArbitraryExpression arbitraryExpression, T value, long limit) {
		super(arbitraryExpression);
		this.value = value;
		this.limit = limit;
	}

	public ArbitrarySet(ArbitraryExpression arbitraryExpression, T value) {
		this(arbitraryExpression, value, Long.MAX_VALUE);
	}

	@Override
	public T getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> apply(Arbitrary<?> from) {
		if (this.limit > 0) {
			limit--;
			return Arbitraries.just(value);
		} else {
			return (Arbitrary<T>)from;
		}
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
		ArbitrarySet<?> that = (ArbitrarySet<?>)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}

	@Override
	public ArbitrarySet<T> copy() {
		return new ArbitrarySet<>(this.getArbitraryExpression(), this.value, this.limit);
	}
}
