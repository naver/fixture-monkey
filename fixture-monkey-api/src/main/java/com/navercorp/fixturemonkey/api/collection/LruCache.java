package com.navercorp.fixturemonkey.api.collection;

import java.util.LinkedHashMap;
import java.util.Map;

final class LruCache<K, V> extends LinkedHashMap<K, V> {
	private final int maxSize;

	LruCache(int maxSize) {
		super(maxSize + 1, 1, true);
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
