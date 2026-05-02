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

import org.jspecify.annotations.Nullable;

/**
 * Immutable sequence of {@link Segment}s representing a path in an object tree.
 * Follows <a href="https://datatracker.ietf.org/doc/rfc9535/">JSONPath RFC 9535</a> terminology.
 * <p>
 * Supports concrete paths ({@code $.items[0]}) and wildcard patterns ({@code $.items[*]}).
 *
 * @see Segment
 * @see Selector
 */
public final class PathExpression implements Comparable<PathExpression> {

	private static final PathExpression ROOT = new PathExpression(Collections.emptyList());

	public static final String ROOT_EXPRESSION = "$";

	private final List<Segment> segments;
	private volatile int cachedHashCode;
	private volatile String cachedExpression;
	private volatile PathExpression cachedParent;
	private final ConcurrentMap<String, PathExpression> cachedChildren = new ConcurrentHashMap<>(16);
	private volatile byte cachedHasWildcard; // 0=not computed, 1=false, 2=true
	private volatile byte cachedHasTypeSelector; // 0=not computed, 1=false, 2=true

	private PathExpression(List<Segment> segments) {
		this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
	}

	private PathExpression(List<Segment> segments, boolean trusted) {
		this.segments = Collections.unmodifiableList(segments);
	}

	public static PathExpression root() {
		return ROOT;
	}

	/**
	 * Parses a path expression string (e.g., {@code "$.items[0]"}, {@code "$.items[*]"}).
	 */
	public static PathExpression of(String expression) {
		if (expression == null || expression.isEmpty() || expression.equals("$")) {
			return ROOT;
		}

		List<Segment> segments = parseExpression(expression);
		return new PathExpression(segments);
	}

	private static List<Segment> parseExpression(String expression) {
		Objects.requireNonNull(expression, "expression must not be null");

		List<Segment> segments = new ArrayList<>();
		String remaining;

		if (expression.startsWith("$")) {
			remaining = expression.substring(1);
		} else if (expression.startsWith(".")) {
			throw new IllegalArgumentException(
				"Path starting with '.' is not supported. Use '$.' or omit '$': " + expression
			);
		} else {
			// For backward compatibility with previous fixture-monkey spec,
			// expressions without '$' prefix are treated as '$.expression'.
			remaining = "." + expression;
		}

		while (!remaining.isEmpty()) {
			if (remaining.startsWith(".")) {
				remaining = remaining.substring(1);
				int end = findNameEnd(remaining);
				if (end == 0) {
					throw new IllegalArgumentException("Empty property name in path: " + expression);
				}
				String name = remaining.substring(0, end);
				segments.add(Segment.ofName(name));
				remaining = remaining.substring(end);
			} else if (remaining.startsWith("[")) {
				int closeIdx = remaining.indexOf(']');
				if (closeIdx == -1) {
					throw new IllegalArgumentException("Unclosed bracket in path: " + expression);
				}
				String content = remaining.substring(1, closeIdx);
				segments.add(parseBracketContent(content, expression));
				remaining = remaining.substring(closeIdx + 1);
			} else {
				throw new IllegalArgumentException(
					"Unexpected character '" + remaining.charAt(0) + "' in path: " + expression
				);
			}
		}

		return segments;
	}

	private static int findNameEnd(String str) {
		int index = 0;
		while (index < str.length()) {
			char character = str.charAt(index);
			if (character == '.' || character == '[') {
				break;
			}
			index++;
		}
		return index;
	}

	private static Segment parseBracketContent(String content, String expression) {
		if (content.equals("*")) {
			return Segment.ofWildcard();
		}
		if (content.equals("key")) {
			return Segment.ofKey();
		}
		if (content.equals("value")) {
			return Segment.ofValue();
		}

		if (!content.contains(",")) {
			return parseIndex(content, expression);
		}

		String[] parts = content.split(",");
		return parseUnion(parts, expression);
	}

	private static Segment parseIndex(String content, String expression) {
		try {
			int index = Integer.parseInt(content.trim());
			return Segment.ofIndex(index);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid index '" + content + "' in path: " + expression);
		}
	}

	private static Segment parseUnion(String[] parts, String expression) {
		String first = parts[0].trim();

		if (first.equals("key") || first.equals("value")) {
			return parseKeyValueUnion(parts, expression);
		}

		return parseIndexUnion(parts, expression);
	}

	private static Segment parseIndexUnion(String[] parts, String expression) {
		int[] indices = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i].trim();
			if (part.isEmpty()) {
				throw new IllegalArgumentException("Empty selector in union: " + expression);
			}
			try {
				indices[i] = Integer.parseInt(part);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid index '" + part + "' in union: " + expression);
			}
		}
		return Segment.ofIndices(indices);
	}

	private static Segment parseKeyValueUnion(String[] parts, String expression) {
		List<Selector> selectors = new java.util.ArrayList<>();
		for (String part : parts) {
			String trimmed = part.trim();
			if (trimmed.equals("key")) {
				selectors.add(new KeySelector());
			} else if (trimmed.equals("value")) {
				selectors.add(new ValueSelector());
			} else {
				throw new IllegalArgumentException("Mixed types in key/value union: " + expression);
			}
		}
		return Segment.of(selectors);
	}

	public PathExpression child(String propertyName) {
		Objects.requireNonNull(propertyName, "propertyName must not be null");

		PathExpression cached = cachedChildren.get(propertyName);
		if (cached != null) {
			return cached;
		}

		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofName(propertyName));
		PathExpression result = new PathExpression(newSegments, true);

		PathExpression existing = cachedChildren.putIfAbsent(propertyName, result);
		return existing != null ? existing : result;
	}

	public PathExpression index(int index) {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofIndex(index));
		return new PathExpression(newSegments, true);
	}

	public PathExpression key() {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofKey());
		return new PathExpression(newSegments, true);
	}

	public PathExpression value() {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofValue());
		return new PathExpression(newSegments, true);
	}

	public PathExpression type(Class<?> type) {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofType(type));
		return new PathExpression(newSegments, true);
	}

	/**
	 * @param exact if true, only exact type matches; if false, subtypes also match
	 */
	public PathExpression type(Class<?> type, boolean exact) {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofType(type, exact));
		return new PathExpression(newSegments, true);
	}

	public PathExpression appendSegment(Segment segment) {
		Objects.requireNonNull(segment, "segment must not be null");
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(segment);
		return new PathExpression(newSegments, true);
	}

	public PathExpression append(PathExpression other) {
		if (other.isRoot()) {
			return this;
		}
		List<Segment> newSegments = new ArrayList<>(segments.size() + other.segments.size());
		newSegments.addAll(segments);
		newSegments.addAll(other.segments);
		return new PathExpression(newSegments, true);
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public String toExpression() {
		String cached = cachedExpression;
		if (cached != null) {
			return cached;
		}
		if (segments.isEmpty()) {
			cachedExpression = "$";
			return "$";
		}
		StringBuilder sb = new StringBuilder("$");
		for (Segment segment : segments) {
			sb.append(segment.toExpression());
		}
		cached = sb.toString();
		cachedExpression = cached;
		return cached;
	}

	public boolean startsWith(PathExpression prefix) {
		if (prefix.segments.size() > segments.size()) {
			return false;
		}
		for (int i = 0; i < prefix.segments.size(); i++) {
			if (!prefix.segments.get(i).equals(segments.get(i))) {
				return false;
			}
		}
		return true;
	}

	public boolean isChildOf(PathExpression parent) {
		return startsWith(parent) && segments.size() > parent.segments.size();
	}

	public boolean isRoot() {
		return segments.isEmpty();
	}

	public int depth() {
		return segments.size();
	}

	public PathExpression getParent() {
		if (segments.isEmpty() || segments.size() == 1) {
			return ROOT;
		}
		PathExpression cached = cachedParent;
		if (cached != null) {
			return cached;
		}
		cached = new PathExpression(new ArrayList<>(segments.subList(0, segments.size() - 1)));
		cachedParent = cached;
		return cached;
	}

	@Nullable
	public Segment getLastSegment() {
		if (segments.isEmpty()) {
			return null;
		}
		return segments.get(segments.size() - 1);
	}

	/**
	 * Checks if this pattern matches the given path.
	 * {@link WildcardSelector} matches any {@link IndexSelector}, {@link KeySelector}, or {@link ValueSelector}.
	 */
	public boolean matches(PathExpression path) {
		List<Segment> pathSegments = path.getSegments();

		if (segments.size() != pathSegments.size()) {
			return false;
		}

		for (int i = 0; i < segments.size(); i++) {
			Segment patternSegment = segments.get(i);
			Segment pathSegment = pathSegments.get(i);

			if (!segmentMatches(patternSegment, pathSegment)) {
				return false;
			}
		}

		return true;
	}

	private boolean segmentMatches(Segment patternSegment, Segment pathSegment) {
		if (!patternSegment.isSingleSelector() || !pathSegment.isSingleSelector()) {
			for (Selector patternSelector : patternSegment.getSelectors()) {
				for (Selector pathSelector : pathSegment.getSelectors()) {
					if (selectorMatches(patternSelector, pathSelector)) {
						return true;
					}
				}
			}
			return false;
		}

		return selectorMatches(patternSegment.getFirstSelector(), pathSegment.getFirstSelector());
	}

	private boolean selectorMatches(Selector patternSelector, Selector pathSelector) {
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
			// ".*" is the field-level wildcard — matches any NameSelector
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
	 * Returns true if this path contains {@link WildcardSelector} ({@code [*]}) or {@code .*}.
	 */
	public boolean hasWildcard() {
		byte cached = cachedHasWildcard;
		if (cached != 0) {
			return cached == 2;
		}
		for (Segment segment : segments) {
			for (Selector selector : segment.getSelectors()) {
				if (selector instanceof WildcardSelector) {
					cachedHasWildcard = 2;
					return true;
				}
				if (selector instanceof NameSelector && "*".equals(((NameSelector)selector).getName())) {
					cachedHasWildcard = 2;
					return true;
				}
			}
		}
		cachedHasWildcard = 1;
		return false;
	}

	public boolean hasTypeSelector() {
		byte cached = cachedHasTypeSelector;
		if (cached != 0) {
			return cached == 2;
		}
		for (Segment segment : segments) {
			for (Selector selector : segment.getSelectors()) {
				if (selector instanceof TypeSelector) {
					cachedHasTypeSelector = 2;
					return true;
				}
			}
		}
		cachedHasTypeSelector = 1;
		return false;
	}

	public PathExpression wildcard() {
		List<Segment> newSegments = new ArrayList<>(segments.size() + 1);
		newSegments.addAll(segments);
		newSegments.add(Segment.ofWildcard());
		return new PathExpression(newSegments, true);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PathExpression that = (PathExpression)obj;
		if (cachedHashCode != 0 && that.cachedHashCode != 0 && cachedHashCode != that.cachedHashCode) {
			return false;
		}
		return Objects.equals(segments, that.segments);
	}

	@Override
	public int hashCode() {
		int hashCode = cachedHashCode;
		if (hashCode == 0 && !segments.isEmpty()) {
			hashCode = Objects.hash(segments);
			cachedHashCode = hashCode;
		}
		return hashCode;
	}

	@Override
	public int compareTo(PathExpression other) {
		return toExpression().compareTo(other.toExpression());
	}

	@Override
	public String toString() {
		return toExpression();
	}
}
