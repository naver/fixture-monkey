package com.navercorp.fixturemonkey.api.container;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It is the Least Recently Used cache.
 * It would remove the least recently used element when it is full.
 *
 * @param <K> key of the cache
 * @param <V> value of the cache
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
public final class LruCache<K, V> extends LinkedHashMap<K, V> {
	private final int maxSize;

	public LruCache(int maxSize) {
		super(maxSize + 1, 1, true);
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
