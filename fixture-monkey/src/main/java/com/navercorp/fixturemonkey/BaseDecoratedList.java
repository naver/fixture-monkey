package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;

@SuppressWarnings({"SuspiciousMethodCalls", "NullableProblems", "unchecked"})
public class BaseDecoratedList<T extends BuilderManipulator> implements DecoratedList<T> {
	private final List<T> list;

	public BaseDecoratedList(List<T> list) {
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
		return new BaseDecoratedList<>(copied);
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
