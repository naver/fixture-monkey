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

package com.navercorp.fixturemonkey.assembly;

import java.util.function.Function;
import java.util.function.Predicate;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.SeedSnapshot;

/**
 * Generates a node's value drawing from the node's value scope, so the value depends only on where the node sits in
 * the sample. Each generation draws from its own scope, so a retried generation gets a new value.
 * <p>
 * Filters, mappings, null injection and uniqueness apply to the wrapped arbitrary rather than on top of this one, so
 * they combine with the wrapped arbitrary's own as if this one were not there — stacked filters merge into one.
 */
final class ScopedCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final CombinableArbitrary<T> delegate;
	private final AssemblyTree assemblyTree;
	private final PathExpression path;

	ScopedCombinableArbitrary(CombinableArbitrary<T> delegate, AssemblyTree assemblyTree, PathExpression path) {
		this.delegate = delegate;
		this.assemblyTree = assemblyTree;
		this.path = path;
	}

	@Override
	public T combined() {
		return SeedSnapshot.runIn(assemblyTree.nextValueScope(path), delegate::combined);
	}

	@Override
	public Object rawValue() {
		return SeedSnapshot.runIn(assemblyTree.nextValueScope(path), delegate::rawValue);
	}

	@Override
	public CombinableArbitrary<T> filter(int tries, Predicate<T> predicate) {
		return new ScopedCombinableArbitrary<>(delegate.filter(tries, predicate), assemblyTree, path);
	}

	@Override
	public <R> CombinableArbitrary<R> map(Function<T, R> mapper) {
		return new ScopedCombinableArbitrary<>(delegate.map(mapper), assemblyTree, path);
	}

	@Override
	public CombinableArbitrary<T> injectNull(double nullProbability) {
		return new ScopedCombinableArbitrary<>(delegate.injectNull(nullProbability), assemblyTree, path);
	}

	@Override
	public CombinableArbitrary<T> unique() {
		return new ScopedCombinableArbitrary<>(delegate.unique(), assemblyTree, path);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean fixed() {
		return delegate.fixed();
	}
}
