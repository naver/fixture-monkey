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

package com.navercorp.fixturemonkey.resolver;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.ContainerSizeFilterMissException;
import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.6.9", status = Status.MAINTAINED)
final class ResolvedCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private static final int VALIDATION_ANNOTATION_FILTERING_COUNT = 1;

	private final RootProperty rootProperty;
	private final LazyArbitrary<ObjectTree> objectTree;
	private final int generateMaxTries;
	private final LazyArbitrary<CombinableArbitrary<T>> arbitrary;
	private final ArbitraryValidator validator;
	private final boolean validOnly;

	private Exception lastException = null;

	public ResolvedCombinableArbitrary(
		RootProperty rootProperty,
		Supplier<ObjectTree> regenerateTree,
		Function<ObjectTree, CombinableArbitrary<T>> generateArbitrary,
		int generateMaxTries,
		ArbitraryValidator validator,
		boolean validOnly
	) {
		this.rootProperty = rootProperty;
		this.objectTree = LazyArbitrary.lazy(regenerateTree);
		this.generateMaxTries = generateMaxTries;
		this.arbitrary = LazyArbitrary.lazy(
			() -> {
				ObjectTree objectTree = this.objectTree.getValue();
				return generateArbitrary.apply(objectTree);
			}
		);
		this.validator = validator;
		this.validOnly = validOnly;
	}

	@Override
	public T combined() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return arbitrary.getValue()
					.filter(VALIDATION_ANNOTATION_FILTERING_COUNT, this.validateFilter(validOnly))
					.combined();
			} catch (ContainerSizeFilterMissException ex) {
				lastException = ex;
				objectTree.clear();
			} catch (FixedValueFilterMissException | RetryableFilterMissException ex) {
				lastException = ex;
			} finally {
				arbitrary.clear();
			}
		}

		throw new IllegalArgumentException(
			String.format(
				"Given type %s could not be generated."
					+ " Check the ArbitraryIntrospector used or the APIs used in the ArbitraryBuilder.",
				rootProperty.getType()
			),
			lastException
		);
	}

	@Override
	public Object rawValue() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return arbitrary.getValue()
					.filter(VALIDATION_ANNOTATION_FILTERING_COUNT, this.validateFilter(validOnly))
					.rawValue();
			} catch (ContainerSizeFilterMissException ex) {
				lastException = ex;
				objectTree.clear();
			} catch (FixedValueFilterMissException | RetryableFilterMissException ex) {
				lastException = ex;
			} finally {
				arbitrary.clear();
			}
		}

		throw new IllegalArgumentException(
			String.format(
				"Given type %s could not be generated."
					+ " Check the ArbitraryIntrospector used or the APIs used in the ArbitraryBuilder.",
				rootProperty.getType()
			),
			lastException
		);
	}

	@Override
	public CombinableArbitrary<T> unique(Map<Object, Object> uniqueMap) {
		return this.arbitrary.getValue().unique(uniqueMap);
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return false;
	}

	private Predicate<T> validateFilter(boolean validOnly) {
		return fixture -> {
			if (!validOnly) {
				return true;
			}

			if (fixture == null) {
				return true;
			}

			this.validator.validate(fixture);
			return true;
		};
	}
}
