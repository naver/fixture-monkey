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

import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.ContainerSizeNotMatchException;
import com.navercorp.fixturemonkey.api.exception.GenerateFixedValueException;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
public final class ResolvedCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final LazyArbitrary<CombinableArbitrary<T>> combinableArbitrary;
	private final int generateMaxTries;

	public ResolvedCombinableArbitrary(
		Supplier<ObjectTree> regenerateTree,
		Function<ObjectTree, CombinableArbitrary<T>> generateArbitrary,
		int generateMaxTries
	) {
		this.combinableArbitrary = LazyArbitrary.lazy(() -> generateArbitrary.apply(regenerateTree.get()));
		this.generateMaxTries = generateMaxTries;
	}

	@Override
	public T combined() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return combinableArbitrary.getValue().combined();
			} catch (ContainerSizeNotMatchException | GenerateFixedValueException ex) {
				combinableArbitrary.clear();
			}
		}

		throw new IllegalArgumentException("Generation is failed.");
	}

	@Override
	public Object rawValue() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return combinableArbitrary.getValue().combined();
			} catch (ContainerSizeNotMatchException ex) {
				combinableArbitrary.clear();
			}
		}

		throw new IllegalArgumentException("Generation is failed.");
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return false;
	}
}
