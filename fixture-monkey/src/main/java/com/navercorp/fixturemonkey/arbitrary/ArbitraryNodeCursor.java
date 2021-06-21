package com.navercorp.fixturemonkey.arbitrary;

public class ArbitraryNodeCursor extends Cursor {
	public ArbitraryNodeCursor(ArbitraryNode<?> node) {
		super(node.getFieldName(), node.getIndexOfIterable());
	}
}
