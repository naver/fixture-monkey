package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public class ContainerSizeManipulator implements MetadataManipulator {
	private ArbitraryExpression arbitraryExpression;
	private final int min;
	private final int max;

	public ContainerSizeManipulator(ArbitraryExpression arbitraryExpression, int min, int max) {
		this.arbitraryExpression = arbitraryExpression;
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	@Override
	public void addPrefix(String expression) {
		arbitraryExpression = arbitraryExpression.appendLeft(expression);
	}

	@Override
	public void accept(ArbitraryBuilder<?> arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ContainerSizeManipulator that = (ContainerSizeManipulator)obj;
		return min == that.min
			&& max == that.max
			&& Objects.equals(arbitraryExpression, that.arbitraryExpression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(arbitraryExpression, min, max);
	}

	@Override
	public Priority getPriority() {
		return Priority.HIGH;
	}

	@Override
	public ContainerSizeManipulator copy() {
		return new ContainerSizeManipulator(this.arbitraryExpression, this.min, this.max);
	}
}
