package com.navercorp.fixturemonkey.generator;

import java.util.Collection;

abstract class CollectionBuilderFrame {
	protected final Collection<Object> collection;

	protected CollectionBuilderFrame(Collection<Object> collection) {
		this.collection = collection;
	}

	CollectionBuilderFrame add(Object value) {
		collection.add(value);
		return this;
	}

	abstract Collection<Object> build();
}
