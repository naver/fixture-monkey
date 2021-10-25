package com.navercorp.fixturemonkey;

import java.util.Collection;
import java.util.function.Consumer;

interface DecoratedList<T> extends Collection<T> {
	T get(int index);

	T remove(int index);

	DecoratedList<T> copy();
}
