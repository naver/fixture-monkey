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

import java.util.function.Function;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

public final class PriorityMatcherOperator {
	private final int priority;
	private final MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> matcherOperator;

	public PriorityMatcherOperator(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> matcherOperator,
		int priority
	) {
		checkPriority(priority);
		this.matcherOperator = matcherOperator;
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> getMatcherOperator() {
		return matcherOperator;
	}

	private void checkPriority(int priority) {
		if (priority < 0) {
			throw new IllegalArgumentException("Priority must be greater than or equal to 0");
		}
	}
}
