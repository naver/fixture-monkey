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
public class MatcherOperator<T> implements Matcher {
	private final Matcher matcher;
	private final T operator;

	public MatcherOperator(Matcher matcher, T operator) {
		this.matcher = matcher;
		this.operator = operator;
	}

	public static <T, C> MatcherOperator<T> exactTypeMatchOperator(Class<C> type, T operator) {
		return new MatcherOperator<>(new ExactTypeMatcher(type), operator);
	}

	public static <T, C> MatcherOperator<T> assignableTypeMatchOperator(Class<C> type, T operator) {
		return new MatcherOperator<>(new AssignableTypeMatcher(type), operator);
	}

	@Override
	public boolean match(Property property, MatcherMetadata matcherMetadata) {
		return this.matcher.match(property, matcherMetadata);
	}

	@Override
	public boolean match(Property property) {
		return this.matcher.match(property);
	}

	public Matcher getMatcher() {
		return this.matcher;
	}

	public T getOperator() {
		return this.operator;
	}
}
