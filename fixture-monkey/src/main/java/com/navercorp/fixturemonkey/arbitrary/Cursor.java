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

package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.ALL_INDEX_STRING;
import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.Objects;

abstract class Cursor { // TODO: matcher를 구현하도록 수정
	private final String name;
	private final int index;

	public Cursor(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public boolean match(Cursor cursor) {
		boolean sameName = nameEquals(cursor.getName());
		boolean sameIndex = indexEquals(cursor.getIndex());
		return sameName && sameIndex;
	}

	private boolean indexEquals(int index) {
		return this.index == index
			|| index == NO_OR_ALL_INDEX_INTEGER_VALUE
			|| this.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
	}

	private boolean nameEquals(String name) {
		return this.name.equals(name)
			|| name.equals(ALL_INDEX_STRING)
			|| this.name.equals(ALL_INDEX_STRING);
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Cursor)) {
			return false;
		}
		Cursor cursor = (Cursor)obj;

		boolean indexEqual = indexEquals(cursor.getIndex());
		boolean nameEqual = nameEquals(cursor.getName());
		return nameEqual && indexEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
