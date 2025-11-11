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

package com.navercorp.fixturemonkey.api.context;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.property.PropertyPath;

/**
 * It is the same context as {@code TraverseContext}, but exposed as public by {@link ArbitraryGeneratorContext}.
 * It focuses mainly on generation.
 */
@API(since = "0.4.3", status = Status.MAINTAINED)
public final class MonkeyGeneratorContext {
	private final SortedMap<PropertyPath, Set<Object>> uniqueSetsByProperty;

	public MonkeyGeneratorContext(SortedMap<PropertyPath, Set<Object>> uniqueSetsByProperty) {
		this.uniqueSetsByProperty = uniqueSetsByProperty;
	}

	@SuppressWarnings("argument")
	public synchronized boolean isUniqueAndCheck(PropertyPath property, @Nullable Object value) {
		Set<Object> set = uniqueSetsByProperty.computeIfAbsent(property, p -> new HashSet<>());
		boolean unique = !set.contains(value);
		if (unique) {
			set.add(value);
			return true;
		}

		return false;
	}

	public void evictUnique(PropertyPath propertyPath) {
		if (uniqueSetsByProperty.containsKey(propertyPath)) {
			uniqueSetsByProperty.get(propertyPath).clear();
		}
	}
}
