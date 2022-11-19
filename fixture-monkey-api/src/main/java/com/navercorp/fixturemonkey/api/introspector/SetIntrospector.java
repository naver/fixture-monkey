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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.unique.FilteredMonkeyArbitrary;
import com.navercorp.fixturemonkey.api.unique.MonkeyCombineArbitrary;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class SetIntrospector implements ArbitraryIntrospector, Matcher {
	private static final Matcher MATCHER = new AssignableTypeMatcher(Set.class);
	private static final int MAX_TRIES = 10000;

	@Override
	public boolean match(Property property) {
		return MATCHER.match(property);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty arbitraryProperty = context.getArbitraryProperty();
		ContainerProperty containerProperty = arbitraryProperty.getContainerProperty();
		Property property = arbitraryProperty.getObjectProperty().getProperty();
		if (containerProperty == null) {
			throw new IllegalArgumentException(
				"container arbitraryProperty should not null. property : " + property.getName()
			);
		}
		ArbitraryContainerInfo containerInfo = containerProperty.getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<Arbitrary<Object>> childrenArbitraries = context.getChildrenArbitraryContexts().getArbitraries().stream()
			.map(arbitrary ->
				new FilteredMonkeyArbitrary<>(
					arbitrary,
					it -> context.isUniqueAndCheck(
						context.getPathProperty(),
						it
					),
					MAX_TRIES
				)
			)
			.collect(Collectors.toList());

		MonkeyCombineArbitrary monkeyCombineArbitrary = new MonkeyCombineArbitrary(
			HashSet::new,
			() -> context.evict(context.getPathProperty()),
			childrenArbitraries
		);

		return new ArbitraryIntrospectorResult(monkeyCombineArbitrary);
	}
}
