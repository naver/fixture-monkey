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

package com.navercorp.fixturemonkey.api.generator;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.exception.FilterMissException;

/**
 * An arbitrary instance for combining arbitraries in order to generate an instance of specific class.
 */
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public interface CombinableArbitrary {
	int MAX_TRIES = 1_000;

	/**
	 * Retrieves a combined object.
	 *
	 * @return a combined object
	 */
	Object combined();

	/**
	 * Retrieves a raw object.
	 * For example, a map whose keys are property names and values are property values.
	 * Caller determines how the map is converted to an instance of class.
	 *
	 * @return an raw object
	 */
	Object rawValue();

	/**
	 * Applies a given {@code predicate} as a constraint.
	 * It would repeat generation {@link #MAX_TRIES} times to satisfy the constraint.
	 * It would throw {@link FilterMissException} If repeated over {@link #MAX_TRIES} times.
	 * @param predicate a constraint to satisfy
	 * @return A filtered {@link CombinableArbitrary}.
	 */
	default CombinableArbitrary filter(Predicate<Object> predicate) {
		return new FilteredCombinableArbitrary(
			MAX_TRIES,
			this,
			predicate
		);
	}

	/**
	 * Transforms a generated object into a new object.
	 * @param mapper a way of transforming
	 * @return A mapped {@link CombinableArbitrary}
	 */
	default CombinableArbitrary map(Function<Object, Object> mapper) {
		return new MappedCombinableArbitrary(
			this,
			mapper
		);
	}

	/**
	 * Makes it return {@code null} with a {@code nullProbability}% chance.
	 * @param nullProbability a probability to be {@code null}
	 * @return A {@link CombinableArbitrary} may return {@code null}
	 */
	default CombinableArbitrary injectNull(double nullProbability) {
		return new NullInjectCombinableArbitrary(
			this,
			nullProbability
		);
	}

	/**
	 * Forces it to generate a new populated object.
	 */
	void clear();

	/**
	 * Checks if it is a fixed object.
	 * If true, {@link #clear()}} would make no change.
	 * @return fixed
	 */
	boolean fixed();
}
