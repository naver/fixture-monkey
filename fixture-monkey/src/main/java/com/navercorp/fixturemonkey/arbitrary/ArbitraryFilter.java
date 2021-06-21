package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;
import java.util.function.Predicate;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

@SuppressWarnings("unchecked")
public class ArbitraryFilter<T> implements PostArbitraryManipulator<T> {
	private ArbitraryExpression arbitraryExpression;
	private final Class<T> clazz;
	private final Predicate<T> filter;
	private long limit;

	public ArbitraryFilter(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter, long limit) {
		this.clazz = clazz;
		this.arbitraryExpression = arbitraryExpression;
		this.filter = filter;
		this.limit = limit;
	}

	public ArbitraryFilter(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter) {
		this(clazz, arbitraryExpression, filter, Long.MAX_VALUE);
	}

	public Class<T> getClazz() {
		return clazz;
	}

	@Override
	public Arbitrary<T> apply(Arbitrary<?> from) {
		if (this.limit > 0) {
			limit--;
			return ((Arbitrary<T>)from).filter(filter);
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
		ArbitraryFilter<?> that = (ArbitraryFilter<?>)obj;
		return clazz.equals(that.clazz)
			&& getArbitraryExpression().equals(that.getArbitraryExpression())
			&& filter.equals(that.filter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clazz, getArbitraryExpression(), filter);
	}

	@Override
	public boolean isMappableTo(ArbitraryNode<T> arbitraryNode) {
		return TypeSupports.isSameType(this.clazz, arbitraryNode.getType().getType());
	}

	@Override
	public void addPrefix(String expression) {
		arbitraryExpression = arbitraryExpression.appendLeft(expression);
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return arbitraryExpression;
	}

	@Override
	public ArbitraryFilter<T> copy() {
		return new ArbitraryFilter<>(this.clazz, this.arbitraryExpression, this.filter, this.limit);
	}
}
