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
import java.util.List;
public class MapSpec implements ExpressionSpecVisitor {
	//
	private final MapExpression mapExpression;
	private final List<ExpressionSpecVisitor> next;
	private final List<MapSpecSet> setList;

	private Integer minSize = null;
	private Integer maxSize = null;
	private boolean notNull = false;

	public MapSpec(String mapName) {
		this.mapExpression = new MapExpression(mapName);
		this.next = new ArrayList<>();
		this.setList = new ArrayList<>();
	}

	//새로운 entry를 추가하고 key를 value로 설정.
	// public MapSpec setKey(Object value){
	// 	return setKey(value, value);
	// }

	// 기존 entry의 key의 값을 value로 교체. 일치하는 키가 없다면 새로운 Entry 생성 후 key의 값을 value로 설정.
	public MapSpec setKey(Object key, Object value) {
		mapExpression.keys.add(key);
		mapExpression.isSetKey.add(true);
		addSet(mapExpression, value);
		return this;
	}

	public MapSpec setValue(Object key, Object value) {
		mapExpression.keys.add(key);
		mapExpression.isSetKey.add(false);
		addSet(mapExpression, value);
		return this;
	}

	// public MapSpec setKey(Object key, Consumer<MapSpec> consumer) {
	// 	MapSpec mapSpec = new MapSpec(mapName);
	// 	consumer.accept(mapSpec);
	// 	addNext(mapSpec);
	// 	return this;
	// }
	//
	// public MapSpec setValue(Object key, Consumer<MapSpec> consumer) {
	// 	MapSpec mapSpec = new MapSpec(mapName);
	// 	consumer.accept(mapSpec);
	// 	addNext(mapSpec);
	// 	return this;
	// }

	// private void addNext(ExpressionSpecVisitor specVisitor) {
	// 	this.next.add(specVisitor);
	// }
	private <T> void addSet(MapExpression mapExpression, T object) {
		this.setList.add(new MapSpecSet<>(mapExpression, object));
	}

	@Override
	public void visit(ExpressionSpec expressionSpec) {
		this.setList.forEach(it ->
			expressionSpec.set(it.mapExpression.getMapName(), it.mapExpression.keys, it.mapExpression.isSetKey, it.value));

		// for (ExpressionSpecVisitor nextMapSpec : this.next) {
		// 	nextMapSpec.visit(expressionSpec);
		// }
	}

	private static class MapSpecSet<T> {
		private final MapExpression mapExpression;
		private final T value;

		public MapSpecSet(MapExpression mapExpression, T value) {
			this.mapExpression = mapExpression;
			this.value = value;
		}
	}

	//값을 설정해야하는 노드를 찾기 위한 정보를 저장하는 클래스
	private static class MapExpression {
		private final String mapName;
		public List<Object> keys;
		// Key의 값을 바꾸고 싶은지, Value의 값을 바꾸고 싶은지 히스토리를 기록. boolean list의 변수명??
		public List<Boolean> isSetKey;

		public MapExpression(String mapName) {
			this.mapName = mapName;
			this.keys = new ArrayList<>();
			this.isSetKey = new ArrayList<>();
		}
		public MapExpression(String mapName, List<Object> key, List<Boolean> isSetKey) {
			this.mapName = mapName;
			this.keys = key;
			this.isSetKey = isSetKey;
		}

		public String getMapName() {
			return mapName;
		}
	}
}
