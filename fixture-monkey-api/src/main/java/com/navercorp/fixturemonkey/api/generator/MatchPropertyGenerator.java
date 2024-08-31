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

package com.navercorp.fixturemonkey.api.generator;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.CompositePropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * Generates the properties by a matched {@link PropertyGenerator}.
 * <p>
 * It is different from the {@link CompositePropertyGenerator}, which uses all the {@link PropertyGenerator}s
 * and combines all the generated properties as a {@link CompositeProperty}.
 * <p>
 * It only uses a matching {@link PropertyGenerator}, not throws Exception if no matched {@link PropertyGenerator}.
 */
@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class MatchPropertyGenerator implements PropertyGenerator {
	private final List<MatcherOperator<PropertyGenerator>> propertyGenerators;

	public MatchPropertyGenerator(List<MatcherOperator<PropertyGenerator>> propertyGenerators) {
		this.propertyGenerators = propertyGenerators;
	}

	@Override
	public List<Property> generateChildProperties(Property property) {
		for (MatcherOperator<PropertyGenerator> propertyGenerator : propertyGenerators) {
			if (propertyGenerator.getMatcher().match(property)) {
				return propertyGenerator.getOperator().generateChildProperties(property);
			}
		}

		throw new IllegalArgumentException("Type " + property.getType() + " has no matching PropertyGenerator.");
	}
}
