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

import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "1.1.12", status = EXPERIMENTAL)
public final class NamedMatcher implements Matcher {
	private final Matcher matcher;
	private final String registeredName;

	public NamedMatcher(Matcher matcher, String registeredName) {
		this.matcher = matcher;
		this.registeredName = registeredName;
	}

	@Override
	public boolean match(Property property) {
		return this.matcher.match(property);
	}

	@Override
	public boolean match(Property property, MatcherMetadata matcherMetadata) {
		return this.matcher.match(property) && registeredName.equals(matcherMetadata.getName());
	}
}
