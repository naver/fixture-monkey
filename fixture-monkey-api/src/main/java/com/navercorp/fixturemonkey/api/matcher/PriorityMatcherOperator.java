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

import org.apiguardian.api.API;

/**
 * A class that represents a matcher operator with a priority.
 * Priority may be negative or positive. {@link DefaultMatcherOperatorContainer}
 * This class extends {@link MatcherOperator} and adds a priority field.
 * This class is intended for internal use only.
 *
 *
 * @param <T> the type of the operator
 * @since 1.1.15
 */
@API(since = "1.1.15", status = EXPERIMENTAL)
public final class PriorityMatcherOperator<T> extends MatcherOperator<T> {
	private final int priority;

	public PriorityMatcherOperator(
		Matcher matcher,
		T operator,
		int priority
	) {
		super(matcher, operator);
		this.priority = priority;
	}

	/**
	 * Returns the priority of this matcher operator.
	 *
	 * @return the priority of this matcher operator
	 */
	public int getPriority() {
		return priority;
	}
}
