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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is used for caching {@link Stream}.
 * It is necessary for using the elements of {@link Stream} because {@link Stream} could only be retrieved once.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
public final class StreamCache {
	private static final ConcurrentLruCache<Stream<?>, List<?>> STREAM_TO_LIST = new ConcurrentLruCache<>(2048);

	/**
	 * Gets the elements of {@link Stream} in an idempotent manner.
	 *
	 * @param stream whose elements are needed
	 * @return the elements of {@link Stream}
	 */
	public static List<?> getList(Stream<?> stream) {
		if (STREAM_TO_LIST.containsKey(stream)) {
			return STREAM_TO_LIST.get(stream);
		}
		List<?> list = stream.collect(Collectors.toList());
		STREAM_TO_LIST.put(stream, list);
		return list;
	}

}
