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

import java.util.Objects;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.api.property.Property;

public final class NamedMatcherOperator<T> extends MatcherOperator<T> {
	private final String registeredName;

	public NamedMatcherOperator(Matcher matcher, T operator, String registeredName) {
		super(matcher, operator);
		this.registeredName = registeredName;
	}

	public boolean matchRegisteredName(String selectName) {
		return this.registeredName.equals(selectName);
	}

	@Override
	public boolean match(Property property, @Nullable MatcherMetadata<?> matcherMetadata) {
		return super.match(property) && registeredName.equals(
			Objects.requireNonNull(matcherMetadata).getMetadataInfo()
		);
	}
}
