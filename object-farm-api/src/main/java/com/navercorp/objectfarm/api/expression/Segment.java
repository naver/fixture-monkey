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

package com.navercorp.objectfarm.api.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

/**
 * Represents a segment in a JSONPath expression that contains one or more selectors.
 * <p>
 * This follows the terminology from <a href="https://datatracker.ietf.org/doc/rfc9535/">JSONPath RFC 9535</a>,
 * where a segment is "one of the constructs that selects children of an input value."
 * A segment contains one or more {@link Selector}s.
 * <p>
 * Segment types by selector:
 * <ul>
 *   <li>Name segment: {@code .items} - contains a {@link NameSelector}</li>
 *   <li>Index segment: {@code [0]} - contains an {@link IndexSelector}</li>
 *   <li>Wildcard segment: {@code [*]} - contains a {@link WildcardSelector}</li>
 *   <li>Union segment: {@code [0,1,2]} - contains multiple {@link IndexSelector}s</li>
 *   <li>Key segment: {@code [key]} - contains a {@link KeySelector}</li>
 *   <li>Value segment: {@code [value]} - contains a {@link ValueSelector}</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * // Single selector segment
 * Segment nameSegment = Segment.ofName("items");
 * Segment indexSegment = Segment.ofIndex(0);
 *
 * // Multiple selector segment (union)
 * Segment unionSegment = Segment.ofIndices(0, 1, 2);
 * </pre>
 *
 * @see Selector
 * @see <a href="https://datatracker.ietf.org/doc/rfc9535/">RFC 9535 - JSONPath</a>
 */
public final class Segment {
	private final List<Selector> selectors;

	private Segment(List<Selector> selectors) {
		if (selectors == null || selectors.isEmpty()) {
			throw new IllegalArgumentException("Segment must contain at least one selector");
		}
		this.selectors = Collections.unmodifiableList(new ArrayList<>(selectors));
	}

	/**
	 * Creates a segment with a single selector.
	 *
	 * @param selector the selector
	 * @return a new segment
	 */
	public static Segment of(Selector selector) {
		Objects.requireNonNull(selector, "selector must not be null");
		return new Segment(Collections.singletonList(selector));
	}

	/**
	 * Creates a segment with multiple selectors.
	 *
	 * @param selectors the selectors
	 * @return a new segment
	 */
	public static Segment of(List<Selector> selectors) {
		return new Segment(selectors);
	}

	/**
	 * Creates a name segment.
	 *
	 * @param name the property name
	 * @return a new segment with a NameSelector
	 */
	public static Segment ofName(String name) {
		return of(new NameSelector(name));
	}

	/**
	 * Creates an index segment.
	 *
	 * @param index the index
	 * @return a new segment with an IndexSelector
	 */
	public static Segment ofIndex(int index) {
		return of(new IndexSelector(index));
	}

	/**
	 * Creates a union segment with multiple indices.
	 *
	 * @param indices the indices
	 * @return a new segment with multiple IndexSelectors
	 */
	public static Segment ofIndices(int... indices) {
		List<Selector> selectors = new ArrayList<>();
		for (int index : indices) {
			selectors.add(new IndexSelector(index));
		}
		return new Segment(selectors);
	}

	/**
	 * Creates a wildcard segment.
	 *
	 * @return a new segment with a WildcardSelector
	 */
	public static Segment ofWildcard() {
		return of(new WildcardSelector());
	}

	/**
	 * Creates a key segment for Map entry keys.
	 *
	 * @return a new segment with a KeySelector
	 */
	public static Segment ofKey() {
		return of(new KeySelector());
	}

	/**
	 * Creates a value segment for Map entry values.
	 *
	 * @return a new segment with a ValueSelector
	 */
	public static Segment ofValue() {
		return of(new ValueSelector());
	}

	/**
	 * Creates a key-value union segment for Map entries.
	 * This matches both the key and value of a Map entry.
	 *
	 * @return a new segment with KeySelector and ValueSelector
	 */
	public static Segment ofKeyValue() {
		List<Selector> selectors = new ArrayList<>();
		selectors.add(new KeySelector());
		selectors.add(new ValueSelector());
		return new Segment(selectors);
	}

	/**
	 * Creates a type segment for type-based matching.
	 *
	 * @param type the target type to match
	 * @return a new segment with a TypeSelector
	 */
	public static Segment ofType(Class<?> type) {
		return of(new TypeSelector(type));
	}

	/**
	 * Creates a type segment with exact or assignable matching.
	 *
	 * @param type the target type to match
	 * @param exact if true, only exact type matches; if false, subtypes also match
	 * @return a new segment with a TypeSelector
	 */
	public static Segment ofType(Class<?> type, boolean exact) {
		return of(new TypeSelector(type, exact));
	}

	/**
	 * Returns the selectors in this segment.
	 *
	 * @return unmodifiable list of selectors
	 */
	public List<Selector> getSelectors() {
		return selectors;
	}

	/**
	 * Returns whether this segment contains a single selector.
	 *
	 * @return true if this segment has exactly one selector
	 */
	public boolean isSingleSelector() {
		return selectors.size() == 1;
	}

	/**
	 * Returns the first (or only) selector in this segment.
	 *
	 * @return the first selector
	 */
	public Selector getFirstSelector() {
		return selectors.get(0);
	}

	/**
	 * Returns the string representation of this segment.
	 * <p>
	 * Format depends on the selector type:
	 * <ul>
	 *   <li>Name: {@code .name}</li>
	 *   <li>Index: {@code [0]}</li>
	 *   <li>Union: {@code [0,1,2]}</li>
	 *   <li>Wildcard: {@code [*]}</li>
	 *   <li>Key: {@code [key]}</li>
	 *   <li>Value: {@code [value]}</li>
	 * </ul>
	 *
	 * @return segment as string expression
	 */
	public String toExpression() {
		if (selectors.size() == 1) {
			Selector selector = selectors.get(0);
			if (selector instanceof NameSelector) {
				return "." + selector.toExpression();
			} else {
				return "[" + selector.toExpression() + "]";
			}
		}

		// Multiple selectors (union)
		String joined = selectors.stream()
			.map(Selector::toExpression)
			.collect(Collectors.joining(","));
		return "[" + joined + "]";
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Segment segment = (Segment)obj;
		return Objects.equals(selectors, segment.selectors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(selectors);
	}

	@Override
	public String toString() {
		return "Segment{selectors=" + selectors + "}";
	}
}
