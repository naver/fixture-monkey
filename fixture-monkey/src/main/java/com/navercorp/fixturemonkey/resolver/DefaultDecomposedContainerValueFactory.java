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

package com.navercorp.fixturemonkey.resolver;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.collection.IteratorCache;
import com.navercorp.fixturemonkey.api.collection.StreamCache;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class DefaultDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	private final DecomposedContainerValueFactory additionalDecomposedContainerValueFactory;

	public DefaultDecomposedContainerValueFactory(
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		this.additionalDecomposedContainerValueFactory = additionalDecomposedContainerValueFactory;
	}

	@Override
	public DecomposableContainerValue from(Object value) {
		Class<?> actualType = value.getClass();

		if (Iterable.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterable<?>)value).iterator();
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableContainerValue(list, list.size());
		} else if (Iterator.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterator<?>)value);
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableContainerValue(list, list.size());
		} else if (Stream.class.isAssignableFrom(actualType)) {
			List<?> container = StreamCache.getList((Stream<?>)value);
			return new DecomposableContainerValue(container, container.size());
		} else if (actualType.isArray()) {
			return new DecomposableContainerValue(value, Array.getLength(value));
		} else if (Map.class.isAssignableFrom(actualType)) {
			Map<?, ?> map = (Map<?, ?>)value;
			return new DecomposableContainerValue(value, map.size());
		} else if (Map.Entry.class.isAssignableFrom(actualType)) {
			return new DecomposableContainerValue(value, 1);
		} else if (isOptional(actualType)) {
			return new DecomposableContainerValue(value, 1);
		}

		return additionalDecomposedContainerValueFactory.from(value);
	}

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}
}
