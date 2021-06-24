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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;
import java.util.function.Predicate;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

@SuppressWarnings("unchecked")
public final class ArbitraryFilter<T> implements PostArbitraryManipulator<T> {
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
}
