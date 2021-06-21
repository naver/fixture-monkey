package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

final class ExpNameCursor extends Cursor {
	ExpNameCursor(String name) {
		super(name, NO_OR_ALL_INDEX_INTEGER_VALUE);
	}
}
