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

import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.TooManyFilterMissesException;

import com.navercorp.fixturemonkey.api.exception.FilterMissException;
import com.navercorp.fixturemonkey.api.exception.IgnoredSizeValidationException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.property.PropertyPath;
import com.navercorp.fixturemonkey.api.property.Traceable;

/**
 * It would generate an object satisfied given {@code predicate}.
 * It would try {@code maxMisses} times, {@code maxMisses} is 1000 in default.
 * It would try {@link #FAILED_THRESHOLD} times when the parent {@link CombinableArbitrary} make it regenerate.
 */
@API(since = "0.5.0", status = Status.MAINTAINED)
final class FilteredCombinableArbitrary implements CombinableArbitrary {
	private static final int FAILED_THRESHOLD = 3;

	private final int maxMisses;
	private final CombinableArbitrary combinableArbitrary;
	private final Predicate<Object> predicate;

	private Exception lastException;
	private int failureCount = 0;

	private static final Logger LOGGER = LoggerFactory.getLogger(FilteredCombinableArbitrary.class);

	FilteredCombinableArbitrary(
		int maxMisses,
		CombinableArbitrary combinableArbitrary,
		Predicate<Object> predicate
	) {
		this.maxMisses = maxMisses;
		this.combinableArbitrary = combinableArbitrary;
		this.predicate = predicate;
	}

	@Override
	public Object combined() {
		if (failureCount == FAILED_THRESHOLD) {
			throw new FilterMissException(lastException);
		}

		Object returned;
		for (int i = 0; i < maxMisses; i++) {
			returned = combinableArbitrary.combined();
			try {
				if (predicate.test(returned)) {
					return returned;
				}
			} catch (IgnoredSizeValidationException ex) {
				Set<String> ignoredConstraintViolationPropertyNames = ex.getIgnoredConstraintViolationPropertyNames();
				LOGGER.warn(
					"Fail to satisfy constraints: " + String.join(", ", ignoredConstraintViolationPropertyNames),
					ex
				);
				return returned;
			} catch (TooManyFilterMissesException | ValidationFailedException | FilterMissException ex) {
				if (combinableArbitrary.fixed()) {
					break;
				}
				lastException = ex;
				combinableArbitrary.clear();
			}
		}
		failureCount++;

		if (lastException == null && combinableArbitrary instanceof FilteredCombinableArbitrary) {
			lastException = ((FilteredCombinableArbitrary)combinableArbitrary).lastException;
		}

		if (lastException instanceof ValidationFailedException) {
			String failedConcatProperties = String.join(", ",
				((ValidationFailedException)lastException).getConstraintViolationPropertyNames());

			throw new FilterMissException(
				String.format("Given properties \"%s\" is not validated by annotations.", failedConcatProperties),
				lastException
			);
		}

		if (combinableArbitrary instanceof Traceable) {
			PropertyPath propertyPath = ((Traceable)combinableArbitrary).getPropertyPath();
			String generateType = propertyPath.getProperty().getType().getTypeName();
			String expression = "".equals(propertyPath.getExpression())
				? "$"
				: propertyPath.getExpression();

			throw new FilterMissException(
				String.format(
					"Generate type \"%s\" is failed due to property \"%s\".",
					generateType,
					expression
				),
				lastException
			);
		}

		throw new FilterMissException(lastException);
	}

	@Override
	public Object rawValue() {
		if (failureCount == FAILED_THRESHOLD) {
			throw new FilterMissException(lastException);
		}

		Object returned;
		for (int i = 0; i < maxMisses; i++) {
			try {
				returned = combinableArbitrary.rawValue();
				if (predicate.test(returned)) {
					return returned;
				}
			} catch (TooManyFilterMissesException | ValidationFailedException | FilterMissException ex) {
				if (combinableArbitrary.fixed()) {
					break;
				}
				lastException = ex;
				combinableArbitrary.clear();
			}
		}
		failureCount++;
		throw new FilterMissException(lastException);
	}

	@Override
	public void clear() {
		if (failureCount == FAILED_THRESHOLD) {
			return;
		}
		combinableArbitrary.clear();
	}

	@Override
	public boolean fixed() {
		return failureCount == FAILED_THRESHOLD || combinableArbitrary.fixed();
	}
}
