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

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty.MapEntryElementType;

/**
 * A default implementation of {@link DecomposedContainerValueFactory}.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
public final class DefaultDecomposedContainerValueFactory implements DecomposedContainerValueFactory {
	private final DecomposedContainerValueFactory additionalDecomposedContainerValueFactory;

	public DefaultDecomposedContainerValueFactory(
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		this.additionalDecomposedContainerValueFactory = additionalDecomposedContainerValueFactory;
	}

	@Override
	@SuppressWarnings("argument")
	public DecomposableJavaContainer from(Object container) {
		Class<?> actualType = container.getClass();

		if (Iterable.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterable<?>)container).iterator();
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableJavaContainer(list, list.size());
		} else if (Iterator.class.isAssignableFrom(actualType)) {
			Iterator<?> iterator = ((Iterator<?>)container);
			List<?> list = IteratorCache.getList(iterator);
			return new DecomposableJavaContainer(list, list.size());
		} else if (Stream.class.isAssignableFrom(actualType)) {
			List<?> javaContainer = StreamCache.getList((Stream<?>)container);
			return new DecomposableJavaContainer(javaContainer, javaContainer.size());
		} else if (actualType.isArray()) {
			return new DecomposableJavaContainer(container, Array.getLength(container));
		} else if (Map.class.isAssignableFrom(actualType)) {
			Map<?, ?> map = (Map<?, ?>)container;
			return new DecomposableJavaContainer(container, map.size());
		} else if (Map.Entry.class.isAssignableFrom(actualType)) {
			return new DecomposableJavaContainer(container, 1);
		} else if (isOptional(actualType)) {
			return new DecomposableJavaContainer(container, 1);
		} else if (MapEntryElementType.class.isAssignableFrom(actualType)) {
			MapEntryElementType mapEntryElementType = (MapEntryElementType)container;
			return new DecomposableJavaContainer(
				new SimpleEntry<>(
					mapEntryElementType.getKey(),
					mapEntryElementType.getValue()
				),
				1
			);
		} else if (Supplier.class.isAssignableFrom(actualType)) {
			return new DecomposableJavaContainer(((Supplier<?>)container).get(), 1);
		} else if (container instanceof Proxy) {
			try {
				return new DecomposableJavaContainer(
					Proxy.getInvocationHandler(container).invoke(container, null, null),
					1
				);
			} catch (Throwable e) {
				// ignored
			}
		}

		return additionalDecomposedContainerValueFactory.from(container);
	}

	private boolean isOptional(Class<?> type) {
		return Optional.class.isAssignableFrom(type)
			|| OptionalInt.class.isAssignableFrom(type)
			|| OptionalLong.class.isAssignableFrom(type)
			|| OptionalDouble.class.isAssignableFrom(type);
	}
}
