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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ChildArbitraryContext {
	private final Property parentProperty;
	private final Map<ArbitraryProperty, Arbitrary<?>> arbitrariesByChildProperty;

	public ChildArbitraryContext(
		Property parentProperty,
		Map<ArbitraryProperty, Arbitrary<?>> arbitrariesByChildProperty
	) {
		this.parentProperty = parentProperty;
		this.arbitrariesByChildProperty = arbitrariesByChildProperty;
	}

	public Property getParentProperty() {
		return parentProperty;
	}

	public void replaceArbitrary(Matcher matcher, Arbitrary<?> arbitrary) {
		for (Entry<ArbitraryProperty, Arbitrary<?>> arbitraryByChildProperty : arbitrariesByChildProperty.entrySet()) {
			ArbitraryProperty arbitraryProperty = arbitraryByChildProperty.getKey();
			if (matcher.match(arbitraryProperty.getProperty())) {
				arbitrariesByChildProperty.put(arbitraryProperty, arbitrary);
			}
		}
	}

	public void removeArbitrary(Matcher matcher) {
		arbitrariesByChildProperty.entrySet().removeIf(it -> matcher.match(it.getKey().getProperty()));
	}

	public Map<String, Arbitrary<?>> getArbitrariesByResolvedName() {
		return arbitrariesByChildProperty.entrySet().stream()
			.collect(toMap(it -> it.getKey().getResolvePropertyName(), Entry::getValue));
	}

	public List<Arbitrary<?>> getArbitraries() {
		return new ArrayList<>(arbitrariesByChildProperty.values());
	}
}
