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

package com.navercorp.fixturemonkey.customizer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

/**
 * A class that represents a matcher operator with a priority.
 * This class extends {@link MatcherOperator} and adds a priority field.
 *
 * @param <T> the type of the operator
 * @since 1.1.12
 */
@API(since = "1.1.12", status = EXPERIMENTAL)
public final class PriorityMatcherOperator<T> extends MatcherOperator<T> {
	private final int priority;

	public PriorityMatcherOperator(
		Matcher matcher,
		T operator,
		int priority
	) {
		super(matcher, operator);
		checkPriority(priority);
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

	/**
	 * Checks if the priority is valid.
	 *
	 * @param priority the priority to be checked
	 * @throws IllegalArgumentException if the priority is less than 0
	 */
	private void checkPriority(int priority) {
		if (priority < 0) {
			throw new IllegalArgumentException("Priority must be greater than or equal to 0");
		}
	}
}
