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
import java.util.Arrays;
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

	private static final PathExpression ROOT = new PathExpression(new Segment[0]);

	public static final String ROOT_EXPRESSION = "$";

	// A bare array rather than a List: a path is created for every node and every element of a set value, and a
	// list would add a wrapper and a backing array to each of them. The array never leaves this class, so the path
	// stays immutable.
	private final Segment[] segments;
	private volatile int cachedHashCode;
	private volatile String cachedExpression;
	private volatile PathExpression cachedParent;
	private volatile ConcurrentMap<String, PathExpression> cachedChildren;
	private volatile byte cachedHasWildcard; // 0=not computed, 1=false, 2=true

	private PathExpression(Segment[] segments) {
		this.segments = segments;
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
		Segment[] parsed = new Segment[segments.size()];
		for (int i = 0; i < parsed.length; i++) {
			parsed[i] = segments.get(i);
		}
		return new PathExpression(parsed);
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

		ConcurrentMap<String, PathExpression> children = childrenCache();
		PathExpression cached = children.get(propertyName);
		if (cached != null) {
			return cached;
		}

		PathExpression result = withLastSegment(Segment.ofName(propertyName));

		PathExpression existing = children.putIfAbsent(propertyName, result);
		return existing != null ? existing : result;
	}

	public PathExpression index(int index) {
		return withLastSegment(Segment.ofIndex(index));
	}

	public PathExpression key() {
		return withLastSegment(Segment.ofKey());
	}

	public PathExpression value() {
		return withLastSegment(Segment.ofValue());
	}

	public PathExpression type(Class<?> type) {
		return withLastSegment(Segment.ofType(type));
	}

	/**
	 * @param exact if true, only exact type matches; if false, subtypes also match
	 */
	public PathExpression type(Class<?> type, boolean exact) {
		return withLastSegment(Segment.ofType(type, exact));
	}

	public PathExpression append(PathExpression other) {
		if (other.isRoot()) {
			return this;
		}
		Segment[] joined = new Segment[segments.length + other.segments.length];
		System.arraycopy(segments, 0, joined, 0, segments.length);
		System.arraycopy(other.segments, 0, joined, segments.length, other.segments.length);
		return new PathExpression(joined);
	}

	/**
	 * Returns the segment at {@code index}, counted from the root.
	 *
	 * @param index the position of the segment, from 0 to {@link #depth()} - 1
	 * @return the segment at the position
	 * @throws IndexOutOfBoundsException when {@code index} is negative or not less than {@link #depth()}
	 */
	public Segment getSegment(int index) {
		return segments[index];
	}

	private PathExpression withLastSegment(Segment segment) {
		Segment[] extended = new Segment[segments.length + 1];
		System.arraycopy(segments, 0, extended, 0, segments.length);
		extended[segments.length] = segment;
		PathExpression path = new PathExpression(extended);
		path.cachedParent = this;
		return path;
	}

	private Segment[] leadingSegments(int count) {
		Segment[] leading = new Segment[count];
		System.arraycopy(segments, 0, leading, 0, count);
		return leading;
	}

	private ConcurrentMap<String, PathExpression> childrenCache() {
		ConcurrentMap<String, PathExpression> children = cachedChildren;
		if (children == null) {
			synchronized (this) {
				children = cachedChildren;
				if (children == null) {
					children = new ConcurrentHashMap<>(4);
					cachedChildren = children;
				}
			}
		}
		return children;
	}

	public String toExpression() {
		String cached = cachedExpression;
		if (cached != null) {
			return cached;
		}
		if (segments.length == 0) {
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
		if (prefix.segments.length > segments.length) {
			return false;
		}
		for (int i = 0; i < prefix.segments.length; i++) {
			if (!prefix.segments[i].equals(segments[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean isChildOf(PathExpression parent) {
		return startsWith(parent) && segments.length > parent.segments.length;
	}

	public boolean isRoot() {
		return segments.length == 0;
	}

	public int depth() {
		return segments.length;
	}

	public PathExpression getParent() {
		if (segments.length <= 1) {
			return ROOT;
		}
		PathExpression cached = cachedParent;
		if (cached != null) {
			return cached;
		}
		cached = new PathExpression(leadingSegments(segments.length - 1));
		cachedParent = cached;
		return cached;
	}

	/**
	 * Returns this path cut down to its first {@code depth} segments.
	 *
	 * @param depth the number of leading segments to keep
	 * @return the root when {@code depth} is zero or less, this path when it covers every segment
	 */
	public PathExpression truncateTo(int depth) {
		if (depth <= 0) {
			return ROOT;
		}
		if (depth >= segments.length) {
			return this;
		}
		return new PathExpression(leadingSegments(depth));
	}

	@Nullable
	public Segment getLastSegment() {
		if (segments.length == 0) {
			return null;
		}
		return segments[segments.length - 1];
	}

	/**
	 * Checks if this pattern matches the given path, segment by segment as {@link Segment#matches(Segment)} does.
	 */
	public boolean matches(PathExpression path) {
		Segment[] pathSegments = path.segments;

		if (segments.length != pathSegments.length) {
			return false;
		}

		for (int i = 0; i < segments.length; i++) {
			Segment patternSegment = segments[i];
			Segment pathSegment = pathSegments[i];

			if (!patternSegment.matches(pathSegment)) {
				return false;
			}
		}

		return true;
	}


	/**
	 * Returns true if this path is a pattern that matches more than one concrete path: it contains
	 * {@link WildcardSelector} ({@code [*]}), {@code .*}, or a union of selectors such as {@code [0,1]}.
	 */
	public boolean hasWildcard() {
		byte cached = cachedHasWildcard;
		if (cached != 0) {
			return cached == 2;
		}
		for (Segment segment : segments) {
			if (!segment.isSingleSelector()) {
				cachedHasWildcard = 2;
				return true;
			}
			Selector selector = segment.getFirstSelector();
			if (selector instanceof WildcardSelector) {
				cachedHasWildcard = 2;
				return true;
			}
			if (selector instanceof NameSelector && "*".equals(((NameSelector)selector).getName())) {
				cachedHasWildcard = 2;
				return true;
			}
		}
		cachedHasWildcard = 1;
		return false;
	}

	public PathExpression wildcard() {
		return withLastSegment(Segment.ofWildcard());
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
		return Arrays.equals(segments, that.segments);
	}

	@Override
	public int hashCode() {
		int hashCode = cachedHashCode;
		if (hashCode == 0 && segments.length != 0) {
			hashCode = 31 + Arrays.hashCode(segments);
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
