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

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.TooManyFilterMissesException;

import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.Traceable;

/**
 * It would generate an object satisfied given {@code predicate}.
 * It would try {@code maxMisses} times, {@code maxMisses} is 1000 in default.
 */
@API(since = "0.5.0", status = Status.MAINTAINED)
final class FilteredCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final int maxMisses;
	private final CombinableArbitrary<T> combinableArbitrary;
	private final Predicate<T> predicate;

	private Exception lastException;

	FilteredCombinableArbitrary(
		int maxMisses,
		CombinableArbitrary<T> combinableArbitrary,
		Predicate<T> predicate
	) {
		this.maxMisses = maxMisses;
		this.combinableArbitrary = combinableArbitrary;
		this.predicate = predicate;
	}

	FilteredCombinableArbitrary(
		int maxMisses,
		FilteredCombinableArbitrary<T> filteredCombinableArbitrary,
		Predicate<T> predicate
	) {
		this.maxMisses = maxMisses;
		this.combinableArbitrary = filteredCombinableArbitrary.combinableArbitrary;
		this.predicate = predicate.and(filteredCombinableArbitrary.predicate);
	}

	@Override
	public T combined() {
		T returned;
		for (int i = 0; i < maxMisses; i++) {
			try {
				returned = combinableArbitrary.combined();
				if (predicate.test(returned)) {
					return returned;
				}

				if (fixed()) {
					throw new FixedValueFilterMissException("Fixed value can not satisfy given filter.");
				}
			} catch (TooManyFilterMissesException | ValidationFailedException | RetryableFilterMissException ex) {
				if (lastException == null || ex.getCause() != null) {
					lastException = ex;
				}
				combinableArbitrary.clear();
			}
		}

		if (lastException == null && combinableArbitrary instanceof FilteredCombinableArbitrary) {
			lastException = ((FilteredCombinableArbitrary<T>)combinableArbitrary).lastException;
		}

		if (lastException instanceof ValidationFailedException) {
			String failedConcatProperties = String.join(", ",
				((ValidationFailedException)lastException).getConstraintViolationPropertyNames());

			throw new RetryableFilterMissException(
				String.format("Given properties \"%s\" is not validated by annotations.", failedConcatProperties),
				lastException
			);
		}

		if (lastException != null) {
			throw newRetryableFilterMissException(lastException);
		}

		if (combinableArbitrary instanceof Traceable) {
			PropertyPath propertyPath = ((Traceable)combinableArbitrary).getPropertyPath();
			String generateType = propertyPath.getProperty().getType().getTypeName();
			String expression = "".equals(propertyPath.getExpression())
				? "$"
				: propertyPath.getExpression();

			throw new RetryableFilterMissException(
				String.format(
					"Generate type \"%s\" is failed due to property \"%s\".",
					generateType,
					expression
				),
				lastException
			);
		}

		throw newRetryableFilterMissException(lastException);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T rawValue() {
		T returned;
		for (int i = 0; i < maxMisses; i++) {
			try {
				returned = (T)combinableArbitrary.rawValue();
				if (predicate.test(returned)) {
					return returned;
				}

				if (fixed()) {
					throw new FixedValueFilterMissException("Fixed value can not satisfy given filter.");
				}
			} catch (TooManyFilterMissesException | ValidationFailedException | RetryableFilterMissException ex) {
				if (lastException == null || ex.getCause() != null) {
					lastException = ex;
				}
				combinableArbitrary.clear();
			} catch (ClassCastException ex) {
				throw new ClassCastException(
					String.format(
						"Filtering is failed due to %s. Please check if given value is well deserialized.",
						ex.getMessage()
					)
				);
			}
		}

		if (lastException == null && combinableArbitrary instanceof FilteredCombinableArbitrary) {
			lastException = ((FilteredCombinableArbitrary<T>)combinableArbitrary).lastException;
		}

		if (lastException instanceof ValidationFailedException) {
			String failedConcatProperties = String.join(", ",
				((ValidationFailedException)lastException).getConstraintViolationPropertyNames());

			throw new RetryableFilterMissException(
				String.format("Given properties \"%s\" is not validated by annotations.", failedConcatProperties),
				lastException
			);
		}

		if (lastException != null) {
			throw newRetryableFilterMissException(lastException);
		}

		if (combinableArbitrary instanceof Traceable) {
			PropertyPath propertyPath = ((Traceable)combinableArbitrary).getPropertyPath();
			String generateType = propertyPath.getProperty().getType().getTypeName();
			String expression = "".equals(propertyPath.getExpression())
				? "$"
				: propertyPath.getExpression();

			throw new RetryableFilterMissException(
				String.format(
					"Generate type \"%s\" is failed due to property \"%s\".",
					generateType,
					expression
				),
				lastException
			);
		}

		throw newRetryableFilterMissException(lastException);
	}

	@Override
	public void clear() {
		combinableArbitrary.clear();
	}

	@Override
	public boolean fixed() {
		return combinableArbitrary.fixed();
	}

	@Override
	public CombinableArbitrary<T> unique() {
		return combinableArbitrary.unique();
	}

	private RetryableFilterMissException newRetryableFilterMissException(@Nullable Throwable throwable) {
		if (!(throwable instanceof RetryableFilterMissException)) {
			return new RetryableFilterMissException(throwable);
		}

		return (RetryableFilterMissException)throwable;
	}
}
