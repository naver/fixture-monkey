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

package com.navercorp.fixturemonkey;

import java.util.Collection;
import java.util.Iterator;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

abstract class DecoratedList<T extends BuilderManipulator> implements Collection<T> {
	protected final DecoratedList<T> decoratedList;

	public DecoratedList(DecoratedList<T> decoratedList) {
		this.decoratedList = decoratedList;
	}

	@Override
	public abstract boolean add(T value);

	@SuppressWarnings("NullableProblems")
	@Override
	public abstract boolean addAll(Collection<? extends T> collection);

	abstract DecoratedList<T> copy();

	public T get(int index) {
		return decoratedList.get(index);
	}

	public T remove(int index) {
		return decoratedList.remove(index);
	}

	@Override
	public int size() {
		return decoratedList.size();
	}

	@Override
	public boolean isEmpty() {
		return decoratedList.isEmpty();
	}

	@Override
	public boolean contains(Object obj) {
		return decoratedList.contains(obj);
	}

	@Override
	public Iterator<T> iterator() {
		return decoratedList.iterator();
	}

	@Override
	public Object[] toArray() {
		return decoratedList.toArray();
	}

	@Override
	public boolean remove(Object obj) {
		return decoratedList.remove(obj);
	}

	@Override
	public void clear() {
		decoratedList.clear();
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean retainAll(Collection collection) {
		return decoratedList.retainAll(collection);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean removeAll(Collection collection) {
		return decoratedList.removeAll(collection);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean containsAll(Collection collection) {
		return decoratedList.containsAll(collection);
	}

	@SuppressWarnings({"unchecked", "NullableProblems"})
	@Override
	public T[] toArray(Object[] array) {
		return decoratedList.toArray(array);
	}
}
