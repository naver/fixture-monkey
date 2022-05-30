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

package com.navercorp.fixturemonkey.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MapSpec implements ExpressionSpecVisitor {
	//
	private final String mapName;
	private final MapKey mapKey;
	private final List<ExpressionSpecVisitor> next;
	private final List<MapSpecSet> setList;

	private Integer minSize = null;
	private Integer maxSize = null;
	private boolean notNull = false;

	public MapSpec(String mapName) {
		this.mapName = mapName;
		this.mapKey = new MapKey();
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
	}

	public MapSpec(String mapName, MapKey mapKey) {
		this.mapName = mapName;
		this.mapKey = mapKey;
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
	}

	//새로운 entry를 추가하고 key를 value로 설정.
	public MapSpec setKey(Object value){
		return setKey("?", value);
	}

	// 기존 entry의 key의 값을 value로 교체. 일치하는 키가 없다면 새로운 Entry 생성 후 key의 값을 value로 설정.
	public MapSpec setKey(Object key, Object value) {
		MapKey mapKey = getMapKey(key,true);
		addSet(this.mapName+"[?]", mapKey, value);
		return this;
	}

	public MapSpec setValue(Object key, Object value) {
		MapKey mapKey = getMapKey(key,false);
		addSet(this.mapName+"[]", mapKey, value);
		return this;
	}

	public MapSpec setKey(Object key, Consumer<MapSpec> consumer) {
		MapKey mapKey = getMapKey(key,true);
		MapSpec mapSpec = new MapSpec(this.mapName+"[]", mapKey);
		consumer.accept(mapSpec);
		addNext(mapSpec);
		return this;
	}

	public MapSpec setValue(Object key, Consumer<MapSpec> consumer) {
		MapKey mapKey = getMapKey(key,false);
		MapSpec mapSpec = new MapSpec(this.mapName+"[]", mapKey);
		consumer.accept(mapSpec);
		addNext(mapSpec);
		return this;
	}

	private MapKey getMapKey(Object key, Boolean bool) {
		List<Object> keys = new ArrayList<>();
		keys.addAll(this.mapKey.keys);
		keys.add(key);
		List<Boolean> isSetKey = new ArrayList<>();
		isSetKey.addAll(this.mapKey.isSetKey);
		isSetKey.add(bool);
		return new MapKey(keys, isSetKey);
	}

	private void addNext(ExpressionSpecVisitor specVisitor) {
		this.next.add(specVisitor);
	}
	private <T> void addSet(String mapName, MapKey mapKey, T object) {
		this.setList.add(new MapSpecSet<>(mapName, mapKey, object));
	}

	@Override
	public void visit(ExpressionSpec expressionSpec) {
		this.setList.forEach(it ->
			expressionSpec.set(it.mapName, it.mapKey.keys, it.mapKey.isSetKey, it.value));

		for (ExpressionSpecVisitor nextMapSpec : this.next) {
			nextMapSpec.visit(expressionSpec);
		}
	}

	private static class MapSpecSet<T> {

		private final String mapName;
		private final MapKey mapKey;
		private final T value;

		public MapSpecSet(String mapName, MapKey mapKey, T value) {
			this.mapName = mapName;
			this.mapKey = mapKey;
			this.value = value;
		}
	}

	//값을 설정해야하는 노드를 찾기 위한 정보를 저장하는 클래스
	private static class MapKey {
		private final List<Object> keys;
		// Key의 값을 바꾸고 싶은지, Value의 값을 바꾸고 싶은지 히스토리를 기록. boolean list의 변수명??
		private final List<Boolean> isSetKey;

		public MapKey() {
			this.keys = new ArrayList<>();
			this.isSetKey = new ArrayList<>();
		}
		public MapKey(List<Object> key, List<Boolean> isSetKey) {
			this.keys = key;
			this.isSetKey = isSetKey;
		}
	}
}
