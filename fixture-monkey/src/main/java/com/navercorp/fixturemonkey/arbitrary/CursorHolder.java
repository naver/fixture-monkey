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

import java.util.List;

final class CursorHolder {
	private final List<Cursor> cursors;
	private int index = 0;
	private final int size;

	public CursorHolder(ArbitraryExpression expression) {
		cursors = CursorFactory.create(expression);
		size = cursors.size();
	}

	public Cursor get() {
		return cursors.get(index);
	}

	public void next() {
		index++;
	}

	public boolean isDone() {
		return size <= index;
	}

	public List<Cursor> getCursors() {
		return cursors;
	}

	public int getIndex() {
		return index;
	}

	public int getSize() {
		return size;
	}
}
