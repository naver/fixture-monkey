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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ChildArbitraryContext {
	private final Property parentProperty;
	private final Map<ArbitraryProperty, LazyArbitrary<Arbitrary<?>>> arbitrariesByChildProperty;

	public ChildArbitraryContext(
		Property parentProperty,
		Map<ArbitraryProperty, LazyArbitrary<Arbitrary<?>>> arbitrariesByChildProperty
	) {
		this.parentProperty = parentProperty;
		this.arbitrariesByChildProperty = arbitrariesByChildProperty;
	}

	public Property getParentProperty() {
		return parentProperty;
	}

	public void replaceArbitrary(Matcher matcher, Arbitrary<?> arbitrary) {
		for (Entry<ArbitraryProperty, LazyArbitrary<Arbitrary<?>>> arbitraryByChildProperty
			: arbitrariesByChildProperty.entrySet()) {
			ArbitraryProperty arbitraryProperty = arbitraryByChildProperty.getKey();
			ObjectProperty objectProperty = arbitraryProperty.getObjectProperty();
			if (matcher.match(objectProperty.getProperty())) {
				arbitrariesByChildProperty.put(arbitraryProperty, LazyArbitrary.lazy(() -> arbitrary));
			}
		}
	}

	public void removeArbitrary(Matcher matcher) {
		arbitrariesByChildProperty.entrySet()
			.removeIf(it -> matcher.match(it.getKey().getObjectProperty().getProperty()));
	}

	public Map<String, LazyArbitrary<Arbitrary<?>>> getArbitrariesByPropertyName() {
		return arbitrariesByChildProperty.entrySet().stream()
			.collect(toMap(it -> it.getKey().getObjectProperty().getProperty().getName(), Entry::getValue));
	}

	public Map<String, LazyArbitrary<Arbitrary<?>>> getArbitrariesByResolvedName() {
		return arbitrariesByChildProperty.entrySet().stream()
			.collect(toMap(it -> it.getKey().getObjectProperty().getResolvedPropertyName(), Entry::getValue));
	}

	public List<Arbitrary<?>> getArbitraries() {
		return arbitrariesByChildProperty.values().stream()
			.map(LazyArbitrary::getValue)
			.collect(Collectors.toList());
	}
}
