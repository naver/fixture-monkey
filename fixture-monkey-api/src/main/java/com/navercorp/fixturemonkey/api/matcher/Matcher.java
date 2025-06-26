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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.MAINTAINED)
@FunctionalInterface
public interface Matcher {
	boolean match(Property property);

	/**
	 * Checks whether the given property matches the criteria using the provided metadata.
	 * This default implementation ignores `matcherMetadata` to maintain compatibility
	 * with existing match flows. Override this method if metadata-based matching becomes necessary.
	 * @see NamedMatcher#match(Property, MatcherMetadata)
	 *
	 * @param property the property to match
	 * @param matcherMetadata additional matching information
	 * @return true if the property matches, otherwise false
	 */
	@API(since = "1.1.15", status = Status.EXPERIMENTAL)
	default boolean match(Property property, MatcherMetadata matcherMetadata) {
		return match(property);
	}

	/**
	 * Creates and returns a new {@code Matcher} that represents the intersection
	 * of this matcher with another specified matcher. The resulting matcher will
	 * only match a property if both this matcher and the specified second matcher
	 * match the property.
	 *
	 * @param second the second matcher to intersect with this matcher
	 * @return a new {@code Matcher} representing the intersection of this matcher
	 *         and the specified second matcher
	 */
	@API(since = "1.0.13", status = Status.EXPERIMENTAL)
	default Matcher intersect(Matcher second) {
		return new IntersectMatcher(this, second);
	}

	/**
	 * Creates and returns a new {@code Matcher} that represents the union
	 * of this matcher with another specified matcher. The resulting matcher will
	 * match a property if either this matcher or the specified second matcher
	 * matches the property.
	 *
	 * @param second the second matcher to form the union with this matcher
	 * @return a new {@code Matcher} representing the union of this matcher
	 *         and the specified second matcher
	 */
	@API(since = "1.0.13", status = Status.EXPERIMENTAL)
	default Matcher union(Matcher second) {
		return new UnionMatcher(this, second);
	}
}
