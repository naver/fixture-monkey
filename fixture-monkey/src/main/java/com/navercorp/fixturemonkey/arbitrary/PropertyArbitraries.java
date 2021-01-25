package com.navercorp.fixturemonkey.arbitrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.Arbitrary;

public class PropertyArbitraries {
	@SuppressWarnings("rawtypes")
	private final Map<String, Arbitrary> propertyArbitraries;

	@SuppressWarnings("rawtypes")
	public PropertyArbitraries(Map<String, Arbitrary> propertyArbitraries) {
		if (propertyArbitraries == null) {
			propertyArbitraries = Collections.emptyMap();
		}
		this.propertyArbitraries = new HashMap<>(propertyArbitraries);
	}

	public Set<Map.Entry<String, Arbitrary>> getPropertyArbitraries() {
		return this.propertyArbitraries.entrySet();
	}
}
