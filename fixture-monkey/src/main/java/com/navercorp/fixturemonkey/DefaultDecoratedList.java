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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

@SuppressWarnings({"SuspiciousMethodCalls", "NullableProblems", "unchecked"})
final class DefaultDecoratedList<T extends BuilderManipulator> extends DecoratedList<T> {
	private final List<T> list;

	public DefaultDecoratedList(List<T> list) {
		super(null); // for DecoratedList type
		this.list = list;
	}

	@Override
	public T get(int index) {
		return list.get(index);
	}

	@Override
	public T remove(int index) {
		return list.remove(index);
	}

	@Override
	public DecoratedList<T> copy() {
		List<T> copied = (List<T>)this.list.stream().map(BuilderManipulator::copy).collect(toList());
		return new DefaultDecoratedList<>(copied);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object obj) {
		return list.contains(obj);
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public boolean add(T obj) {
		return list.add(obj);
	}

	@Override
	public boolean remove(Object obj) {
		return list.remove(obj);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		return list.addAll(collection);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean retainAll(Collection collection) {
		return list.retainAll(collection);
	}

	@Override
	public boolean removeAll(Collection collection) {
		return list.removeAll(collection);
	}

	@Override
	public boolean containsAll(Collection collection) {
		return list.containsAll(collection);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] toArray(Object[] array) {
		return (T[])list.toArray(array);
	}
}
