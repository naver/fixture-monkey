package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.Objects;

abstract class Cursor {
	private final String name;
	private final int index;

	public Cursor(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public boolean indexEquals(int index) {
		return this.index == index || index == NO_OR_ALL_INDEX_INTEGER_VALUE || this.index == NO_OR_ALL_INDEX_INTEGER_VALUE;
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

		boolean isIndexEqual = indexEquals(cursor.getIndex());
		return isIndexEqual && name.equals(cursor.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
