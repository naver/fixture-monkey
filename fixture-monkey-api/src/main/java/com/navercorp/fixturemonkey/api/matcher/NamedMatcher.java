/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.api.matcher;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Objects;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.property.Property;

/**
 * A {@link Matcher} of a builder registered by name. It matches what the wrapped matcher matches; the name tells the
 * builders registered for the same type apart, so one of them can be selected. Builders {@code registerByName}
 * registers for the same type are alternatives; otherwise, builders are alternatives when their wrapped matchers are
 * type matchers of the same kind for the same type, or are equal.
 * <p>
 * The weight is how likely the builder is chosen among the alternatives a sample chooses from: the chance is its
 * weight over the sum of their weights.
 */
@API(since = "1.1.15", status = EXPERIMENTAL)
public final class NamedMatcher implements Matcher {
	@API(since = "1.2.4", status = EXPERIMENTAL)
	public static final int DEFAULT_WEIGHT = 1;

	private final Matcher matcher;
	private final String registeredName;
	private final int weight;

	public NamedMatcher(Matcher matcher, String registeredName) {
		this(matcher, registeredName, DEFAULT_WEIGHT);
	}

	/**
	 * Creates a matcher of a builder registered by name with a weight.
	 *
	 * @param matcher        the matcher this one matches with
	 * @param registeredName the name the builder is registered with
	 * @param weight         how likely the builder is chosen among its alternatives, a positive number
	 * @throws IllegalArgumentException when the weight is not positive
	 */
	@API(since = "1.2.4", status = EXPERIMENTAL)
	public NamedMatcher(Matcher matcher, String registeredName, int weight) {
		if (weight <= 0) {
			throw new IllegalArgumentException("weight must be positive. weight: " + weight);
		}
		this.matcher = matcher;
		this.registeredName = Objects.requireNonNull(registeredName, "registeredName must not be null");
		this.weight = weight;
	}

	@Override
	public boolean match(Property property) {
		return this.matcher.match(property);
	}

	@Override
	public boolean match(Property property, MatcherMetadata matcherMetadata) {
		return this.matcher.match(property)
			&& matcherMetadata instanceof NamedMatcherMetadata
			&& registeredName.equals(((NamedMatcherMetadata)matcherMetadata).getName());
	}

	/**
	 * Returns the matcher this one matches with.
	 */
	@API(since = "1.2.4", status = EXPERIMENTAL)
	public Matcher getMatcher() {
		return matcher;
	}

	/**
	 * Returns the name the builder is registered with.
	 */
	@API(since = "1.2.4", status = EXPERIMENTAL)
	public String getRegisteredName() {
		return registeredName;
	}

	/**
	 * Returns how likely the builder is chosen among its alternatives.
	 */
	@API(since = "1.2.4", status = EXPERIMENTAL)
	public int getWeight() {
		return weight;
	}

	public static NamedMatcherMetadata metadata(String name) {
		return new NamedMatcherMetadata(name);
	}

	@API(since = "1.1.15", status = EXPERIMENTAL)
	public static final class NamedMatcherMetadata implements MatcherMetadata {
		private final String name;

		private NamedMatcherMetadata(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
}
