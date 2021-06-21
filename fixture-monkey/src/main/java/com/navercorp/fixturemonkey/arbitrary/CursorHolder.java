package com.navercorp.fixturemonkey.arbitrary;

import java.util.List;

class CursorHolder {
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
