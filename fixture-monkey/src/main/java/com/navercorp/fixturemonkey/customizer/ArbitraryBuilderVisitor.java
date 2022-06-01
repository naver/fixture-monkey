package com.navercorp.fixturemonkey.customizer;

import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;

public interface ArbitraryBuilderVisitor {
	void visit(ArbitraryBuilder arbitraryBuilder);
}
