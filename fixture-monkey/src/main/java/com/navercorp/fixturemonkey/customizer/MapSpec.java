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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;

public final class MapSpec implements ArbitraryBuilderVisitor {
	private final String mapName;
	private final List<Boolean> isSetKey;
	private final List<ArbitraryBuilderVisitor> next;
	@SuppressWarnings("rawtypes")
	private final List<MapSpecSet> setList;

	public MapSpec(String mapName) {
		this.mapName = mapName;
		this.isSetKey = new ArrayList<>();
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
	}

	public MapSpec(String mapName, List<Boolean> isSetKey) {
		this.mapName = mapName;
		this.isSetKey = isSetKey;
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void visit(ArbitraryBuilder arbitraryBuilder) {
		this.setList.forEach(it -> arbitraryBuilder.set(it.mapName, it.isSetKey, it.key, it.value));

		for (ArbitraryBuilderVisitor nextMapSpec : this.next) {
			nextMapSpec.visit(arbitraryBuilder);
		}
	}

	public <K> MapSpec addKey(K key) {
		addSet(mapName, isSetKey, key, null);
		return this;
	}

	public MapSpec addKey(Consumer<MapSpec> consumer) {
		List<Boolean> isSetKey = getIsSetKey(true);
		MapSpec mapSpec = new MapSpec(this.mapName + "[]", isSetKey);
		consumer.accept(mapSpec);
		addNext(mapSpec);
		return this;
	}

	public <V> MapSpec addValue(V value) {
		addSet(mapName, isSetKey, null, value);
		return this;
	}

	public MapSpec addValue(Consumer<MapSpec> consumer) {
		List<Boolean> isSetKey = getIsSetKey(false);
		MapSpec mapSpec = new MapSpec(this.mapName + "[]", isSetKey);
		consumer.accept(mapSpec);
		addNext(mapSpec);
		return this;
	}

	public <K, V> MapSpec put(K key, V value) {
		addSet(mapName, isSetKey, key, value);
		return this;
	}

	private List<Boolean> getIsSetKey(Boolean bool) {
		List<Boolean> isSetKey = new ArrayList<>(this.isSetKey);
		isSetKey.add(bool);
		return isSetKey;
	}

	private void addNext(ArbitraryBuilderVisitor arbitraryBuilderVisitor) {
		this.next.add(arbitraryBuilderVisitor);
	}

	private <K, V> void addSet(String mapName, List<Boolean> isSetKey, K key, V value) {
		this.setList.add(new MapSpecSet<>(mapName, isSetKey, key, value));
	}

	private static class MapSpecSet<K, V> {
		private final String mapName;
		private final List<Boolean> isSetKey;
		private final K key;
		private final V value;

		public MapSpecSet(String mapName, List<Boolean> isSetKey, K key, V value) {
			this.mapName = mapName;
			this.isSetKey = isSetKey;
			this.key = key;
			this.value = value;
		}
	}
}
