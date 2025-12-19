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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It is the Concurrent Least Recently Used cache.
 * It would remove the least recently used element when it is full.
 *
 * @param <K> key of the cache
 * @param <V> value of the cache
 */
@API(since = "0.5.10", status = Status.MAINTAINED)
@SuppressWarnings({"contracts", "override", "return"})
public final class ConcurrentLruCache<K, V> implements Map<K, V> {
	private final Map<K, V> lruCache;

	public ConcurrentLruCache(int maxSize) {
		lruCache = Collections.synchronizedMap(new LruCache<>(maxSize));
	}

	@Override
	public int size() {
		return lruCache.size();
	}

	@Override
	public boolean isEmpty() {
		return lruCache.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return lruCache.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return lruCache.containsValue(value);
	}

	@Override
	public @Nullable V get(Object key) {
		return lruCache.get(key);
	}

	@Override
	public V put(K key, V value) {
		return lruCache.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return lruCache.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		lruCache.putAll(map);
	}

	@Override
	public void clear() {
		lruCache.clear();
	}

	@Override
	public Set<K> keySet() {
		return lruCache.keySet();
	}

	@Override
	public Collection<V> values() {
		return lruCache.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return lruCache.entrySet();
	}
}
