package com.navercorp.fixturemonkey.api.matcher;

import java.util.List;

import com.navercorp.fixturemonkey.api.property.Property;

public interface MatcherOperatorRegistry<T> {
	List<MatcherOperator<T>> values();

	List<MatcherOperator<T>> get(Property property);
}
