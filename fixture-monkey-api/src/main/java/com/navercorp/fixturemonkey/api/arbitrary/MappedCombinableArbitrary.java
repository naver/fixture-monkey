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

package com.navercorp.fixturemonkey.api.arbitrary;

import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Traceable;

/**
 * It would transform a generated object into a new object.
 */
@API(since = "0.5.0", status = Status.MAINTAINED)
final class MappedCombinableArbitrary<T, U> implements CombinableArbitrary<U> {
	private final CombinableArbitrary<T> combinableArbitrary;
	private final Function<T, U> mapper;

	MappedCombinableArbitrary(CombinableArbitrary<T> combinableArbitrary, Function<T, U> mapper) {
		this.combinableArbitrary = combinableArbitrary;
		this.mapper = mapper;
	}

	@Override
	public U combined() {
		return mapper.apply(combinableArbitrary.combined());
	}

	@SuppressWarnings({"unchecked", "return"})
	@Override
	public Object rawValue() {
		try {
			return mapper.apply((T)combinableArbitrary.rawValue());
		} catch (ClassCastException ex) {
			if (combinableArbitrary instanceof Traceable) {
				throw new ClassCastException(
					String.format(
						"Given property '%s' could not use mapper. Check out if using the proper introspector.",
						((Traceable)combinableArbitrary).getPropertyPath().getExpression()
					)
				);
			}
			throw new ClassCastException("Could not use mapper. Check out if using the proper introspector.");
		}
	}

	@Override
	public void clear() {
		combinableArbitrary.clear();
	}

	@Override
	public boolean fixed() {
		return combinableArbitrary.fixed();
	}
}
