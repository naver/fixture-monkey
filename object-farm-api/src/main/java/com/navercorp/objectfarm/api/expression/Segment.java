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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
	private static final Segment WILDCARD = new Segment(new WildcardSelector());
	private static final Segment KEY = new Segment(new KeySelector());
	private static final Segment VALUE = new Segment(new ValueSelector());
	private static final int MAX_CACHED_NAMES = 4096;
	private static final ConcurrentMap<String, Segment> SEGMENTS_BY_NAME = new ConcurrentHashMap<>();

	private final Selector firstSelector;
	private final List<Selector> unionSelectors;

	private Segment(Selector selector) {
		this.firstSelector = selector;
		this.unionSelectors = Collections.emptyList();
	}

	private Segment(List<Selector> selectors) {
		if (selectors == null || selectors.isEmpty()) {
			throw new IllegalArgumentException("Segment must contain at least one selector");
		}
		this.firstSelector = selectors.get(0);
		this.unionSelectors = selectors.size() == 1
			? Collections.emptyList()
			: Collections.unmodifiableList(new ArrayList<>(selectors));
	}

	/**
	 * Creates a segment with a single selector.
	 *
	 * @param selector the selector
	 * @return a new segment
	 */
	public static Segment of(Selector selector) {
		Objects.requireNonNull(selector, "selector must not be null");
		return new Segment(selector);
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
	 * Creates a name segment. Segments of the same name may be shared, since a segment is immutable.
	 *
	 * @param name the property name
	 * @return a segment with a NameSelector
	 */
	public static Segment ofName(String name) {
		Objects.requireNonNull(name, "name must not be null");
		Segment cached = SEGMENTS_BY_NAME.get(name);
		if (cached != null) {
			return cached;
		}
		Segment segment = of(new NameSelector(name));
		if (SEGMENTS_BY_NAME.size() >= MAX_CACHED_NAMES) {
			return segment;
		}
		Segment existing = SEGMENTS_BY_NAME.putIfAbsent(name, segment);
		return existing != null ? existing : segment;
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
	 * Returns the wildcard segment.
	 *
	 * @return the segment with a WildcardSelector
	 */
	public static Segment ofWildcard() {
		return WILDCARD;
	}

	/**
	 * Returns the key segment for Map entry keys.
	 *
	 * @return the segment with a KeySelector
	 */
	public static Segment ofKey() {
		return KEY;
	}

	/**
	 * Returns the value segment for Map entry values.
	 *
	 * @return the segment with a ValueSelector
	 */
	public static Segment ofValue() {
		return VALUE;
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
	 * Returns whether this segment, as a pattern, matches {@code pathSegment}: {@code [*]} matches an index, key or
	 * value, {@code .*} matches any name, and a segment of several selectors matches when any of its selectors
	 * matches one of the path segment's.
	 *
	 * @param pathSegment the segment of a concrete path
	 * @return true when this segment matches it
	 */
	public boolean matches(Segment pathSegment) {
		if (!isSingleSelector() || !pathSegment.isSingleSelector()) {
			for (Selector patternSelector : getSelectors()) {
				for (Selector pathSelector : pathSegment.getSelectors()) {
					if (selectorMatches(patternSelector, pathSelector)) {
						return true;
					}
				}
			}
			return false;
		}
		return selectorMatches(getFirstSelector(), pathSegment.getFirstSelector());
	}

	private static boolean selectorMatches(Selector patternSelector, Selector pathSelector) {
		if (patternSelector instanceof WildcardSelector) {
			return (pathSelector instanceof IndexSelector
				|| pathSelector instanceof KeySelector
				|| pathSelector instanceof ValueSelector);
		}
		if (patternSelector instanceof KeySelector) {
			return pathSelector instanceof KeySelector;
		}
		if (patternSelector instanceof ValueSelector) {
			return pathSelector instanceof ValueSelector;
		}
		if (patternSelector instanceof TypeSelector) {
			if (!(pathSelector instanceof TypeSelector)) {
				return false;
			}
			TypeSelector patternType = (TypeSelector)patternSelector;
			TypeSelector pathType = (TypeSelector)pathSelector;
			return patternType.matchesType(pathType.getTargetType());
		}
		if (patternSelector instanceof NameSelector) {
			if (!(pathSelector instanceof NameSelector)) {
				return false;
			}
			NameSelector patternName = (NameSelector)patternSelector;
			if ("*".equals(patternName.getName())) {
				return true;
			}
			NameSelector pathName = (NameSelector)pathSelector;
			return patternName.getName().equals(pathName.getName());
		}
		if (patternSelector instanceof IndexSelector) {
			if (!(pathSelector instanceof IndexSelector)) {
				return false;
			}
			IndexSelector patternIndex = (IndexSelector)patternSelector;
			IndexSelector pathIndex = (IndexSelector)pathSelector;
			return patternIndex.getIndex() == pathIndex.getIndex();
		}
		return false;
	}

	/**
	 * Returns the selectors in this segment.
	 *
	 * @return unmodifiable list of selectors
	 */
	public List<Selector> getSelectors() {
		return isSingleSelector() ? Collections.singletonList(firstSelector) : unionSelectors;
	}

	/**
	 * Returns whether this segment contains a single selector.
	 *
	 * @return true if this segment has exactly one selector
	 */
	public boolean isSingleSelector() {
		return unionSelectors.isEmpty();
	}

	/**
	 * Returns the first (or only) selector in this segment.
	 *
	 * @return the first selector
	 */
	public Selector getFirstSelector() {
		return firstSelector;
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
		if (isSingleSelector()) {
			if (firstSelector instanceof NameSelector) {
				return "." + firstSelector.toExpression();
			} else {
				return "[" + firstSelector.toExpression() + "]";
			}
		}

		// Multiple selectors (union)
		String joined = unionSelectors.stream()
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
		return firstSelector.equals(segment.firstSelector) && unionSelectors.equals(segment.unionSelectors);
	}

	@Override
	public int hashCode() {
		int selectorsHashCode = isSingleSelector() ? 31 + firstSelector.hashCode() : unionSelectors.hashCode();
		return 31 + selectorsHashCode;
	}

	@Override
	public String toString() {
		return "Segment{selectors=" + getSelectors() + "}";
	}
}
