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

import static com.navercorp.fixturemonkey.Constants.MAX_MANIPULATION_COUNT;

import java.util.Objects;
import java.util.function.Predicate;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.TypeSupports;

public final class ArbitrarySetPostCondition<T> extends AbstractArbitraryExpressionManipulator
	implements PostArbitraryManipulator<T> {
	private final Class<T> clazz;
	private final Predicate<T> filter;
	private long limit;

	public ArbitrarySetPostCondition(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter,
		long limit) {
		super(arbitraryExpression);
		this.clazz = clazz;
		this.filter = filter;
		this.limit = limit;
	}

	public ArbitrarySetPostCondition(Class<T> clazz, ArbitraryExpression arbitraryExpression, Predicate<T> filter) {
		this(clazz, arbitraryExpression, filter, MAX_MANIPULATION_COUNT);
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public Predicate<T> getFilter() {
		return filter;
	}

	@Override
	public Arbitrary<T> apply(Arbitrary<T> from) {
		if (this.limit > 0) {
			limit--;
			return from.filter(filter);
		} else {
			return from;
		}
	}

	@Override
	public boolean isMappableTo(ArbitraryNode<T> arbitraryNode) {
		Class<?> nodeClazz = arbitraryNode.getType().getType();
		return TypeSupports.isCompatibleType(this.clazz, nodeClazz)
			|| this.clazz.isAssignableFrom(nodeClazz);
	}

	@Override
	public ArbitrarySetPostCondition<T> copy() {
		return new ArbitrarySetPostCondition<>(this.clazz, this.getArbitraryExpression(), this.filter, this.limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitrarySetPostCondition<T> that = (ArbitrarySetPostCondition<T>)obj;
		return clazz.equals(that.clazz)
			&& getArbitraryExpression().equals(that.getArbitraryExpression())
			&& filter.equals(that.filter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clazz, getArbitraryExpression(), filter);
	}
}
