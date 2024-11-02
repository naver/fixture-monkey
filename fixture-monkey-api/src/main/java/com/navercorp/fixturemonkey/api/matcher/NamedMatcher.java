package com.navercorp.fixturemonkey.api.matcher;

import com.navercorp.fixturemonkey.api.property.Property;

public final class NamedMatcher implements Matcher {
	private final Matcher matcher;
	private final String registeredName;

	public NamedMatcher(Matcher matcher, String registeredName) {
		this.matcher = matcher;
		this.registeredName = registeredName;
	}

	public boolean matchRegisteredName(String registeredName) {
		return this.registeredName.equals(registeredName);
	}

	@Override
	public boolean match(Property property) {
		return this.matcher.match(property);
	}

	@Override
	public boolean match(Property property, MatcherMetadata matcherMetadata) {
		return this.matcher.match(property) && registeredName.equals(matcherMetadata.getName());
	}
}
