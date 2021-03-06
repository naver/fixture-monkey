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

package com.navercorp.fixturemonkey.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;

@SuppressWarnings("rawtypes")
public class FieldArbitraries {
	private final Map<String, Arbitrary> arbitraryMap;

	public FieldArbitraries(Map<String, Arbitrary> arbitraryMap) {
		this.arbitraryMap = new HashMap<>(arbitraryMap);
	}

	public boolean isArbitrary(String fieldName) {
		return this.arbitraryMap.containsKey(fieldName);
	}

	@Nullable
	public Arbitrary<?> getArbitrary(String fieldName) {
		return arbitraryMap.get(fieldName);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Arbitrary> T getArbitrary(String fieldName, Class<T> arbitraryType) {
		return (T)arbitraryMap.get(fieldName);
	}

	public void putArbitrary(String fieldName, Arbitrary arbitrary) {
		this.arbitraryMap.put(fieldName, arbitrary);
	}

	public void removeArbitrary(String fieldName) {
		this.arbitraryMap.remove(fieldName);
	}

	public void replaceArbitrary(String fieldName, Arbitrary arbitrary) {
		if (!this.arbitraryMap.containsKey(fieldName)) {
			throw new IllegalStateException("fieldName does not exist field. fieldName: " + fieldName);
		}

		this.arbitraryMap.put(fieldName, arbitrary);
	}

	public void applyArbitrary(String fieldName, Function<Arbitrary, Arbitrary> apply) {
		Arbitrary<?> arbitrary = this.arbitraryMap.get(fieldName);
		if (arbitrary == null) {
			throw new IllegalStateException("fieldName does not exist field. fieldName: " + fieldName);
		}

		this.arbitraryMap.put(fieldName, apply.apply(arbitrary));
	}

	@SuppressWarnings("unchecked")
	public <T extends Arbitrary> void applyArbitrary(String fieldName, Class<T> arbitraryType,
		Function<T, Arbitrary<T>> apply) {
		T arbitrary = (T)this.arbitraryMap.get(fieldName);
		if (arbitrary == null) {
			throw new IllegalStateException("fieldName does not exist field. fieldName: " + fieldName);
		}

		this.arbitraryMap.put(fieldName, apply.apply(arbitrary));
	}

	public Set<Map.Entry<String, Arbitrary>> entrySet() {
		return this.arbitraryMap.entrySet();
	}

	public FieldArbitraries clear() {
		this.arbitraryMap.clear();
		return this;
	}
}
