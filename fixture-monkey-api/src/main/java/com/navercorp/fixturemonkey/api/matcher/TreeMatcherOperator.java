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

@API(since = "1.0.4", status = Status.EXPERIMENTAL)
public final class TreeMatcherOperator<T> implements TreeMatcher {
	private final TreeMatcher matcher;
	private final T operator;

	public TreeMatcherOperator(TreeMatcher matcher, T operator) {
		this.matcher = matcher;
		this.operator = operator;
	}

	@Override
	public boolean match(TreeMatcherMetadata metadata) {
		return this.matcher.match(metadata);
	}

	public T getOperator() {
		return operator;
	}
}
