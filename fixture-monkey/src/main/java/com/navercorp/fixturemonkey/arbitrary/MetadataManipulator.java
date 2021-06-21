package com.navercorp.fixturemonkey.arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public interface MetadataManipulator extends PriorityManipulator {
	void accept(ArbitraryBuilder<?> arbitraryBuilder);

	MetadataManipulator copy();
}
