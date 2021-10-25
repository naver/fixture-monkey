package com.navercorp.fixturemonkey;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

@SuppressWarnings({"NullableProblems", "SuspiciousMethodCalls"})
final class CallbackList<T extends BuilderManipulator> implements DecoratedList<T> {
	private final DecoratedList<T> list;
	private final Consumer<T> callback;

	public CallbackList(DecoratedList<T> list, Consumer<T> callback) {
		this.list = list;
		this.callback = callback;
	}

	@Override
	public boolean add(T value) {
		boolean added = list.add(value);
		callback.accept(value);
		return added;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> collection) {
		boolean addAll = list.addAll(collection);
		collection.forEach(callback);
		return addAll;
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
	public boolean remove(Object obj) {
		return list.remove(obj);
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

	@Override
	public DecoratedList<T> copy() {
		return new CallbackList<>(this.list.copy(), callback);
	}
}
