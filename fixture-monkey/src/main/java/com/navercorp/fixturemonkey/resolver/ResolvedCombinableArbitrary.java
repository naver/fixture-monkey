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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.ContainerSizeFilterMissException;
import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.tree.ObjectTree;

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
final class ResolvedCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final RootProperty rootProperty;
	private final LazyArbitrary<ObjectTree> objectTree;
	private final int generateMaxTries;
	private final LazyArbitrary<CombinableArbitrary<T>> arbitrary;

	@SuppressWarnings("unchecked")
	public ResolvedCombinableArbitrary(
		RootProperty rootProperty,
		Supplier<ObjectTree> regenerateTree,
		Consumer<ObjectTree> manipulateObjectTree,
		int generateMaxTries
	) {
		this.rootProperty = rootProperty;
		this.objectTree = LazyArbitrary.lazy(regenerateTree);
		this.generateMaxTries = generateMaxTries;
		this.arbitrary = LazyArbitrary.lazy(
			() -> {
				ObjectTree objectTree = this.objectTree.getValue();
				manipulateObjectTree.accept(objectTree);
				return (CombinableArbitrary<T>)objectTree.generate();
			}
		);
	}

	@Override
	public T combined() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return arbitrary.getValue().combined();
			} catch (ContainerSizeFilterMissException ex) {
				objectTree.clear();
			} catch (FixedValueFilterMissException ignored) {
			} finally {
				arbitrary.clear();
			}
		}

		throw new IllegalArgumentException(
			String.format("Given type %s is failed to generate.", rootProperty.getType())
		);
	}

	@Override
	public Object rawValue() {
		for (int i = 0; i < generateMaxTries; i++) {
			try {
				return arbitrary.getValue().rawValue();
			} catch (ContainerSizeFilterMissException ex) {
				objectTree.clear();
			} catch (FixedValueFilterMissException ignored) {
			} finally {
				arbitrary.clear();
			}
		}

		throw new IllegalArgumentException(
			String.format("Given type %s is failed to generate.", rootProperty.getType())
		);
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean fixed() {
		return false;
	}
}
