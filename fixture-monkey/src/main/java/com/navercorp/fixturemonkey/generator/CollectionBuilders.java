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

package com.navercorp.fixturemonkey.generator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

class CollectionBuilders {
	@SuppressWarnings("rawtypes")
	public static <T> Arbitrary<T> build(Class<T> clazz, List<ArbitraryNode> nodes) {
		if (isList(clazz)) {
			return ListBuilder.INSTANCE.build(nodes);
		} else if (isSet(clazz)) {
			return SetBuilder.INSTANCE.build(nodes);
		} else if (isStream(clazz)) {
			return StreamBuilder.INSTANCE.build(nodes);
		} else if (isIterator(clazz)) {
			return IteratorBuilder.INSTANCE.build(nodes);
		} else if (isMap(clazz)) {
			return MapBuilder.INSTANCE.build(nodes);
		} else if (isMapEntry(clazz)) {
			return MapEntryBuilder.INSTANCE.build(nodes);
		} else if (isOptional(clazz)) {
			return OptionalBuilder.INSTANCE.build(nodes);
		} else {
			throw new IllegalArgumentException("Not implemented collection.");
		}
	}

	private static <T> boolean isList(Class<T> clazz) {
		return clazz.isAssignableFrom(List.class)
			|| clazz.isAssignableFrom(Iterable.class);
	}

	private static <T> boolean isSet(Class<T> clazz) {
		return clazz.isAssignableFrom(Set.class);
	}

	private static <T> boolean isStream(Class<T> clazz) {
		return clazz.isAssignableFrom(Stream.class);
	}

	private static <T> boolean isIterator(Class<T> clazz) {
		return clazz.isAssignableFrom(Iterator.class);
	}

	private static <T> boolean isMap(Class<T> clazz) {
		return clazz.isAssignableFrom(Map.class);
	}

	private static <T> boolean isMapEntry(Class<T> clazz) {
		return clazz.isAssignableFrom(Map.Entry.class);
	}

	private static <T> boolean isOptional(Class<T> clazz) {
		return clazz.isAssignableFrom(Optional.class);
	}
}
