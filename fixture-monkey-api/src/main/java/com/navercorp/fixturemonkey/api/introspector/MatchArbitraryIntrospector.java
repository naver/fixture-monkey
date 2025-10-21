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

package com.navercorp.fixturemonkey.api.introspector;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;

/**
 * Introspects by a matched {@link ArbitraryGenerator}.
 * <p>
 * It is different from {@link CompositeArbitraryIntrospector}.
 * A {@link ArbitraryIntrospector} not matching the condition returns {@code NOT_INTROSPECTED},
 * the next {@link ArbitraryIntrospector} will be used.
 * If there are one or more {@link ArbitraryIntrospector} that match the condition, the first one is used.
 */
@API(since = "0.6.12", status = Status.MAINTAINED)
public final class MatchArbitraryIntrospector implements ArbitraryIntrospector {
	private final List<ArbitraryIntrospector> introspectors;

	public MatchArbitraryIntrospector(List<ArbitraryIntrospector> introspectors) {
		this.introspectors = introspectors;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		for (ArbitraryIntrospector introspector : this.introspectors) {
			if (introspector instanceof Matcher) {
				if (!((Matcher)introspector).match(context.getResolvedProperty())) {
					continue;
				}
			}

			ArbitraryIntrospectorResult result = introspector.introspect(context);
			if (!ArbitraryIntrospectorResult.NOT_INTROSPECTED.equals(result)) {
				return result;
			}
		}

		return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
	}

	@Nullable
	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		for (ArbitraryIntrospector introspector : this.introspectors) {
			if (introspector instanceof Matcher && !((Matcher)introspector).match(property)) {
				continue;
			}

			PropertyGenerator propertyGenerator = introspector.getRequiredPropertyGenerator(property);
			if (propertyGenerator != null) {
				return propertyGenerator;
			}
		}
		return null;
	}
}
