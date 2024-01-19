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

/**
 * A matcher that combines two other matchers using a logical AND operation.
 */
@API(since = "1.0.13", status = Status.EXPERIMENTAL)
final class IntersectMatcher implements Matcher {
	private final Matcher first;
	private final Matcher second;

	public IntersectMatcher(Matcher first, Matcher second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean match(Property property) {
		return first.match(property) && second.match(property);
	}
}
