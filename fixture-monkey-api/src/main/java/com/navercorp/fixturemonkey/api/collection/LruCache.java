package com.navercorp.fixturemonkey.api.collection;

import java.util.LinkedHashMap;
import java.util.Map;

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
