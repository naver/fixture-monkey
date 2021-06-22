package com.navercorp.fixturemonkey.customizer;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IterableSpec {
	IterableSpec ofMinSize(int size);

	IterableSpec ofMaxSize(int size);

	IterableSpec ofSize(int size);

	IterableSpec ofSizeBetween(int min, int max);

	IterableSpec ofNotNull();

	IterableSpec setElement(long fieldIndex, Object object);

	<T> IterableSpec filterElement(long fieldIndex, Predicate<T> filter);

	IterableSpec listElement(long fieldIndex, Consumer<IterableSpec> spec);

	IterableSpec listFieldElement(long fieldIndex, String fieldName, Consumer<IterableSpec> spec);

	IterableSpec setElementField(long fieldIndex, String fieldName, Object object);

	<T> IterableSpec filterElementField(long fieldIndex, String fieldName, Predicate<T> filter);

	<T> IterableSpec any(Predicate<T> filter);

	<T> IterableSpec all(Predicate<T> filter);

	IterableSpec any(Object object);

	IterableSpec all(Object object);
}
