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

/**
 * Represents a selector that identifies child nodes to select within a segment.
 * <p>
 * This follows the terminology from <a href="https://datatracker.ietf.org/doc/rfc9535/">JSONPath RFC 9535</a>,
 * where a selector is "a single item within a segment that takes the input value and produces
 * a nodelist consisting of child nodes of the input value."
 * <p>
 * Available selector types:
 * <ul>
 *   <li>{@link NameSelector} - name selector for object properties (e.g., "items")</li>
 *   <li>{@link IndexSelector} - index selector for array/list elements (e.g., "0")</li>
 *   <li>{@link WildcardSelector} - wildcard selector matching any child (e.g., "*")</li>
 *   <li>{@link KeySelector} - selector for Map entry keys</li>
 *   <li>{@link ValueSelector} - selector for Map entry values</li>
 *   <li>{@link TypeSelector} - type selector for matching nodes by Java type</li>
 * </ul>
 * <p>
 * Note: {@link KeySelector}, {@link ValueSelector}, and {@link TypeSelector} are extensions,
 * not part of the original JSONPath RFC.
 *
 * @see Segment
 * @see <a href="https://datatracker.ietf.org/doc/rfc9535/">RFC 9535 - JSONPath</a>
 */
public interface Selector {
	/**
	 * Returns the string representation of this selector (without brackets).
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>NameSelector: "items"</li>
	 *   <li>IndexSelector: "0"</li>
	 *   <li>WildcardSelector: "*"</li>
	 * </ul>
	 *
	 * @return selector as string
	 */
	String toExpression();
}
