package com.navercorp.fixturemonkey;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

@SuppressWarnings("NullableProblems")
public class CallbackList<T> implements List<T> {
	private final List<T> list;
	private final Consumer<T> callback;

	public CallbackList(List<T> list, Consumer<T> callback) {
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
	public boolean addAll(int index, @NotNull Collection<? extends T> collection) {
		boolean addAll = list.addAll(index, collection);
		collection.forEach(callback);
		return addAll;
	}

	@Override
	public T set(int index, T element) {
		T set = list.set(index, element);
		callback.accept(element);
		return set;
	}

	@Override
	public void add(int index, T element) {
		list.add(index, element);
		callback.accept(element);
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

	@SuppressWarnings("SuspiciousToArrayCall")
	@Override
	public <T1> T1[] toArray(@NotNull T1[] array) {
		return list.toArray(array);
	}

	@Override
	public boolean remove(Object obj) {
		return list.remove(obj);
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		return list.containsAll(collection);
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		return list.removeAll(collection);
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		return list.retainAll(collection);
	}

	@Override
	public void clear() {
		list.clear();
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
	public int indexOf(Object obj) {
		return list.indexOf(obj);
	}

	@Override
	public int lastIndexOf(Object obj) {
		return list.lastIndexOf(obj);
	}

	@Override
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
}
