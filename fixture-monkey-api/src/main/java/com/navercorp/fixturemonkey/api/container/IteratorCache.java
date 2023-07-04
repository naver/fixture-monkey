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

package com.navercorp.fixturemonkey.api.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is used for caching {@link Iterator}.
 * It is necessary for using the elements of {@link Iterator} because {@link Iterator} could only be retrieved once.
 */
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class IteratorCache {
	private static final ConcurrentLruCache<Iterator<?>, List<?>> ITERATOR_TO_LIST = new ConcurrentLruCache<>(2048);

	/**
	 * Gets the elements of {@link Iterator} in an idempotent manner.
	 *
	 * @param iterator whose elements are needed
	 * @return the elements of {@link Iterator}
	 */
	public static List<?> getList(Iterator<?> iterator) {
		if (ITERATOR_TO_LIST.containsKey(iterator)) {
			return ITERATOR_TO_LIST.get(iterator);
		}
		List<?> list = toList(iterator);
		ITERATOR_TO_LIST.put(iterator, list);
		return list;
	}

	private static <T> List<T> toList(Iterator<T> iterator) {
		List<T> list = new ArrayList<>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}
}
