package com.navercorp.fixturemonkey.api.matcher;

import com.navercorp.fixturemonkey.api.property.Property;

import java.util.List;

public interface MatcherOperatorRegistry<T> {
	List<MatcherOperator<T>> values();
	List<MatcherOperator<T>> get(Property property);
}
