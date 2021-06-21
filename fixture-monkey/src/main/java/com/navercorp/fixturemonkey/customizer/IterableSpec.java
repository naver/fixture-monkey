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
