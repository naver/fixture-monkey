package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public abstract class AbstractArbitrarySet<T> implements PreArbitraryManipulator<T> {
	private ArbitraryExpression arbitraryExpression;

	public AbstractArbitrarySet(ArbitraryExpression arbitraryExpression) {
		this.arbitraryExpression = arbitraryExpression;
	}

	@Override
	public void addPrefix(String expression) {
		arbitraryExpression = arbitraryExpression.appendLeft(expression);
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	public abstract Object getValue();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractArbitrarySet<?> that = (AbstractArbitrarySet<?>)obj;
		return arbitraryExpression.equals(that.arbitraryExpression);
	}

	@Override
	public final void accept(ArbitraryBuilder<T> arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(arbitraryExpression);
	}
}
